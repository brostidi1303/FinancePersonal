package com.example.financeapp.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.financeapp.R
import com.google.gson.annotations.SerializedName

data class Transaction(
    val id: Int,
    val title: String,
    val date: String,        // Format: "dd/MM/yyyy" hoặc "Hôm nay"
    val time: String,        // Format: "HH:mm"
    val amount: Double,
    val icon: Int,
    val iconColor: Color,
    val isIncome: Boolean,
    val note: String = ""
)

data class Category(
    val id: Int,
    val name: String,
    val icon: Int,
    val color: Color
)

data class Wallet(
    val id: Int,
    val name: String,
    val balance: Double
)

val APP_CATEGORIES = listOf(
    Category(1, "Ăn uống", R.drawable.restaurant, Color(0xFF5B7FFF)), // Xanh dương
    Category(2, "Cà phê", R.drawable.coffee, Color(0xFF795548)),    // Nâu
    Category(3, "Mua sắm", R.drawable.grocery_store, Color(0xFFE91E63)), // Hồng
    Category(4, "Di chuyển", R.drawable.plane, Color(0xFF00C48C)),   // Xanh lá
    Category(5, "Hóa đơn", R.drawable.invoice, Color(0xFFFF9800)),   // Cam
    Category(6, "Giải trí", R.drawable.theater, Color(0xFF9C27B0)),  // Tím
    Category(7, "Y tế", R.drawable.health_care, Color(0xFFF44336)),  // Đỏ
    Category(8, "Khác", R.drawable.application, Color(0xFF607D8B))   // Xám xanh
)

// Tạo file RegimeResponse.kt hoặc bỏ vào package model

data class RegimeResponse(
    val date: String,
    val regime: String,
    val score: Double,
    val description: String,
    val components: RegimeComponents,
    @SerializedName("is_trading_day") val isTradingDay: Boolean,
    @SerializedName("trading_note") val tradingNote: String?
)

data class RegimeComponents(
    val trend: Double,
    val breadth: Double,
    val foreign: Double,
    val volatility: Double
)

// ✅ NEW: History Response
data class RegimeHistoryResponse(
    val days: Int,
    val history: List<RegimeHistoryItem>,
    val transitions: Int
)

data class RegimeHistoryItem(
    val date: String,        // Format: "2026-02-09"
    val regime: String,
    val score: Double
)

// ==================== ANOMALIES DATA MODELS ====================

/**
 * Response từ API /anomalies
 */
data class AnomaliesResponse(
    val date: String,
    val anomalies: List<Anomaly>
)

/**
 * Một bất thường (anomaly) trong thị trường
 */
data class Anomaly(
    val type: String,           // VD: "BREAKOUT_SETUP"
    val symbol: String,         // Mã cổ phiếu: VD: "MPC", "SHP"
    val headline: String,       // Tiêu đề: VD: "MPC phá vỡ đỉnh 20 ngày..."
    val details: AnomalyDetails,
    val confidence: Double      // Độ tin cậy: 0.0 -> 1.0
)

/**
 * Chi tiết của anomaly
 */
data class AnomalyDetails(
    val target: Double?,            // Mục tiêu giá
    @SerializedName("stop_loss")
    val stopLoss: Double?,          // Giá cắt lỗ
    @SerializedName("entry_zone")
    val entryZone: List<Double>?,   // Vùng giá vào lệnh [min, max]
    @SerializedName("volume_ratio")
    val volumeRatio: Double?,       // Tỷ lệ khối lượng
    @SerializedName("previous_high")
    val previousHigh: Double?,      // Đỉnh trước đó
    @SerializedName("breakout_price")
    val breakoutPrice: Double?      // Giá phá vỡ
)

data class NarrativeResponse(
    val confidence: String,
    val date: String,
    val narrative: String,
    @SerializedName("supporting_articles")
    val supportingArticles: List<Int>
)