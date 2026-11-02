package com.example.financeapp.domain.stock

import com.example.financeapp.data.market.Candle
import com.example.financeapp.data.market.SsiStock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BuyPlanAnalyzerTest {

    private val analyzer = BuyPlanAnalyzer()

    private fun snapshot(
        symbol: String = "TEST",
        tradedValue: Double = 100_000_000_000.0,
        buyForeign: Double = 0.0,
        sellForeign: Double = 0.0,
        ceiling: Double? = null,
        matched: Double? = null
    ) = SsiStock(
        stockSymbol = symbol, stockType = "s", exchange = "hose",
        matchedPrice = matched, priceChangePercent = 0.0, refPrice = null,
        ceiling = ceiling, floor = null, openPrice = null, highest = null, lowest = null,
        nmTotalTradedQty = null, nmTotalTradedValue = tradedValue,
        buyForeignValue = buyForeign, sellForeignValue = sellForeign, tradingDate = null
    )

    /** Chuỗi nến tăng đều, khối lượng đủ lớn để qua bộ lọc thanh khoản. */
    private fun risingCandles(n: Int = 120, start: Double = 20.0, step: Double = 0.15) =
        (0 until n).map { i ->
            val c = start + i * step
            Candle(time = i.toLong(), open = c - 0.05, high = c + 0.1, low = c - 0.1, close = c, volume = 1_000_000.0)
        }

    private fun fallingCandles(n: Int = 120, start: Double = 50.0, step: Double = 0.15) =
        (0 until n).map { i ->
            val c = start - i * step
            Candle(time = i.toLong(), open = c + 0.05, high = c + 0.1, low = c - 0.1, close = c, volume = 1_000_000.0)
        }

    private fun neutralContext() = MarketContext(
        indexClose = 1000.0, indexMa20 = 1000.0, indexMa50 = 1000.0,
        indexRoc3 = 0.0, indexRoc7 = 0.0, regime = MarketRegime.NEUTRAL
    )

    // ── Bộ lọc cứng ──────────────────────────────────────────────────────

    @Test
    fun `loai ma thieu lich su gia`() {
        val r = analyzer.analyze("X", risingCandles(n = 30), snapshot(), neutralContext())
        assertTrue(r is BuyPlanAnalyzer.Result.Skipped)
        assertEquals("Không đủ lịch sử giá", (r as BuyPlanAnalyzer.Result.Skipped).reason)
    }

    @Test
    fun `loai ma thanh khoan thap`() {
        val thin = risingCandles().map { it.copy(volume = 100.0) }
        val r = analyzer.analyze("X", thin, snapshot(), neutralContext())
        assertEquals("Thanh khoản thấp", (r as BuyPlanAnalyzer.Result.Skipped).reason)
    }

    @Test
    fun `loai ma qua mua cuc doan`() {
        // Tăng liên tục không có phiên giảm -> RSI = 100
        val r = analyzer.analyze("X", risingCandles(), snapshot(), neutralContext())
        assertEquals("Quá mua cực đoan", (r as BuyPlanAnalyzer.Result.Skipped).reason)
    }

    @Test
    fun `loai ma dang gia tran`() {
        val candles = wavyCandles()
        val last = candles.last().close * 1000.0
        val r = analyzer.analyze(
            "X", candles,
            snapshot(ceiling = last, matched = last),
            neutralContext()
        )
        assertEquals("Đang giá trần", (r as BuyPlanAnalyzer.Result.Skipped).reason)
    }

    /** Dao động nhẹ quanh xu hướng tăng để RSI nằm trong vùng hợp lệ. */
    private fun wavyCandles(n: Int = 120) = (0 until n).map { i ->
        val c = 20.0 + i * 0.12 + if (i % 3 == 0) -0.25 else 0.08
        Candle(i.toLong(), c - 0.05, c + 0.15, c - 0.15, c, 1_000_000.0)
    }

    // ── Hướng của điểm số ────────────────────────────────────────────────

    @Test
    fun `xu huong tang cham diem cao hon xu huong giam`() {
        val up = analyzer.analyze("UP", wavyCandles(), snapshot(), neutralContext())
        val down = analyzer.analyze("DOWN", fallingCandles(), snapshot(), neutralContext())

        assertTrue("Mã tăng phải được phân tích", up is BuyPlanAnalyzer.Result.Ok)
        assertTrue("Mã giảm phải được phân tích", down is BuyPlanAnalyzer.Result.Ok)

        val upScore = (up as BuyPlanAnalyzer.Result.Ok).analysis.mediumPlan.score
        val downScore = (down as BuyPlanAnalyzer.Result.Ok).analysis.mediumPlan.score
        assertTrue("Điểm mã tăng ($upScore) phải cao hơn mã giảm ($downScore)", upScore > downScore)
    }

    @Test
    fun `thi truong bat loi lam giam diem`() {
        val riskOff = neutralContext().copy(regime = MarketRegime.RISK_OFF)
        val a = analyzer.analyze("X", wavyCandles(), snapshot(), neutralContext())
        val b = analyzer.analyze("X", wavyCandles(), snapshot(), riskOff)
        val sa = (a as BuyPlanAnalyzer.Result.Ok).analysis.mediumPlan.score
        val sb = (b as BuyPlanAnalyzer.Result.Ok).analysis.mediumPlan.score
        assertEquals(sa * (0.75 / 0.90), sb, 1e-6)
    }

    @Test
    fun `khoi ngoai mua rong cong diem`() {
        val ctx = neutralContext()
        val buy = analyzer.analyze("X", wavyCandles(), snapshot(buyForeign = 20e9), ctx)
        val sell = analyzer.analyze("X", wavyCandles(), snapshot(sellForeign = 20e9), ctx)
        val sBuy = (buy as BuyPlanAnalyzer.Result.Ok).analysis.mediumPlan.score
        val sSell = (sell as BuyPlanAnalyzer.Result.Ok).analysis.mediumPlan.score
        assertTrue("Mua ròng ($sBuy) phải hơn bán ròng ($sSell)", sBuy > sSell)
    }

    // ── Mức giá kế hoạch ─────────────────────────────────────────────────

    @Test
    fun `cat lo nam duoi va muc tieu nam tren gia hien tai`() {
        val r = analyzer.analyze("X", wavyCandles(), snapshot(), neutralContext())
        val a = (r as BuyPlanAnalyzer.Result.Ok).analysis
        listOf(a.shortPlan, a.mediumPlan).forEach { p ->
            assertTrue("Cắt lỗ phải dưới giá", p.stopLoss < a.price)
            assertTrue("Mục tiêu phải trên giá", p.target > a.price)
            assertTrue("Vùng mua phải bao quanh giá", p.entryLow < a.price && p.entryHigh > a.price)
            assertTrue("Tỷ lệ lãi/lỗ phải dương", (p.riskReward ?: 0.0) > 0)
        }
    }

    @Test
    fun `khung 7 ngay dat cat lo rong hon khung 3 ngay`() {
        val r = analyzer.analyze("X", wavyCandles(), snapshot(), neutralContext())
        val a = (r as BuyPlanAnalyzer.Result.Ok).analysis
        assertTrue(a.mediumPlan.stopLoss < a.shortPlan.stopLoss)
        assertTrue(a.mediumPlan.target > a.shortPlan.target)
    }

    @Test
    fun `gia duoc quy doi tu nghin dong sang dong`() {
        val candles = wavyCandles()
        val r = analyzer.analyze("X", candles, snapshot(), neutralContext())
        val a = (r as BuyPlanAnalyzer.Result.Ok).analysis
        assertEquals(candles.last().close * 1000.0, a.price, 1e-6)
    }

    // ── Trọng số ─────────────────────────────────────────────────────────

    @Test
    fun `tong trong so moi khung bang 1`() {
        assertEquals(1.0, BuyPlanAnalyzer.WEIGHTS_SHORT.values.sum(), 1e-9)
        assertEquals(1.0, BuyPlanAnalyzer.WEIGHTS_MEDIUM.values.sum(), 1e-9)
    }

    @Test
    fun `diem luon nam trong khoang 0 den 100`() {
        val ctx = neutralContext()
        listOf(wavyCandles(), fallingCandles()).forEach { candles ->
            val r = analyzer.analyze("X", candles, snapshot(), ctx)
            if (r is BuyPlanAnalyzer.Result.Ok) {
                listOf(r.analysis.shortPlan.score, r.analysis.mediumPlan.score).forEach {
                    assertTrue("Điểm $it ngoài khoảng hợp lệ", it in 0.0..100.0)
                }
            }
        }
    }

    // ── Trạng thái thị trường ────────────────────────────────────────────

    @Test
    fun `xac dinh dung trang thai thi truong`() {
        assertEquals(MarketRegime.RISK_ON, MarketRegime.from(110.0, 100.0, 90.0))
        assertEquals(MarketRegime.NEUTRAL, MarketRegime.from(95.0, 90.0, 100.0))
        assertEquals(MarketRegime.RISK_OFF, MarketRegime.from(80.0, 90.0, 100.0))
    }
}
