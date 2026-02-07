package com.example.financeapp.viewModel

import android.app.Application
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
}