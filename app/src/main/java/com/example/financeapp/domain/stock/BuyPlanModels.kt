package com.example.financeapp.domain.stock

/** Khung thời gian đánh giá. */
enum class Horizon(val days: Int, val label: String) {
    SHORT(3, "3 ngày"),
    MEDIUM(7, "7 ngày")
}

/** Trạng thái chung của thị trường, suy ra từ VNINDEX. */
enum class MarketRegime(val label: String, val multiplier: Double, val note: String) {
    RISK_ON(
        "Thuận lợi", 1.00,
        "VNINDEX trên cả MA20 và MA50 — xu hướng chung ủng hộ việc mở vị thế mua."
    ),
    NEUTRAL(
        "Trung tính", 0.90,
        "VNINDEX trên một trong hai đường MA — nên giảm tỷ trọng và chọn lọc kỹ hơn."
    ),
    RISK_OFF(
        "Bất lợi", 0.75,
        "VNINDEX dưới cả MA20 và MA50 — xu hướng chung đang giảm, mọi điểm số đã bị chiết khấu."
    );

    companion object {
        fun from(close: Double, ma20: Double?, ma50: Double?): MarketRegime {
            if (ma20 == null || ma50 == null) return NEUTRAL
            val above20 = close > ma20
            val above50 = close > ma50
            return when {
                above20 && above50 -> RISK_ON
                above20 || above50 -> NEUTRAL
                else -> RISK_OFF
            }
        }
    }
}

/** Bối cảnh thị trường dùng chung cho mọi mã trong một lần quét. */
data class MarketContext(
    val indexClose: Double,
    val indexMa20: Double?,
    val indexMa50: Double?,
    val indexRoc3: Double?,
    val indexRoc7: Double?,
    val regime: MarketRegime
)

/** Một yếu tố đã được chấm điểm, giữ lại để hiển thị minh bạch cho người dùng. */
data class ScoredFactor(
    val key: String,
    val label: String,
    val score: Double,
    val weight: Double,
    val detail: String
) {
    val isStrength: Boolean get() = score >= 70.0
    val isWeakness: Boolean get() = score <= 35.0
}

/** Kế hoạch mua đề xuất cho một mã ở một khung thời gian. */
data class BuyPlan(
    val horizon: Horizon,
    val score: Double,
    val factors: List<ScoredFactor>,
    val entryLow: Double,
    val entryHigh: Double,
    val stopLoss: Double,
    val target: Double
) {
    val strengths: List<String> get() = factors.filter { it.isStrength }.map { it.detail }
    val weaknesses: List<String> get() = factors.filter { it.isWeakness }.map { it.detail }

    /** Tỷ lệ lãi/lỗ kỳ vọng dựa trên vùng vào, cắt lỗ và mục tiêu. */
    val riskReward: Double?
        get() {
            val mid = (entryLow + entryHigh) / 2.0
            val risk = mid - stopLoss
            val reward = target - mid
            return if (risk > 0) reward / risk else null
        }

    /**
     * Nhãn độ tin cậy của MÔ HÌNH, không phải xác suất sinh lời.
     * Thang điểm phản ánh mức độ các chỉ báo đồng thuận với nhau.
     */
    val confidenceLabel: String
        get() = when {
            score >= 75 -> "Đồng thuận cao"
            score >= 60 -> "Đồng thuận khá"
            score >= 45 -> "Trung tính"
            score >= 30 -> "Yếu"
            else -> "Rất yếu"
        }
}

/** Kết quả phân tích đầy đủ của một mã. */
data class StockAnalysis(
    val symbol: String,
    val sector: String,
    val price: Double,           // đồng
    val changePercent: Double,
    val rsi: Double?,
    val ma20: Double?,
    val ma50: Double?,
    val volumeRatio: Double?,
    val roc3: Double?,
    val roc7: Double?,
    val atr: Double?,
    val foreignNetValue: Double,
    val tradedValue: Double,
    val shortPlan: BuyPlan,
    val mediumPlan: BuyPlan
) {
    fun planFor(horizon: Horizon): BuyPlan =
        if (horizon == Horizon.SHORT) shortPlan else mediumPlan
}

/** Điểm trung bình của một ngành. */
data class SectorScore(
    val sector: String,
    val averageScore: Double,
    val symbolCount: Int,
    val topSymbols: List<String>
)

/** Toàn bộ kết quả một lần quét. */
data class MarketScanResult(
    val scannedAt: Long,
    val context: MarketContext,
    val analyses: List<StockAnalysis>,
    val sectorScores: List<SectorScore>,
    val skipped: Map<String, Int>
)
