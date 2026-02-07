package com.example.financeapp.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import com.example.financeapp.data.Transaction
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

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

    // Lưu danh sách transactions - TRỰC TIẾP với Transaction class
    fun saveTransactions(transactions: List<Transaction>) {
        val json = gson.toJson(transactions)
        prefs.edit().putString(KEY_TRANSACTIONS, json).apply()
    }

    // Lấy danh sách transactions - TRỰC TIẾP với Transaction class
    fun getTransactions(): List<Transaction> {
        val json = prefs.getString(KEY_TRANSACTIONS, null) ?: return emptyList()
        val type = object : TypeToken<List<Transaction>>() {}.type
        return gson.fromJson(json, type)
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