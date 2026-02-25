package com.example.financeapp.viewModel

import android.app.Application
import android.icu.text.SimpleDateFormat
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financeapp.data.APP_CATEGORIES
import com.example.financeapp.data.AnomaliesResponse
import com.example.financeapp.data.Category
import com.example.financeapp.data.NarrativeResponse
import com.example.financeapp.data.NewsArticle
import com.example.financeapp.data.NewsBySectorResponse
import com.example.financeapp.data.NewsBySymbolResponse
import com.example.financeapp.data.NewsResponse
import com.example.financeapp.data.RegimeHistoryResponse
import com.example.financeapp.data.RegimeResponse
import com.example.financeapp.utils.PreferencesManager
import com.example.financeapp.data.Transaction
import com.example.financeapp.di.api.FinanceApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.internal.notifyAll
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class FinanceViewModel @Inject constructor(
    private val prefsManager: PreferencesManager,
    private val apiService: FinanceApiService // ✅ Inject API Service
) : ViewModel() { // ✅ Dùng ViewModel thuần thay vì AndroidViewModel

    // State cho balance
    private val _totalBalance = MutableStateFlow(0.0)
    val totalBalance: StateFlow<Double> = _totalBalance.asStateFlow()

    private val _initialBalance = MutableStateFlow(0.0)
    val initialBalance: StateFlow<Double> = _initialBalance.asStateFlow()

    // State cho transactions
    private val _transactions = mutableStateListOf<Transaction>()
    val transactions: SnapshotStateList<Transaction> = _transactions

    // ✅ State cho categories
    private val _categories = mutableStateListOf<Category>()
    val categories: SnapshotStateList<Category> = _categories

    // ✅ Dark mode state - DI CHUYỂN LÊN TRƯỚC init BLOCK
    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // ==================== API STATES ====================
    private val _marketRegime = MutableStateFlow<RegimeResponse?>(null)
    val marketRegime: StateFlow<RegimeResponse?> = _marketRegime.asStateFlow()

    // ✅ NEW: State cho history data
    private val _marketRegimeHistory = MutableStateFlow<RegimeHistoryResponse?>(null)
    val marketRegimeHistory: StateFlow<RegimeHistoryResponse?> = _marketRegimeHistory.asStateFlow()

    private val _anomalies = MutableStateFlow<AnomaliesResponse?>(null)
    val anomalies: StateFlow<AnomaliesResponse?> = _anomalies.asStateFlow()
    // ✅ NEW: State cho ngày được chọn
    private val _selectedAnomalyDate = MutableStateFlow<String?>(null)
    val selectedAnomalyDate: StateFlow<String?> = _selectedAnomalyDate.asStateFlow()

    private val _narrative = MutableStateFlow<NarrativeResponse?>(null)
    val narrative: StateFlow<NarrativeResponse?> = _narrative.asStateFlow()

    private val _selectedNarrativeDate = MutableStateFlow<String?>(null)
    val selectedNarrativeDate: StateFlow<String?> = _selectedNarrativeDate.asStateFlow()

    private val _news = MutableStateFlow<NewsResponse?>(null)
    val news: StateFlow<NewsResponse?> = _news.asStateFlow()

    private val _selectedNewsDate = MutableStateFlow<String?>(null)
    val selectedNewsDate: StateFlow<String?> = _selectedNewsDate.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isHistoryLoading = MutableStateFlow(false)
    val isHistoryLoading: StateFlow<Boolean> = _isHistoryLoading.asStateFlow()

    private val _isAnomaliesLoading = MutableStateFlow(false)
    val isAnomaliesLoading: StateFlow<Boolean> = _isAnomaliesLoading.asStateFlow()

    private val _isNarrativeLoading = MutableStateFlow(false)
    val isNarrativeLoading: StateFlow<Boolean> = _isNarrativeLoading.asStateFlow()

    private val _isNewsLoading = MutableStateFlow(false)
    val isNewsLoading: StateFlow<Boolean> = _isNewsLoading.asStateFlow()

    // ==================== NEWS ALL STATES ====================

    private val _allNews = MutableStateFlow<List<NewsArticle>>(emptyList())
    val allNews: StateFlow<List<NewsArticle>> = _allNews.asStateFlow()

    private val _isAllNewsLoading = MutableStateFlow(false)
    val isAllNewsLoading: StateFlow<Boolean> = _isAllNewsLoading.asStateFlow()

    private val _hasMoreNews = MutableStateFlow(true)
    val hasMoreNews: StateFlow<Boolean> = _hasMoreNews.asStateFlow()

    private val _newsBySector = MutableStateFlow<NewsBySectorResponse?>(null)
    val newsBySector: StateFlow<NewsBySectorResponse?> = _newsBySector.asStateFlow()

    private val _isSectorNewsLoading = MutableStateFlow(false)
    val isSectorNewsLoading: StateFlow<Boolean> = _isSectorNewsLoading.asStateFlow()

    private val _currentSearchSector = MutableStateFlow<String?>(null)
    val currentSearchSector: StateFlow<String?> = _currentSearchSector.asStateFlow()

    private val _newsBySymbol = MutableStateFlow<NewsBySymbolResponse?>(null)
    val newsBySymbol: StateFlow<NewsBySymbolResponse?> = _newsBySymbol.asStateFlow()

    private val _isSymbolNewsLoading = MutableStateFlow(false)
    val isSymbolNewsLoading: StateFlow<Boolean> = _isSymbolNewsLoading.asStateFlow()

    private val _currentSearchSymbol = MutableStateFlow<String?>(null)
    val currentSearchSymbol: StateFlow<String?> = _currentSearchSymbol.asStateFlow()

    init {
        loadData()
        fetchMarketRegime()
        fetchMarketRegimeHistory()
        fetchAnomalies()
        fetchNarrative()
        //fetchNews()
        fetchAllNews()
        hasMoreNews
    }

    /**
     * Load tất cả data từ SharedPreferences
     */
    private fun loadData() {
        viewModelScope.launch {
            _totalBalance.value = prefsManager.getTotalBalance()
            _initialBalance.value = prefsManager.getInitialBalance()

            val savedTransactions = prefsManager.getTransactions()
            _transactions.clear()
            _transactions.addAll(savedTransactions)

            // ✅ Load categories
            val savedCategories = prefsManager.getCategories()
            _categories.clear()
            if (savedCategories.isEmpty()) {
                // Nếu chưa có, dùng mặc định
                _categories.addAll(APP_CATEGORIES)
            } else {
                _categories.addAll(savedCategories)
            }
            _isDarkMode.value = prefsManager.getDarkMode()
        }
    }

    // ✅ Hàm gọi API
    fun fetchMarketRegime() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getMarketRegime()
                if (response.isSuccessful) {
                    _marketRegime.value = response.body()
                } else {
                    Log.e("FinanceViewModel", "API Error: ${response.code()}")
                    _marketRegime.value = null
                }
            } catch (e: Exception) {
                Log.e("FinanceViewModel", "Network Exception", e)
                _marketRegime.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ✅ NEW: Hàm gọi API history (7 ngày gần nhất)
    fun fetchMarketRegimeHistory(days: Int = 90) {
        viewModelScope.launch {
            _isHistoryLoading.value = true
            try {
                val response = apiService.getMarketRegimeHistory(days)
                if (response.isSuccessful) {
                    _marketRegimeHistory.value = response.body()
                    Log.d("FinanceViewModel", "History loaded: ${response.body()?.history?.size} items")
                } else {
                    Log.e("FinanceViewModel", "History API Error: ${response.code()}")
                    _marketRegimeHistory.value = null
                }
            } catch (e: Exception) {
                Log.e("FinanceViewModel", "History Network Exception", e)
                _marketRegimeHistory.value = null
            } finally {
                _isHistoryLoading.value = false
            }
        }
    }

    /**
     * Gọi API anomalies
     * @param targetDate Format "yyyy-MM-dd" (VD: "2026-02-13")
     * Nếu null, API sẽ trả về ngày gần nhất
     */
    fun fetchAnomalies(targetDate: String? = null) {
        viewModelScope.launch {
            _isAnomaliesLoading.value = true
            _selectedAnomalyDate.value = targetDate

            try {
                val response = apiService.getAnomalies(targetDate)
                if (response.isSuccessful) {
                    val data = response.body()
                    _anomalies.value = data

                    if (data == null || data.anomalies.isEmpty()) {
                        Log.w("FinanceViewModel", "No anomalies data for date: $targetDate")
                    } else {
                        Log.d("FinanceViewModel", "Anomalies loaded: ${data.anomalies.size} items for date: ${data.date}")
                    }
                } else {
                    Log.e("FinanceViewModel", "Anomalies API Error: ${response.code()}")
                    _anomalies.value = null
                }
            } catch (e: Exception) {
                Log.e("FinanceViewModel", "Anomalies Network Exception", e)
                _anomalies.value = null
            } finally {
                _isAnomaliesLoading.value = false
            }
        }
    }

    /**
     * ✅ Gọi API Nhận định AI
     */
//    fun fetchNarrative(targetDate: String? = null) {
//        viewModelScope.launch {
//            _isNarrativeLoading.value = true
//            _selectedNarrativeDate.value = targetDate
//            try {
//                val response = apiService.getNarrative(targetDate)
//                if (response.isSuccessful) {
//                    _narrative.value = response.body()
//                } else {
//                    Log.e("FinanceViewModel", "Narrative API Error: ${response.code()}")
//                    _narrative.value = null
//                }
//            } catch (e: Exception) {
//                Log.e("FinanceViewModel", "Narrative Network Exception", e)
//                _narrative.value = null
//            } finally {
//                _isNarrativeLoading.value = false
//            }
//        }
//    }

    fun fetchNarrative(targetDate: String? = null) {
        viewModelScope.launch {
            _isNarrativeLoading.value = true
            _selectedNarrativeDate.value = targetDate

            try {
                val response = apiService.getNarrative(targetDate)
                if (response.isSuccessful) {
                    val data = response.body()
                    _narrative.value = data

                    if (data != null) {
                        Log.d("FinanceViewModel", "Narrative loaded for date: ${data.date}")

                        // ✅ TỰ ĐỘNG gọi news với cùng ngày
                        fetchNews(data.date)
                    } else {
                        Log.w("FinanceViewModel", "No narrative data for date: $targetDate")
                        _news.value = null
                    }
                } else {
                    Log.e("FinanceViewModel", "Narrative API Error: ${response.code()}")
                    _narrative.value = null
                    _news.value = null
                }
            } catch (e: Exception) {
                Log.e("FinanceViewModel", "Narrative Network Exception", e)
                _narrative.value = null
                _news.value = null
            } finally {
                _isNarrativeLoading.value = false
            }
        }
    }

    /**
     * ✅ Gọi API tin tức
     */
//    fun fetchNews(targetDate: String? = null) {
//        viewModelScope.launch {
//            _isNewsLoading.value = true
//            _selectedNewsDate.value = targetDate
//            try {
//                val response = apiService.getNews(targetDate)
//                if (response.isSuccessful) {
//                    _news.value = response.body()
//                    Log.d("FinanceViewModel", "News loaded: ${response.body()?.news?.size} articles")
//                } else {
//                    Log.e("FinanceViewModel", "News API Error: ${response.code()}")
//                    _news.value = null
//                }
//            } catch (e: Exception) {
//                Log.e("FinanceViewModel", "News Network Exception", e)
//                _news.value = null
//            } finally {
//                _isNewsLoading.value = false
//            }
//        }
//    }
    /**
     * ✅ UPDATED: Gọi API news (được gọi tự động từ fetchNarrative)
     */
    fun fetchNews(targetDate: String? = null) {
        viewModelScope.launch {
            _isNewsLoading.value = true
            _selectedNewsDate.value = targetDate

            try {
                val response = apiService.getNews(targetDate)
                if (response.isSuccessful) {
                    val data = response.body()
                    _news.value = data

                    if (data == null || data.news.isEmpty()) {
                        Log.w("FinanceViewModel", "No news data for date: $targetDate")
                    } else {
                        Log.d("FinanceViewModel", "News loaded: ${data.news.size} items for date: ${data.date}")
                    }
                } else {
                    Log.e("FinanceViewModel", "News API Error: ${response.code()}")
                    _news.value = null
                }
            } catch (e: Exception) {
                Log.e("FinanceViewModel", "News Network Exception", e)
                _news.value = null
            } finally {
                _isNewsLoading.value = false
            }
        }
    }

    /**
     * ✅ NEW: Fetch all news từ offset 1000 về 0
     * Load tất cả tin tức và loại bỏ duplicate
     */
    fun fetchAllNews() {
        viewModelScope.launch {
            _isAllNewsLoading.value = true
            _allNews.value = emptyList()

            val allArticles = mutableListOf<NewsArticle>()
            val seenIds = mutableSetOf<String>()

            // ✅ Bắt đầu từ 0 (Tin mới nhất)
            var currentOffset = 0
            val maxOffset = 250 // Giới hạn số lượng tin muốn lấy

            while (currentOffset <= maxOffset) {
                try {
                    Log.d("FinanceViewModel1", "Fetching news at offset: $currentOffset")

                    val response = apiService.getAllNews(
                        limit = 100,
                        offset = currentOffset
                    )

                    if (response.isSuccessful) {
                        val data = response.body()

                        if (data != null && data.news.isNotEmpty()) {
                            // Lọc duplicate
                            data.news.forEach { article ->
                                val uniqueId = article.getUniqueId()
                                if (!seenIds.contains(uniqueId)) {
                                    seenIds.add(uniqueId)
                                    // Add thẳng vào mảng vì data đã xếp sẵn từ mới -> cũ
                                    allArticles.add(article)
                                }
                            }

                            // Không cần sort nữa, gán thẳng cho State
                            _allNews.value = allArticles.toList()

                            Log.d("FinanceViewModel1", "Loaded ${data.news.size} articles, total unique: ${allArticles.size}")
                        } else {
                            // ✅ NẾU HẾT TIN: API trả về rỗng -> Dừng luôn vòng lặp, không cần chạy tiếp lên 250
                            Log.d("FinanceViewModel1", "Hết dữ liệu ở offset $currentOffset. Dừng tải.")
                            break
                        }
                    } else {
                        Log.e("FinanceViewModel1", "News API Error at offset $currentOffset: ${response.code()}")
                    }

                } catch (e: Exception) {
                    Log.e("FinanceViewModel1", "Lỗi mạng tại offset $currentOffset: ${e.message}")
                }

                // ✅ Cộng thêm 100 để lấy trang tiếp theo (cũ hơn)
                currentOffset += 2

                // Nghỉ 0.5s để không bị block IP
                delay(500)
            }

            Log.d("FinanceViewModel1", "Finished loading all news. Total unique articles: ${allArticles.size}")
            _hasMoreNews.value = false
            _isAllNewsLoading.value = false
        }
    }

    /**
     * ✅ NEW: Load more news (pagination)
     * Dùng khi user scroll đến cuối danh sách
     */
    fun loadMoreNews(currentOffset: Int) {
        viewModelScope.launch {
            if (_isAllNewsLoading.value || !_hasMoreNews.value) return@launch

            _isAllNewsLoading.value = true

            try {
                val response = apiService.getAllNews(
                    limit = 100,
                    offset = currentOffset
                )

                if (response.isSuccessful) {
                    val data = response.body()

                    if (data != null && data.news.isNotEmpty()) {
                        val currentList = _allNews.value.toMutableList()
                        val seenIds = currentList.map { it.getUniqueId() }.toSet()

                        // Thêm tin mới, loại bỏ duplicate
                        data.news.forEach { article ->
                            val uniqueId = article.getUniqueId()
                            if (!seenIds.contains(uniqueId)) {
                                currentList.add(article)
                            }
                        }

                        _allNews.value = currentList
                    } else {
                        _hasMoreNews.value = false
                    }
                }
            } catch (e: Exception) {
                Log.e("FinanceViewModel", "Error loading more news", e)
            } finally {
                _isAllNewsLoading.value = false
            }
        }
    }

    /**
     * ✅ Gọi API tin tức theo sector
     * @param sector Tên sector (VD: "Ngân hàng", "Công nghệ")
     */
    fun fetchNewsBySector(sector: String) {
        // Nếu sector trống, không làm gì
        if (sector.isBlank()) {
            _newsBySector.value = null
            _currentSearchSector.value = null
            return
        }

        viewModelScope.launch {
            _isSectorNewsLoading.value = true
            _currentSearchSector.value = sector

            try {
                // XÓA dòng encode thủ công này
                // val encodedSector = java.net.URLEncoder.encode(sector.trim(), "UTF-8")

                // Truyền trực tiếp chuỗi gốc đã trim khoảng trắng thừa
                val response = apiService.getNewsBySector(sector.trim())

                if (response.isSuccessful) {
                    val data = response.body()
                    _newsBySector.value = data

                    Log.d("FinanceViewModel", "Sector news loaded: ${data?.news?.size} articles for sector: ${data?.sector}")
                } else {
                    Log.e("FinanceViewModel", "Sector news API Error: ${response.code()}")
                    _newsBySector.value = null
                }
            } catch (e: Exception) {
                Log.e("FinanceViewModel", "Sector news Network Exception", e)
                _newsBySector.value = null
            } finally {
                _isSectorNewsLoading.value = false
            }
        }
    }

    /**
     * Gọi API tin tức theo mã cổ phiếu
     * @param symbol Mã cổ phiếu (VD: "BSR")
     */
    fun fetchNewsBySymbol(symbol: String) {
        if (symbol.isBlank()) {
            clearSymbolSearch()
            return
        }

        viewModelScope.launch {
            _isSymbolNewsLoading.value = true
            _currentSearchSymbol.value = symbol.uppercase() // Chuẩn hóa mã cổ phiếu thành chữ hoa

            try {
                // Truyền symbol đã bỏ khoảng trắng
                val response = apiService.getNewsBySymbol(symbol.trim())

                if (response.isSuccessful) {
                    val data = response.body()
                    _newsBySymbol.value = data
                    Log.d("FinanceViewModel", "Symbol news loaded: ${data?.news?.size} articles for ${data?.symbol}")
                } else {
                    Log.e("FinanceViewModel", "Symbol news API Error: ${response.code()}")
                    _newsBySymbol.value = null
                }
            } catch (e: Exception) {
                Log.e("FinanceViewModel", "Symbol news Network Exception", e)
                _newsBySymbol.value = null
            } finally {
                _isSymbolNewsLoading.value = false
            }
        }
    }

    /**
     * Xóa kết quả tìm kiếm theo mã cổ phiếu
     */
    fun clearSymbolSearch() {
        _newsBySymbol.value = null
        _currentSearchSymbol.value = null
    }

    /**
     * ✅ Clear sector search
     */
    fun clearSectorSearch() {
        _newsBySector.value = null
        _currentSearchSector.value = null
    }

    /**
     * ✅ NEW: Reset all news state
     */
    fun resetAllNews() {
        _allNews.value = emptyList()
        _hasMoreNews.value = true
    }

    /**
     * ✅ NEW: Reset về ngày hiện tại (không có targetDate)
     */
    fun resetAnomaliesDate() {
        fetchAnomalies(null)
    }

    fun resetNarrativeDate() {
        fetchNarrative(null)
    }

    fun resetNewsDate() {
        fetchNews(null)
    }

    // ==================== CATEGORY MANAGEMENT ====================

    /**
     * Thêm category mới
     */
    fun addCategory(category: Category) {
        _categories.add(category)
        saveCategories()
    }

    /**
     * Cập nhật category
     */
    fun updateCategory(oldCategory: Category, newCategory: Category) {
        val index = _categories.indexOf(oldCategory)
        if (index != -1) {
            _categories[index] = newCategory
            saveCategories()
        }
    }

    /**
     * Xóa category
     */
    fun removeCategory(category: Category) {
        _categories.remove(category)
        saveCategories()
    }

    /**
     * Lưu categories vào SharedPreferences
     */
    private fun saveCategories() {
        prefsManager.saveCategories(_categories.toList())
    }

    /**
     * Tìm category theo tên
     */
    fun findCategoryByName(name: String): Category? {
        return _categories.find { it.name.equals(name, ignoreCase = true) }
    }

    // ==================== TRANSACTION MANAGEMENT ====================

    fun updateTotalBalance(newBalance: Double) {
        _totalBalance.value = newBalance
        prefsManager.saveTotalBalance(newBalance)
    }

    fun updateInitialBalance(newBalance: Double) {
        _initialBalance.value = newBalance
        prefsManager.saveInitialBalance(newBalance)
    }

    fun addTransaction(transaction: Transaction) {
        _transactions.add(0, transaction)
        saveTransactions()

        val newBalance = if (transaction.isIncome) {
            _totalBalance.value + transaction.amount
        } else {
            _totalBalance.value - transaction.amount
        }
        updateTotalBalance(newBalance)

        if (transaction.isIncome && _initialBalance.value == 0.0) {
            updateInitialBalance(transaction.amount)
        }
    }

    fun removeTransaction(transaction: Transaction) {
        _transactions.remove(transaction)
        saveTransactions()

        val newBalance = if (transaction.isIncome) {
            _totalBalance.value - transaction.amount
        } else {
            _totalBalance.value + transaction.amount
        }
        updateTotalBalance(newBalance)
    }

    fun updateTransaction(oldTransaction: Transaction, newTransaction: Transaction) {
        val index = _transactions.indexOf(oldTransaction)
        if (index != -1) {
            _transactions[index] = newTransaction
            saveTransactions()
            recalculateBalance()
        }
    }

    private fun saveTransactions() {
        prefsManager.saveTransactions(_transactions.toList())
    }

    private fun recalculateBalance() {
        val income = _transactions.filter { it.isIncome }.sumOf { it.amount }
        val expense = _transactions.filter { !it.isIncome }.sumOf { it.amount }
        val newBalance = income - expense

        updateTotalBalance(newBalance)

        if (income > 0 && _initialBalance.value == 0.0) {
            updateInitialBalance(income)
        }
    }

    fun clearAllData() {
        _transactions.clear()
        _totalBalance.value = 0.0
        _initialBalance.value = 0.0
        _categories.clear()
        _categories.addAll(APP_CATEGORIES)
        prefsManager.clearAll()
    }

    fun getTotalIncome(): Double {
        return _transactions.filter { it.isIncome }.sumOf { it.amount }
    }

    fun getTotalExpense(): Double {
        return _transactions.filter { !it.isIncome }.sumOf { it.amount }
    }

    fun getTransactionsByDate(date: String): List<Transaction> {
        return _transactions.filter { it.date == date }
    }

    fun searchTransactions(query: String): List<Transaction> {
        return _transactions.filter {
            it.title.contains(query, ignoreCase = true)
        }
    }

    fun getMonthlyExpense(): Double {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        return _transactions
            .filter { !it.isIncome }
            .filter { transaction ->
                val transactionDate = parseTransactionDate(transaction.date)
                transactionDate?.let {
                    it.get(Calendar.MONTH) == currentMonth &&
                            it.get(Calendar.YEAR) == currentYear
                } ?: false
            }
            .sumOf { it.amount }
    }

    fun getLastMonthExpense(): Double {
        val calendar = Calendar.getInstance()
        val lastMonth = if (calendar.get(Calendar.MONTH) == 0) 11 else calendar.get(Calendar.MONTH) - 1
        val lastMonthYear = if (calendar.get(Calendar.MONTH) == 0) {
            calendar.get(Calendar.YEAR) - 1
        } else {
            calendar.get(Calendar.YEAR)
        }

        return _transactions
            .filter { !it.isIncome }
            .filter { transaction ->
                val transactionDate = parseTransactionDate(transaction.date)
                transactionDate?.let {
                    it.get(Calendar.MONTH) == lastMonth &&
                            it.get(Calendar.YEAR) == lastMonthYear
                } ?: false
            }
            .sumOf { it.amount }
    }

    fun getWeeklyExpense(): Map<Int, Double> {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        val lastDay = Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        }.get(Calendar.DAY_OF_MONTH)

        val daysPerWeek = lastDay / 4

        val weeklyExpense = mutableMapOf<Int, Double>()

        for (week in 1..4) {
            val weekStart = (week - 1) * daysPerWeek + 1
            val weekEnd = if (week == 4) lastDay else week * daysPerWeek

            val weekTotal = _transactions
                .filter { !it.isIncome }
                .filter { transaction ->
                    val transactionDate = parseTransactionDate(transaction.date)
                    transactionDate?.let { cal ->
                        cal.get(Calendar.MONTH) == currentMonth &&
                                cal.get(Calendar.YEAR) == currentYear &&
                                cal.get(Calendar.DAY_OF_MONTH) in weekStart..weekEnd
                    } ?: false
                }
                .sumOf { it.amount }

            weeklyExpense[week] = weekTotal
        }

        return weeklyExpense
    }

    private fun parseTransactionDate(dateString: String): Calendar? {
        if (dateString == "Hôm nay") {
            return Calendar.getInstance()
        }

        val formats = listOf("dd/MM/yyyy", "dd 'Th'MM")

        for (pattern in formats) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale("vi", "VN"))
                val date = sdf.parse(dateString)
                if (date != null) {
                    return Calendar.getInstance().apply { time = date }
                }
            } catch (e: Exception) {
                continue
            }
        }

        return null
    }

    private fun parseTransactionTime(timeString: String): Pair<Int, Int>? {
        return try {
            val parts = timeString.split(":")
            if (parts.size == 2) {
                val hour = parts[0].toInt()
                val minute = parts[1].toInt()
                Pair(hour, minute)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun getTransactionCalendar(transaction: Transaction): Calendar? {
        val dateCal = parseTransactionDate(transaction.date) ?: return null
        val time = parseTransactionTime(transaction.time) ?: return null

        return dateCal.apply {
            set(Calendar.HOUR_OF_DAY, time.first)
            set(Calendar.MINUTE, time.second)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    fun getSortedTransactions(): List<Transaction> {
        return _transactions.sortedByDescending { transaction ->
            getTransactionCalendar(transaction)?.timeInMillis ?: 0L
        }
    }

    // ==================== DARK MODE MANAGEMENT ====================

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
        prefsManager.saveDarkMode(_isDarkMode.value)
    }

    fun setDarkMode(isDark: Boolean) {
        _isDarkMode.value = isDark
        prefsManager.saveDarkMode(isDark)
    }
}