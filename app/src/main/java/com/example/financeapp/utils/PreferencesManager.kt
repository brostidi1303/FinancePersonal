package com.example.financeapp.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import com.example.financeapp.data.Category
import com.example.financeapp.data.Transaction
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import kotlin.apply

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("finance_app_prefs", Context.MODE_PRIVATE)

    // Gson với custom TypeAdapter cho Color
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(Color::class.java, ColorTypeAdapter())
        .create()

    companion object {
        private const val KEY_TOTAL_BALANCE = "total_balance"
        private const val KEY_INITIAL_BALANCE = "initial_balance"
        private const val KEY_TRANSACTIONS = "transactions"
        private const val KEY_CATEGORIES = "categories" // ✅ Thêm key cho categories
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
        val json = gson.toJson(transactions)
        prefs.edit().putString(KEY_TRANSACTIONS, json).apply()
    }

    // Lấy danh sách transactions
    fun getTransactions(): List<Transaction> {
        val json = prefs.getString(KEY_TRANSACTIONS, null) ?: return emptyList()
        val type = object : TypeToken<List<Transaction>>() {}.type
        return gson.fromJson(json, type)
    }

    // ✅ Lưu danh sách categories
    fun saveCategories(categories: List<Category>) {
        val json = gson.toJson(categories)
        prefs.edit().putString(KEY_CATEGORIES, json).apply()
    }

    // ✅ Lấy danh sách categories
    fun getCategories(): List<Category> {
        val json = prefs.getString(KEY_CATEGORIES, null) ?: return emptyList()
        val type = object : TypeToken<List<Category>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ✅ THÊM 2 hàm này vào PreferencesManager
    fun saveDarkMode(isDark: Boolean) {
        prefs.edit().putBoolean("dark_mode", isDark).apply()
    }

    fun getDarkMode(): Boolean {
        return prefs.getBoolean("dark_mode", true) // default: dark
    }

    // Clear all data
    fun clearAll() {
        prefs.edit().clear().apply()
    }
}

// Custom TypeAdapter để serialize/deserialize Color
class ColorTypeAdapter : TypeAdapter<Color>() {
    override fun write(out: JsonWriter, value: Color?) {
        if (value == null) {
            out.nullValue()
        } else {
            // Lưu Color dưới dạng hex string
            out.value(String.format("#%08X", value.value.toInt()))
        }
    }

    override fun read(`in`: JsonReader): Color {
        val colorString = `in`.nextString()
        return try {
            Color(android.graphics.Color.parseColor(colorString))
        } catch (e: Exception) {
            Color.Gray // Default color nếu parse lỗi
        }
    }
}