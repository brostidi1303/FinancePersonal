package com.example.financeapp.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import com.example.financeapp.R
import com.example.financeapp.data.Category
import com.example.financeapp.data.Transaction
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("finance_app_prefs", Context.MODE_PRIVATE)

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(Color::class.java, ColorTypeAdapter())
        .create()

    companion object {
        private const val KEY_TOTAL_BALANCE = "total_balance"
        private const val KEY_INITIAL_BALANCE = "initial_balance"
        private const val KEY_TRANSACTIONS = "transactions"
        private const val KEY_CATEGORIES = "categories"
        private const val KEY_AUTH_TOKEN = "auth_token" // Bổ sung key cho token
    }

    // ✅ Bổ sung hàm lấy Token cho AuthInterceptor
    fun getAuthToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }

    fun saveAuthToken(token: String) {
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    // Các hàm còn lại giữ nguyên như code cũ của bạn
    fun saveTotalBalance(balance: Double) {
        prefs.edit().putFloat(KEY_TOTAL_BALANCE, balance.toFloat()).apply()
    }

    fun getTotalBalance(): Double {
        return prefs.getFloat(KEY_TOTAL_BALANCE, 0f).toDouble()
    }

    fun saveInitialBalance(balance: Double) {
        prefs.edit().putFloat(KEY_INITIAL_BALANCE, balance.toFloat()).apply()
    }

    fun getInitialBalance(): Double {
        return prefs.getFloat(KEY_INITIAL_BALANCE, 0f).toDouble()
    }

    fun saveTransactions(transactions: List<Transaction>) {
        val json = gson.toJson(transactions)
        prefs.edit().putString(KEY_TRANSACTIONS, json).apply()
    }

    fun getTransactions(): List<Transaction> {
        val json = prefs.getString(KEY_TRANSACTIONS, null) ?: return emptyList()
        val type = object : TypeToken<List<Transaction>>() {}.type
        return try {
            val transactions: List<Transaction> = gson.fromJson(json, type)
            transactions.map { transaction ->
                val validIcon = try {
                    context.resources.getResourceName(transaction.icon)
                    transaction.icon
                } catch (e: Exception) {
                    R.drawable.application // Nhớ import R đúng package
                }
                transaction.copy(icon = validIcon)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveCategories(categories: List<Category>) {
        val json = gson.toJson(categories)
        prefs.edit().putString(KEY_CATEGORIES, json).apply()
    }

    fun getCategories(): List<Category> {
        val json = prefs.getString(KEY_CATEGORIES, null) ?: return emptyList()
        val type = object : TypeToken<List<Category>>() {}.type
        return try {
            val categories: List<Category> = gson.fromJson(json, type)
            categories.map { category ->
                val validIcon = try {
                    context.resources.getResourceName(category.icon)
                    category.icon
                } catch (e: Exception) {
                    R.drawable.application
                }
                category.copy(icon = validIcon)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveDarkMode(isDark: Boolean) {
        prefs.edit().putBoolean("dark_mode", isDark).apply()
    }

    fun getDarkMode(): Boolean {
        return prefs.getBoolean("dark_mode", true)
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}

class ColorTypeAdapter : TypeAdapter<Color>() {
    override fun write(out: JsonWriter, value: Color?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(String.format("#%08X", value.value.toInt()))
        }
    }

    override fun read(`in`: JsonReader): Color {
        val colorString = `in`.nextString()
        return try {
            Color(android.graphics.Color.parseColor(colorString))
        } catch (e: Exception) {
            Color.Gray
        }
    }
}