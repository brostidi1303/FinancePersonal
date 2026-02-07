package com.example.financeapp.viewModel

import android.app.Application
import android.icu.text.SimpleDateFormat
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.financeapp.PreferencesManager
import com.example.financeapp.data.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsManager = PreferencesManager(application)

    // State cho balance
    private val _totalBalance = MutableStateFlow(0.0)
    val totalBalance: StateFlow<Double> = _totalBalance.asStateFlow()

    private val _initialBalance = MutableStateFlow(0.0)
    val initialBalance: StateFlow<Double> = _initialBalance.asStateFlow()

    // State cho transactions - dùng SnapshotStateList để tương thích với Compose
    private val _transactions = mutableStateListOf<Transaction>()
    val transactions: SnapshotStateList<Transaction> = _transactions

    init {
        // Load data từ SharedPreferences khi ViewModel được tạo
        loadData()
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
        }
    }

    /**
     * Cập nhật total balance
     */
    fun updateTotalBalance(newBalance: Double) {
        _totalBalance.value = newBalance
        prefsManager.saveTotalBalance(newBalance)
    }

    /**
     * Cập nhật initial balance
     */
    fun updateInitialBalance(newBalance: Double) {
        _initialBalance.value = newBalance
        prefsManager.saveInitialBalance(newBalance)
    }

    /**
     * Thêm transaction mới
     */
    fun addTransaction(transaction: Transaction) {
        _transactions.add(0, transaction) // Thêm vào đầu danh sách
        saveTransactions()

        // Tự động cập nhật balance
        val newBalance = if (transaction.isIncome) {
            _totalBalance.value + transaction.amount
        } else {
            _totalBalance.value - transaction.amount
        }
        updateTotalBalance(newBalance)

        // Nếu là thu nhập đầu tiên và chưa có initial balance
        if (transaction.isIncome && _initialBalance.value == 0.0) {
            updateInitialBalance(transaction.amount)
        }
    }

    /**
     * Xóa transaction
     */
    fun removeTransaction(transaction: Transaction) {
        _transactions.remove(transaction)
        saveTransactions()

        // Cập nhật lại balance
        val newBalance = if (transaction.isIncome) {
            _totalBalance.value - transaction.amount
        } else {
            _totalBalance.value + transaction.amount
        }
        updateTotalBalance(newBalance)
    }

    /**
     * Cập nhật transaction
     */
    fun updateTransaction(oldTransaction: Transaction, newTransaction: Transaction) {
        val index = _transactions.indexOf(oldTransaction)
        if (index != -1) {
            _transactions[index] = newTransaction
            saveTransactions()

            // Tính toán lại balance
            recalculateBalance()
        }
    }

    /**
     * Lưu transactions vào SharedPreferences
     */
    private fun saveTransactions() {
        prefsManager.saveTransactions(_transactions.toList())
    }

    /**
     * Tính toán lại toàn bộ balance từ transactions
     */
    private fun recalculateBalance() {
        val income = _transactions.filter { it.isIncome }.sumOf { it.amount }
        val expense = _transactions.filter { !it.isIncome }.sumOf { it.amount }
        val newBalance = income - expense

        updateTotalBalance(newBalance)

        if (income > 0 && _initialBalance.value == 0.0) {
            updateInitialBalance(income)
        }
    }

    /**
     * Clear tất cả data
     */
    fun clearAllData() {
        _transactions.clear()
        _totalBalance.value = 0.0
        _initialBalance.value = 0.0
        prefsManager.clearAll()
    }

    /**
     * Tính tổng thu nhập
     */
    fun getTotalIncome(): Double {
        return _transactions.filter { it.isIncome }.sumOf { it.amount }
    }

    /**
     * Tính tổng chi tiêu
     */
    fun getTotalExpense(): Double {
        return _transactions.filter { !it.isIncome }.sumOf { it.amount }
    }

    /**
     * Lấy transactions theo ngày
     */
    fun getTransactionsByDate(date: String): List<Transaction> {
        return _transactions.filter { it.date == date }
    }

    /**
     * Tìm kiếm transactions
     */
    fun searchTransactions(query: String): List<Transaction> {
        return _transactions.filter {
            it.title.contains(query, ignoreCase = true)
        }
    }

    /**
     * Tính tổng chi tiêu trong tháng hiện tại
     */
    fun getMonthlyExpense(): Double {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        return _transactions
            .filter { !it.isIncome }  // Chỉ lấy expense
            .filter { transaction ->
                val transactionDate = parseTransactionDate(transaction.date)
                transactionDate?.let {
                    it.get(Calendar.MONTH) == currentMonth &&
                            it.get(Calendar.YEAR) == currentYear
                } ?: false
            }
            .sumOf { it.amount }
    }

    /**
     * Tính tổng chi tiêu tháng trước
     */
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

    /**
     * Lấy chi tiêu theo tuần trong tháng hiện tại
     * @return Map<Int, Double> - Key: số tuần (1-4), Value: tổng chi tiêu
     */
    fun getWeeklyExpense(): Map<Int, Double> {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        // Lấy số ngày trong tháng
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

    /**
     * Parse date string từ transaction
     * Hỗ trợ nhiều format:
     * - "Hôm nay, HH:mm"
     * - "dd/MM/yyyy, HH:mm"
     * - "dd ThMM, HH:mm"
     */
    private fun parseTransactionDate(dateString: String): Calendar? {
        // Case 1: "Hôm nay, HH:mm"
        if (dateString.startsWith("Hôm nay")) {
            return Calendar.getInstance()
        }

        // Case 2: "dd/MM/yyyy, HH:mm" hoặc "dd/MM/yyyy"
        val formats = listOf(
            "dd/MM/yyyy, HH:mm",
            "dd/MM/yyyy",
            "dd 'Th'MM, HH:mm"
        )

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

    /**
     * Lấy transactions trong khoảng thời gian
     */
    fun getTransactionsBetween(startDate: Calendar, endDate: Calendar): List<Transaction> {
        return _transactions.filter { transaction ->
            val transactionDate = parseTransactionDate(transaction.date)
            transactionDate?.let {
                it.timeInMillis >= startDate.timeInMillis &&
                        it.timeInMillis <= endDate.timeInMillis
            } ?: false
        }
    }

    /**
     * Lấy chi tiêu theo category trong tháng hiện tại
     */
    fun getMonthlyExpenseByCategory(): Map<String, Double> {
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
            .groupBy { it.title }  // Group by category name
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }

}