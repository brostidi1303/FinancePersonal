package com.example.financeapp.data.market

import com.example.financeapp.di.api.SsiBoardService
import com.example.financeapp.di.api.VnDirectChartService
import com.example.financeapp.domain.stock.BuyPlanAnalyzer
import com.example.financeapp.domain.stock.Horizon
import com.example.financeapp.domain.stock.MarketScanResult
import com.example.financeapp.domain.stock.StockAnalysis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Quét thị trường: lấy snapshot toàn sàn từ SSI, chọn danh mục theo thanh khoản,
 * tải lịch sử giá từ VNDirect rồi chấm điểm.
 *
 * Snapshot chỉ tốn 1 request cho cả 400+ mã, nên danh mục được lọc TRƯỚC khi
 * tải lịch sử — tránh bắn hàng trăm request vô ích từ điện thoại.
 */
@Singleton
class MarketScanRepository @Inject constructor(
    private val ssiService: SsiBoardService,
    private val chartService: VnDirectChartService,
    private val analyzer: BuyPlanAnalyzer
) {

    companion object {
        private const val HISTORY_DAYS = 200
        private const val SECONDS_PER_DAY = 86_400L

        /** Số request lịch sử chạy song song. Giữ thấp để không bị chặn tần suất. */
        private const val MAX_CONCURRENCY = 6

        private const val CACHE_TTL_MS = 5 * 60 * 1000L
    }

    private var cached: MarketScanResult? = null

    /**
     * @param forceRefresh bỏ qua cache 5 phút
     * @return kết quả quét, hoặc Result.failure nếu không lấy được dữ liệu nền
     */
    suspend fun scan(forceRefresh: Boolean = false): Result<MarketScanResult> = runCatching {
        cached?.let {
            if (!forceRefresh && System.currentTimeMillis() - it.scannedAt < CACHE_TTL_MS) {
                return@runCatching it
            }
        }
        withContext(Dispatchers.IO) { performScan() }.also { cached = it }
    }

    private suspend fun performScan(): MarketScanResult = coroutineScope {
        val to = System.currentTimeMillis() / 1000
        val from = to - HISTORY_DAYS * SECONDS_PER_DAY

        // 1. Snapshot toàn sàn HOSE (1 request)
        val boardResponse = ssiService.getExchangeBoard("hose")
        val board = boardResponse.body()?.data
            ?: throw IllegalStateException(
                "Không lấy được bảng giá SSI (HTTP ${boardResponse.code()})"
            )

        val snapshots = board
            .filter { it.isCommonStock && StockUniverse.isCovered(it.stockSymbol!!) }
            .associateBy { it.stockSymbol!! }

        if (snapshots.isEmpty()) throw IllegalStateException("Bảng giá SSI không có mã nào khớp danh mục")

        // 2. Chọn danh mục theo giá trị giao dịch, cắt còn MAX_UNIVERSE_SIZE
        val universe = snapshots.values
            .sortedByDescending { it.nmTotalTradedValue ?: 0.0 }
            .take(StockUniverse.MAX_UNIVERSE_SIZE)
            .mapNotNull { it.stockSymbol }

        // 3. Lịch sử VNINDEX để dựng bối cảnh thị trường
        val indexCandles = fetchCandles(StockUniverse.INDEX_SYMBOL, from, to)
            ?: throw IllegalStateException("Không lấy được dữ liệu VNINDEX")
        val context = analyzer.buildContext(indexCandles)

        // 4. Tải lịch sử từng mã, giới hạn số request song song
        val gate = Semaphore(MAX_CONCURRENCY)
        val histories = universe.map { symbol ->
            async { symbol to gate.withPermit { fetchCandles(symbol, from, to) } }
        }.awaitAll()

        // 5. Chấm điểm
        val analyses = mutableListOf<StockAnalysis>()
        val skipped = mutableMapOf<String, Int>()
        histories.forEach { (symbol, candles) ->
            val snap = snapshots[symbol]
            if (candles == null || snap == null) {
                skipped.merge("Không tải được dữ liệu", 1, Int::plus)
                return@forEach
            }
            when (val r = analyzer.analyze(symbol, candles, snap, context)) {
                is BuyPlanAnalyzer.Result.Ok -> analyses += r.analysis
                is BuyPlanAnalyzer.Result.Skipped -> skipped.merge(r.reason, 1, Int::plus)
            }
        }

        Timber.d("Quét xong: %d mã phân tích, %d bị loại", analyses.size, skipped.values.sum())

        MarketScanResult(
            scannedAt = System.currentTimeMillis(),
            context = context,
            analyses = analyses,
            sectorScores = analyzer.sectorScores(analyses, Horizon.MEDIUM),
            skipped = skipped
        )
    }

    /**
     * Bảng giá trong phiên cho TOÀN SÀN HOSE.
     *
     * Chỉ tốn đúng 1 request cho cả 400+ mã nên gọi lặp mỗi 20 giây vẫn rất nhẹ,
     * và không giới hạn trong danh mục VN100 như hàm [scan].
     */
    suspend fun fetchLiveBoard(): Result<LiveBoard> = runCatching {
        withContext(Dispatchers.IO) {
            val response = ssiService.getExchangeBoard("hose")
            val rows = response.body()?.data
                ?: throw IllegalStateException(
                    "Không lấy được bảng giá SSI (HTTP ${response.code()})"
                )

            val quotes = rows.filter { it.isCommonStock && (it.matchedPrice ?: 0.0) > 0.0 }
                .map { r ->
                    LiveQuote(
                        symbol = r.stockSymbol!!,
                        sector = StockUniverse.sectorOf(r.stockSymbol),
                        price = r.matchedPrice ?: 0.0,
                        referencePrice = r.refPrice ?: 0.0,
                        changePercent = r.priceChangePercent ?: 0.0,
                        openPrice = r.openPrice ?: 0.0,
                        high = r.highest ?: 0.0,
                        low = r.lowest ?: 0.0,
                        ceiling = r.ceiling ?: 0.0,
                        floor = r.floor ?: 0.0,
                        volume = r.nmTotalTradedQty ?: 0.0,
                        tradedValue = r.nmTotalTradedValue ?: 0.0,
                        foreignBuyValue = r.buyForeignValue ?: 0.0,
                        foreignSellValue = r.sellForeignValue ?: 0.0
                    )
                }

            if (quotes.isEmpty()) throw IllegalStateException("Bảng giá SSI đang trống")

            // Trường session giống nhau trên mọi mã; lấy giá trị phổ biến nhất cho chắc.
            val sessionCode = rows.mapNotNull { it.session }
                .groupingBy { it }.eachCount()
                .maxByOrNull { it.value }?.key

            LiveBoard(
                updatedAt = System.currentTimeMillis(),
                tradingDate = rows.firstNotNullOfOrNull { it.tradingDate },
                session = MarketSession.from(sessionCode),
                quotes = quotes
            )
        }
    }

    private suspend fun fetchCandles(symbol: String, from: Long, to: Long): List<Candle>? = try {
        val res = chartService.getHistory(symbol = symbol, resolution = "D", from = from, to = to)
        val body = res.body()
        if (res.isSuccessful && body != null && body.isOk) body.toCandles() else null
    } catch (e: Exception) {
        Timber.w(e, "Lỗi tải lịch sử %s", symbol)
        null
    }
}
