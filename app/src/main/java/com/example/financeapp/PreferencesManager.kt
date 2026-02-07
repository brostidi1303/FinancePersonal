package com.example.financeapp

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.financeapp.data.Transaction
import com.example.financeapp.home.GreenPositive
import com.example.financeapp.home.RedNegative
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("finance_app_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_TOTAL_BALANCE = "total_balance"
        private const val KEY_INITIAL_BALANCE = "initial_balance"
        private const val KEY_TRANSACTIONS = "transactions"
    }

    // Lưu và lấy Total Balance
    fun saveTotalBalance(balance: Double) {
        prefs.edit().putFloat(KEY_TOTAL_BALANCE, balance.toFloat()).apply()
    }

    fun getTotalBalance(): Double {
        return prefs.getFloat(KEY_TOTAL_BALANCE, 0f).toDouble()
    }

    // Lưu và lấy Initial Balance
    fun saveInitialBalance(balance: Double) {
        prefs.edit().putFloat(KEY_INITIAL_BALANCE, balance.toFloat()).apply()
    }

    fun getInitialBalance(): Double {
        return prefs.getFloat(KEY_INITIAL_BALANCE, 0f).toDouble()
    }

    // Lưu danh sách transactions
    fun saveTransactions(transactions: List<Transaction>) {
        val transactionDataList = transactions.map { it.toTransactionData() }
        val json = gson.toJson(transactionDataList)
        prefs.edit().putString(KEY_TRANSACTIONS, json).apply()
    }

    // Lấy danh sách transactions
    fun getTransactions(): List<Transaction> {
        val json = prefs.getString(KEY_TRANSACTIONS, null) ?: return emptyList()
        val type = object : TypeToken<List<TransactionData>>() {}.type
        val transactionDataList: List<TransactionData> = gson.fromJson(json, type)
        return transactionDataList.map { it.toTransaction() }
    }

    // Clear all data
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}

// Data class để serialize/deserialize (vì ImageVector và Color không thể lưu trực tiếp)
data class TransactionData(
    val id: Int,
    val title: String,
    val date: String,
    val amount: Double,
    val iconName: String, // Lưu tên icon thay vì ImageVector
    val iconColorHex: String, // Lưu màu dưới dạng hex string
    val isIncome: Boolean
)

// Extension functions để chuyển đổi giữa Transaction và TransactionData
fun Transaction.toTransactionData(): TransactionData {
    return TransactionData(
        id = this.id,
        title = this.title,
        date = this.date,
        amount = this.amount,
        iconName = when (this.icon) {
            Icons.Default.Add -> "Add"
            Icons.Default.ShoppingCart -> "ShoppingCart"
            else -> "Default"
        },
        iconColorHex = String.format("#%08X", this.iconColor.value.toInt()),
        isIncome = this.isIncome
    )
}

fun TransactionData.toTransaction(): Transaction {
    return Transaction(
        id = this.id,
        title = this.title,
        date = this.date,
        amount = this.amount,
        icon = when (this.iconName) {
            "Add" -> Icons.Default.Add
            "ShoppingCart" -> Icons.Default.ShoppingCart
            else -> Icons.Default.Add
        },
        iconColor = try {
            Color(android.graphics.Color.parseColor(this.iconColorHex))
        } catch (e: Exception) {
            if (this.isIncome) GreenPositive else RedNegative
        },
        isIncome = this.isIncome
    )
}