package com.example.financeapp.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financeapp.data.market.BoardSort
import com.example.financeapp.data.market.LiveBoard
import com.example.financeapp.data.market.LiveQuote
import com.example.financeapp.data.market.MarketScanRepository
import com.example.financeapp.domain.stock.Horizon
import com.example.financeapp.domain.stock.MarketScanResult
import com.example.financeapp.domain.stock.SectorScore
import com.example.financeapp.domain.stock.StockAnalysis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Ba chế độ xem của khối sàng lọc. */
enum class StockViewTab(val label: String) {
    THREE_DAY("3 ngày"),
    SEVEN_DAY("7 ngày"),
    LIVE("Trong phiên");

    /** Khung chấm điểm tương ứng; tab LIVE không chấm điểm nên trả về null. */
    val horizon: Horizon?
        get() = when (this) {
            THREE_DAY -> Horizon.SHORT
            SEVEN_DAY -> Horizon.MEDIUM
            LIVE -> null
        }
}

data class BuyPlanUiState(
    val isLoading: Boolean = false,
    val result: MarketScanResult? = null,
    val tab: StockViewTab = StockViewTab.SEVEN_DAY,
    val selectedSector: String? = null,
    val expandedSymbol: String? = null,
    val error: String? = null,

    // Bảng giá trong phiên
    val board: LiveBoard? = null,
    val boardSort: BoardSort = BoardSort.TOP_VALUE,
    val isBoardLoading: Boolean = false,
    val boardError: String? = null
) {
    /** Khung đang xem; tab LIVE vẫn cần một khung để tính điểm ngành nền. */
    val horizon: Horizon get() = tab.horizon ?: Horizon.MEDIUM

    val rankedStocks: List<StockAnalysis>
        get() = result?.analyses.orEmpty()
            .filter { selectedSector == null || it.sector == selectedSector }
            .sortedByDescending { it.planFor(horizon).score }

    val sectors: List<SectorScore>
        get() = result?.let { r ->
            r.analyses.groupBy { it.sector }
                .map { (sector, items) ->
                    val sorted = items.sortedByDescending { it.planFor(horizon).score }
                    SectorScore(
                        sector = sector,
                        averageScore = sorted.sumOf { it.planFor(horizon).score } / sorted.size,
                        symbolCount = sorted.size,
                        topSymbols = sorted.take(3).map { it.symbol }
                    )
                }
                .sortedByDescending { it.averageScore }
        }.orEmpty()

    /**
     * Bảng giá đã sắp xếp, luôn lấy TOÀN BỘ sàn.
     *
     * Cố ý không áp [selectedSector]: tab bảng giá không hiển thị bản đồ ngành
     * nên người dùng không có cách nào bỏ lọc ở đây, và một bộ lọc chọn từ tab
     * khác mà âm thầm cắt bớt bảng giá là hành vi gây hiểu nhầm.
     */
    val sortedQuotes: List<LiveQuote>
        get() = boardSort.apply(board?.quotes.orEmpty())
}

@HiltViewModel
class BuyPlanViewModel @Inject constructor(
    private val repository: MarketScanRepository
) : ViewModel() {

    companion object {
        /** Chu kỳ tự làm mới bảng giá khi phiên đang mở. */
        const val BOARD_REFRESH_MS = 20_000L
    }

    private val _uiState = MutableStateFlow(BuyPlanUiState())
    val uiState: StateFlow<BuyPlanUiState> = _uiState.asStateFlow()

    private var boardJob: Job? = null

    fun load(forceRefresh: Boolean = false) {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            repository.scan(forceRefresh)
                .onSuccess { r -> _uiState.update { it.copy(isLoading = false, result = r) } }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, error = e.message ?: "Không tải được dữ liệu thị trường")
                    }
                }
        }
    }

    fun setTab(tab: StockViewTab) {
        _uiState.update { it.copy(tab = tab) }
        // Việc BẬT vòng làm mới do màn hình quyết định (chỉ khi còn hiển thị),
        // ở đây chỉ cần tắt ngay khi rời khỏi tab bảng giá.
        if (tab != StockViewTab.LIVE) stopBoardUpdates()
    }

    fun setBoardSort(sort: BoardSort) = _uiState.update { it.copy(boardSort = sort) }

    fun selectSector(sector: String?) = _uiState.update {
        it.copy(selectedSector = if (it.selectedSector == sector) null else sector)
    }

    fun toggleExpanded(symbol: String) = _uiState.update {
        it.copy(expandedSymbol = if (it.expandedSymbol == symbol) null else symbol)
    }

    /**
     * Tải bảng giá lặp lại trong lúc tab LIVE đang mở.
     *
     * Vòng lặp tự dừng khi SSI báo phiên đã đóng — dùng trạng thái server trả về
     * thay vì so giờ máy, nên ngày nghỉ lễ cũng không gọi thừa.
     */
    fun startBoardUpdates() {
        if (boardJob?.isActive == true) return
        boardJob = viewModelScope.launch {
            while (isActive) {
                refreshBoardOnce()
                val session = _uiState.value.board?.session
                if (session != null && !session.isLive) break
                delay(BOARD_REFRESH_MS)
            }
        }
    }

    fun stopBoardUpdates() {
        boardJob?.cancel()
        boardJob = null
    }

    /** Làm mới bảng giá đúng một lần, dùng cho nút tải lại thủ công. */
    fun refreshBoard() {
        viewModelScope.launch { refreshBoardOnce() }
    }

    private suspend fun refreshBoardOnce() {
        _uiState.update { it.copy(isBoardLoading = true) }
        repository.fetchLiveBoard()
            .onSuccess { b ->
                _uiState.update { it.copy(board = b, isBoardLoading = false, boardError = null) }
            }
            .onFailure { e ->
                _uiState.update {
                    it.copy(
                        isBoardLoading = false,
                        boardError = e.message ?: "Không tải được bảng giá"
                    )
                }
            }
    }

    override fun onCleared() {
        stopBoardUpdates()
        super.onCleared()
    }
}
