package com.example.financeapp.domain.stock

import com.example.financeapp.data.market.Candle
import com.example.financeapp.data.market.SsiStock
import com.example.financeapp.data.market.StockUniverse
import com.example.financeapp.domain.stock.Indicators.clamp
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Chấm điểm kế hoạch mua cho khung 3 ngày và 7 ngày.
 *
 * Nguyên tắc: mọi điểm số đều suy ra từ luật tường minh bên dưới, không có hộp đen.
 * Điểm cuối phản ánh mức ĐỒNG THUẬN GIỮA CÁC CHỈ BÁO, không phải xác suất sinh lời.
 */
@Singleton
class BuyPlanAnalyzer @Inject constructor() {

    companion object {
        /** Giá trị giao dịch trung bình 20 phiên tối thiểu (đồng) để mã đủ thanh khoản. */
        const val MIN_AVG_VALUE = 5_000_000_000.0

        /** RSI vượt ngưỡng này coi như quá mua cực đoan, không mua đuổi. */
        const val MAX_RSI = 85.0

        /** Số phiên tối thiểu để tính đủ MA50 và MACD. */
        const val MIN_CANDLES = 60

        /** VNDirect trả giá theo nghìn đồng, SSI theo đồng. Quy đổi tại đây. */
        private const val VND_PER_UNIT = 1000.0

        val WEIGHTS_SHORT = mapOf(
            "roc" to 0.18, "volume" to 0.16, "rsi" to 0.14, "macdHist" to 0.14,
            "ma20" to 0.12, "rs" to 0.14, "foreign" to 0.12
        )
        val WEIGHTS_MEDIUM = mapOf(
            "trend" to 0.20, "macdCross" to 0.15, "roc" to 0.15, "rs" to 0.15,
            "rsi" to 0.12, "volTrend" to 0.12, "foreign" to 0.11
        )
    }

    sealed interface Result {
        data class Ok(val analysis: StockAnalysis) : Result
        data class Skipped(val reason: String) : Result
    }

    // ── Chấm điểm từng yếu tố, mỗi hàm trả về 0..100 ─────────────────────

    /** RSI: vùng 50-65 là tăng lành mạnh; quá cao là mua đuổi, quá thấp là bắt dao rơi. */
    internal fun scoreRsi(r: Double?): Double = when {
        r == null -> 50.0
        r in 50.0..65.0 -> 100.0
        r >= 45.0 && r < 50.0 -> 80.0
        r > 65.0 && r <= 72.0 -> 70.0
        r >= 40.0 && r < 45.0 -> 60.0
        r >= 30.0 && r < 40.0 -> 45.0
        r > 72.0 && r <= 80.0 -> 35.0
        r < 30.0 -> 40.0
        else -> 10.0
    }

    /** Khối lượng phiên gần nhất so với trung bình 20 phiên. */
    internal fun scoreVolume(ratio: Double?): Double = when {
        ratio == null -> 50.0
        ratio >= 2.0 -> 100.0
        ratio >= 1.5 -> 85.0
        ratio >= 1.2 -> 70.0
        ratio >= 0.8 -> 50.0
        ratio >= 0.5 -> 30.0
        else -> 15.0
    }

    /** Đà tăng giá. Tăng quá nóng bị hạ điểm vì rủi ro chốt lời. */
    internal fun scoreRoc(x: Double?, k: Double, exhaust: Double): Double = when {
        x == null -> 50.0
        x > exhaust -> 60.0
        else -> clamp(50.0 + x * k)
    }

    /** Sức mạnh tương đối so với VNINDEX. */
    internal fun scoreRelativeStrength(rs: Double?): Double =
        if (rs == null) 50.0 else clamp(50.0 + rs * 10.0)

    /** Khối ngoại mua ròng tính theo tỷ trọng trên tổng giá trị khớp lệnh. */
    internal fun scoreForeign(net: Double, total: Double): Double =
        if (total <= 0.0) 50.0 else clamp(50.0 + (net / total) * 500.0)

    /** Khoảng cách giá tới MA20: sát ngay trên MA20 là vùng vào đẹp nhất. */
    internal fun scoreMa20Gap(close: Double, ma20: Double?): Double {
        if (ma20 == null || ma20 == 0.0) return 50.0
        val gap = (close / ma20 - 1.0) * 100.0
        return when {
            gap in 0.0..4.0 -> 100.0
            gap > 4.0 && gap <= 8.0 -> 75.0
            gap >= -2.0 && gap < 0.0 -> 65.0
            gap > 8.0 && gap <= 14.0 -> 45.0
            gap >= -5.0 && gap < -2.0 -> 40.0
            gap > 14.0 -> 20.0
            else -> 15.0
        }
    }

    /** Histogram MACD đang mở rộng nghĩa là động lượng đang mạnh lên. */
    internal fun scoreMacdHistogram(m: Indicators.Macd?): Double {
        if (m == null) return 50.0
        return when {
            m.histogram > 0 && m.histogram > m.previousHistogram -> 100.0
            m.histogram <= 0 && m.histogram > m.previousHistogram -> 72.0
            m.histogram > 0 && m.histogram <= m.previousHistogram -> 48.0
            else -> 18.0
        }
    }

    /** MACD nằm trên đường tín hiệu và trên 0 là tín hiệu tăng rõ nhất. */
    internal fun scoreMacdCross(m: Indicators.Macd?): Double {
        if (m == null) return 50.0
        return when {
            m.line > m.signal && m.line > 0 -> 100.0
            m.line > m.signal -> 75.0
            m.line > 0 -> 40.0
            else -> 20.0
        }
    }

    /** Cấu trúc xu hướng: giá > MA20 > MA50 là xếp lớp tăng chuẩn. */
    internal fun scoreTrend(close: Double, ma20: Double?, ma50: Double?): Double {
        if (ma20 == null || ma50 == null) return 50.0
        return when {
            close > ma20 && ma20 > ma50 -> 100.0
            close > ma20 -> 70.0
            ma20 > ma50 -> 45.0
            else -> 15.0
        }
    }

    /** Thanh khoản 5 phiên so với 20 phiên: dòng tiền đang vào hay đang rút. */
    internal fun scoreVolumeTrend(v5: Double?, v20: Double?): Double {
        if (v5 == null || v20 == null || v20 == 0.0) return 50.0
        return clamp(50.0 + (v5 / v20 - 1.0) * 120.0)
    }

    // ── Bối cảnh thị trường ──────────────────────────────────────────────

    fun buildContext(indexCandles: List<Candle>): MarketContext {
        val c = indexCandles.map { it.close }
        val ma20 = Indicators.sma(c, 20)
        val ma50 = Indicators.sma(c, 50)
        val close = c.lastOrNull() ?: 0.0
        return MarketContext(
            indexClose = close,
            indexMa20 = ma20,
            indexMa50 = ma50,
            indexRoc3 = Indicators.roc(c, 3),
            indexRoc7 = Indicators.roc(c, 7),
            regime = MarketRegime.from(close, ma20, ma50)
        )
    }

    // ── Phân tích một mã ─────────────────────────────────────────────────

    fun analyze(
        symbol: String,
        candles: List<Candle>,
        snapshot: SsiStock,
        ctx: MarketContext
    ): Result {
        if (candles.size < MIN_CANDLES) return Result.Skipped("Không đủ lịch sử giá")

        val closes = candles.map { it.close }
        val highs = candles.map { it.high }
        val lows = candles.map { it.low }
        val volumes = candles.map { it.volume }

        val close = closes.last()
        val ma20 = Indicators.sma(closes, 20)
        val ma50 = Indicators.sma(closes, 50)
        val vol20 = Indicators.sma(volumes, 20)
        val vol5 = Indicators.sma(volumes, 5)
        val rsi = Indicators.rsi(closes, 14)
        val macd = Indicators.macd(closes)
        val roc3 = Indicators.roc(closes, 3)
        val roc7 = Indicators.roc(closes, 7)
        val atr = Indicators.atr(highs, lows, closes, 14)

        // Lọc cứng trước khi chấm điểm
        val avgValue = (vol20 ?: 0.0) * close * VND_PER_UNIT
        if (avgValue < MIN_AVG_VALUE) return Result.Skipped("Thanh khoản thấp")
        if (rsi != null && rsi > MAX_RSI) return Result.Skipped("Quá mua cực đoan")
        if (snapshot.isAtCeiling) return Result.Skipped("Đang giá trần")
        if (atr == null || atr <= 0.0) return Result.Skipped("Không tính được biến động")

        val volumeRatio = if (vol20 != null && vol20 > 0) volumes.last() / vol20 else null
        val foreignNet = snapshot.foreignNetValue
        val tradedValue = snapshot.nmTotalTradedValue ?: 0.0

        // Các yếu tố dùng chung cho cả hai khung thời gian
        val sRsi = scoreRsi(rsi)
        val sVolume = scoreVolume(volumeRatio)
        val sMa20 = scoreMa20Gap(close, ma20)
        val sMacdHist = scoreMacdHistogram(macd)
        val sMacdCross = scoreMacdCross(macd)
        val sTrend = scoreTrend(close, ma20, ma50)
        val sVolTrend = scoreVolumeTrend(vol5, vol20)
        val sForeign = scoreForeign(foreignNet, tradedValue)

        val shortFactors = listOf(
            factor("roc", "Đà giá 3 phiên", scoreRoc(roc3, k = 8.0, exhaust = 10.0), WEIGHTS_SHORT,
                up = "Đà tăng giá ngắn hạn tốt", down = "Giá đang giảm trong ngắn hạn"),
            factor("volume", "Khối lượng phiên", sVolume, WEIGHTS_SHORT,
                up = "Khối lượng đột biến xác nhận dòng tiền", down = "Khối lượng cạn, thiếu lực mua"),
            factor("rsi", "RSI(14)", sRsi, WEIGHTS_SHORT,
                up = "RSI ở vùng tăng lành mạnh", down = "RSI ở vùng rủi ro"),
            factor("macdHist", "Động lượng MACD", sMacdHist, WEIGHTS_SHORT,
                up = "MACD histogram đang mở rộng", down = "Động lượng MACD suy yếu"),
            factor("ma20", "Vị trí so với MA20", sMa20, WEIGHTS_SHORT,
                up = "Giá nằm ngay trên MA20, vùng vào đẹp", down = "Giá lệch xa MA20"),
            factor("rs", "Sức mạnh vs VNINDEX", scoreRelativeStrength(diff(roc3, ctx.indexRoc3)), WEIGHTS_SHORT,
                up = "Khỏe hơn VNINDEX", down = "Yếu hơn VNINDEX"),
            factor("foreign", "Khối ngoại", sForeign, WEIGHTS_SHORT,
                up = "Khối ngoại mua ròng", down = "Khối ngoại bán ròng")
        )

        val mediumFactors = listOf(
            factor("trend", "Cấu trúc xu hướng", sTrend, WEIGHTS_MEDIUM,
                up = "Xu hướng tăng rõ (giá > MA20 > MA50)", down = "Cấu trúc xu hướng chưa ủng hộ"),
            factor("macdCross", "MACD vs tín hiệu", sMacdCross, WEIGHTS_MEDIUM,
                up = "MACD nằm trên đường tín hiệu", down = "MACD dưới đường tín hiệu"),
            factor("roc", "Đà giá 7 phiên", scoreRoc(roc7, k = 5.0, exhaust = 15.0), WEIGHTS_MEDIUM,
                up = "Đà tăng duy trì qua 7 phiên", down = "Giá giảm trong 7 phiên"),
            factor("rs", "Sức mạnh vs VNINDEX", scoreRelativeStrength(diff(roc7, ctx.indexRoc7)), WEIGHTS_MEDIUM,
                up = "Khỏe hơn VNINDEX", down = "Yếu hơn VNINDEX"),
            factor("rsi", "RSI(14)", sRsi, WEIGHTS_MEDIUM,
                up = "RSI ở vùng tăng lành mạnh", down = "RSI ở vùng rủi ro"),
            factor("volTrend", "Xu hướng thanh khoản", sVolTrend, WEIGHTS_MEDIUM,
                up = "Thanh khoản 5 phiên cải thiện", down = "Thanh khoản đang thu hẹp"),
            factor("foreign", "Khối ngoại", sForeign, WEIGHTS_MEDIUM,
                up = "Khối ngoại mua ròng", down = "Khối ngoại bán ròng")
        )

        val mult = ctx.regime.multiplier
        val priceVnd = close * VND_PER_UNIT
        val atrVnd = atr * VND_PER_UNIT

        return Result.Ok(
            StockAnalysis(
                symbol = symbol,
                sector = StockUniverse.sectorOf(symbol),
                price = priceVnd,
                changePercent = snapshot.priceChangePercent ?: 0.0,
                rsi = rsi,
                ma20 = ma20?.times(VND_PER_UNIT),
                ma50 = ma50?.times(VND_PER_UNIT),
                volumeRatio = volumeRatio,
                roc3 = roc3,
                roc7 = roc7,
                atr = atrVnd,
                foreignNetValue = foreignNet,
                tradedValue = tradedValue,
                shortPlan = BuyPlan(
                    horizon = Horizon.SHORT,
                    score = weightedScore(shortFactors) * mult,
                    factors = shortFactors,
                    entryLow = priceVnd - 0.3 * atrVnd,
                    entryHigh = priceVnd + 0.3 * atrVnd,
                    stopLoss = priceVnd - 1.5 * atrVnd,
                    target = priceVnd + 2.5 * atrVnd
                ),
                mediumPlan = BuyPlan(
                    horizon = Horizon.MEDIUM,
                    score = weightedScore(mediumFactors) * mult,
                    factors = mediumFactors,
                    entryLow = priceVnd - 0.3 * atrVnd,
                    entryHigh = priceVnd + 0.3 * atrVnd,
                    stopLoss = priceVnd - 2.0 * atrVnd,
                    target = priceVnd + 3.5 * atrVnd
                )
            )
        )
    }

    /** Gom điểm theo ngành để trả lời câu hỏi ngành nào đang khỏe. */
    fun sectorScores(analyses: List<StockAnalysis>, horizon: Horizon): List<SectorScore> =
        analyses.groupBy { it.sector }
            .map { (sector, items) ->
                val sorted = items.sortedByDescending { it.planFor(horizon).score }
                SectorScore(
                    sector = sector,
                    averageScore = sorted.sumOf { it.planFor(horizon).score } / sorted.size,
                    symbolCount = sorted.size,
                    topSymbols = sorted.take(3).map { it.symbol }
                )
            }
            .sortedByDescending { it.averageScore }

    private fun diff(a: Double?, b: Double?): Double? =
        if (a == null) null else a - (b ?: 0.0)

    private fun weightedScore(factors: List<ScoredFactor>): Double =
        factors.sumOf { it.score * it.weight }

    private fun factor(
        key: String,
        label: String,
        score: Double,
        weights: Map<String, Double>,
        up: String,
        down: String
    ): ScoredFactor = ScoredFactor(
        key = key,
        label = label,
        score = score,
        weight = weights[key] ?: 0.0,
        detail = if (score >= 70.0) up else down
    )
}
