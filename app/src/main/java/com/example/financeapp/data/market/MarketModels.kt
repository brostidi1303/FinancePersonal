package com.example.financeapp.data.market

import com.google.gson.annotations.SerializedName

/**
 * Response của VNDirect dchart (định dạng TradingView UDF).
 * LƯU Ý ĐƠN VỊ: giá trả về theo NGHÌN ĐỒNG (50.3 = 50.300đ), khác SSI trả theo đồng.
 */
data class ChartHistoryResponse(
    @SerializedName("t") val time: List<Long>?,
    @SerializedName("o") val open: List<Double>?,
    @SerializedName("h") val high: List<Double>?,
    @SerializedName("l") val low: List<Double>?,
    @SerializedName("c") val close: List<Double>?,
    @SerializedName("v") val volume: List<Double>?,
    @SerializedName("s") val status: String?
) {
    val isOk: Boolean get() = status == "ok"

    /** Chuẩn hoá về list nến, bỏ các phiên thiếu dữ liệu. */
    fun toCandles(): List<Candle> {
        val t = time ?: return emptyList()
        val o = open ?: return emptyList()
        val h = high ?: return emptyList()
        val l = low ?: return emptyList()
        val c = close ?: return emptyList()
        val v = volume ?: return emptyList()
        val n = minOf(t.size, o.size, h.size, l.size, c.size, v.size)
        return (0 until n).map { i -> Candle(t[i], o[i], h[i], l[i], c[i], v[i]) }
    }
}

/** Một phiên giao dịch. Giá theo nghìn đồng (đơn vị gốc của VNDirect). */
data class Candle(
    val time: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double
)

/** Response của SSI iboard - snapshot toàn sàn. */
data class SsiBoardResponse(
    val code: String?,
    val message: String?,
    val data: List<SsiStock>?
)

/** Giá ở đây theo ĐỒNG (50300), không phải nghìn đồng. */
data class SsiStock(
    val stockSymbol: String?,
    val stockType: String?,
    val exchange: String?,
    val matchedPrice: Double?,
    val priceChangePercent: Double?,
    val refPrice: Double?,
    val ceiling: Double?,
    val floor: Double?,
    val openPrice: Double?,
    val highest: Double?,
    val lowest: Double?,
    val nmTotalTradedQty: Double?,
    val nmTotalTradedValue: Double?,
    val buyForeignValue: Double?,
    val sellForeignValue: Double?,
    val tradingDate: String?,
    /** Mã trạng thái phiên, ví dụ "LO", "ATC", "BREAK". */
    val session: String? = null
) {
    /** Chỉ lấy cổ phiếu thường, bỏ chứng quyền/ETF/trái phiếu. */
    val isCommonStock: Boolean get() = stockType == "s" && !stockSymbol.isNullOrBlank()

    /** Giá trị khối ngoại mua ròng trong phiên (đồng). Dương = mua ròng. */
    val foreignNetValue: Double get() = (buyForeignValue ?: 0.0) - (sellForeignValue ?: 0.0)

    /** Đang dư mua trần / dư bán sàn thì không thể khớp lệnh mua hợp lý. */
    val isAtCeiling: Boolean
        get() = matchedPrice != null && ceiling != null && matchedPrice >= ceiling
}
