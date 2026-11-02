package com.example.financeapp.data.market

/**
 * Trạng thái phiên giao dịch, đọc từ trường `session` của SSI.
 *
 * Dùng chính dữ liệu server trả về thay vì so giờ máy — nhờ vậy ngày nghỉ lễ
 * hay phiên kéo dài bất thường đều tự xử lý đúng mà không phải cập nhật lịch.
 */
enum class MarketSession(val code: String, val label: String, val isLive: Boolean) {
    PRE_OPEN("P", "Trước giờ mở cửa", false),
    ATO("ATO", "Phiên mở cửa (ATO)", true),
    CONTINUOUS("LO", "Khớp lệnh liên tục", true),
    LUNCH_BREAK("BREAK", "Nghỉ trưa", false),
    ATC("ATC", "Phiên đóng cửa (ATC)", true),
    PUT_THROUGH("PT", "Giao dịch thỏa thuận", false),
    CLOSED("C", "Đã đóng cửa", false),
    UNKNOWN("", "Không rõ trạng thái", false);

    companion object {
        fun from(code: String?): MarketSession {
            val c = code?.trim()?.uppercase() ?: return UNKNOWN
            return entries.firstOrNull { it.code == c } ?: when (c) {
                "O", "OPEN" -> CONTINUOUS
                "I", "INTERMISSION" -> LUNCH_BREAK
                "CLOSE", "CLOSED" -> CLOSED
                else -> UNKNOWN
            }
        }
    }
}

/** Trạng thái giá so với biên độ cho phép trong ngày. */
enum class PriceState { CEILING, FLOOR, REFERENCE, UP, DOWN }

/** Một mã trên bảng giá, dữ liệu trong phiên. Mọi giá theo ĐỒNG. */
data class LiveQuote(
    val symbol: String,
    val sector: String,
    val price: Double,
    val referencePrice: Double,
    val changePercent: Double,
    val openPrice: Double,
    val high: Double,
    val low: Double,
    val ceiling: Double,
    val floor: Double,
    val volume: Double,
    val tradedValue: Double,
    val foreignBuyValue: Double,
    val foreignSellValue: Double
) {
    val changeValue: Double get() = price - referencePrice

    val foreignNetValue: Double get() = foreignBuyValue - foreignSellValue

    /** Vị trí giá hiện tại trong biên độ của chính ngày hôm nay: 0 = đáy, 1 = đỉnh. */
    val dayRangePosition: Double?
        get() = if (high > low) ((price - low) / (high - low)).coerceIn(0.0, 1.0) else null

    val state: PriceState
        get() = when {
            ceiling > 0 && price >= ceiling -> PriceState.CEILING
            floor > 0 && price <= floor -> PriceState.FLOOR
            price > referencePrice -> PriceState.UP
            price < referencePrice -> PriceState.DOWN
            else -> PriceState.REFERENCE
        }
}

/** Toàn bộ bảng giá tại một thời điểm. */
data class LiveBoard(
    val updatedAt: Long,
    val tradingDate: String?,
    val session: MarketSession,
    val quotes: List<LiveQuote>
) {
    val advancing: Int get() = quotes.count { it.changePercent > 0 }
    val declining: Int get() = quotes.count { it.changePercent < 0 }
    val unchanged: Int get() = quotes.count { it.changePercent == 0.0 }
    val ceilingCount: Int get() = quotes.count { it.state == PriceState.CEILING }
    val floorCount: Int get() = quotes.count { it.state == PriceState.FLOOR }

    val totalTradedValue: Double get() = quotes.sumOf { it.tradedValue }
    val totalForeignNet: Double get() = quotes.sumOf { it.foreignNetValue }
}

/** Tiêu chí sắp xếp bảng giá. */
enum class BoardSort(val label: String) {
    TOP_GAINERS("Tăng mạnh"),
    TOP_LOSERS("Giảm mạnh"),
    TOP_VALUE("GTGD lớn"),
    FOREIGN_BUY("NN mua ròng"),
    FOREIGN_SELL("NN bán ròng");

    fun apply(quotes: List<LiveQuote>): List<LiveQuote> = when (this) {
        TOP_GAINERS -> quotes.sortedByDescending { it.changePercent }
        TOP_LOSERS -> quotes.sortedBy { it.changePercent }
        TOP_VALUE -> quotes.sortedByDescending { it.tradedValue }
        FOREIGN_BUY -> quotes.sortedByDescending { it.foreignNetValue }
        FOREIGN_SELL -> quotes.sortedBy { it.foreignNetValue }
    }
}
