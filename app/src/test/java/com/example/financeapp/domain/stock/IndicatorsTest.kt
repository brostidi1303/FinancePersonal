package com.example.financeapp.domain.stock

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Đối chiếu chỉ báo với bộ số mẫu gốc trong sách của J. Welles Wilder
 * ("New Concepts in Technical Trading Systems"), nơi RSI được công bố sẵn đáp án.
 */
class IndicatorsTest {

    /** 33 giá đóng cửa mẫu của Wilder. */
    private val wilder = listOf(
        44.34, 44.09, 44.15, 43.61, 44.33, 44.83, 45.10, 45.42, 45.84, 46.08,
        45.89, 46.03, 45.61, 46.28, 46.28, 46.00, 46.03, 46.41, 46.22, 45.64,
        46.21, 46.25, 45.71, 46.45, 45.78, 45.35, 44.03, 44.18, 44.22, 44.57,
        43.42, 42.66, 43.13
    )

    @Test
    fun `rsi khop so cong bo cua Wilder`() {
        assertEquals(70.46, Indicators.rsi(wilder.take(15))!!, 0.01)
        assertEquals(66.25, Indicators.rsi(wilder.take(16))!!, 0.01)
        assertEquals(66.48, Indicators.rsi(wilder.take(17))!!, 0.01)
    }

    @Test
    fun `rsi tra null khi thieu du lieu`() {
        assertNull(Indicators.rsi(wilder.take(14)))
    }

    @Test
    fun `rsi bang 100 khi chi tang`() {
        assertEquals(100.0, Indicators.rsi((1..20).map { it.toDouble() })!!, 0.001)
    }

    @Test
    fun `sma tinh dung tren n phien cuoi`() {
        val xs = listOf(1.0, 2.0, 3.0, 4.0, 5.0)
        assertEquals(4.0, Indicators.sma(xs, 3)!!, 1e-9)
        assertEquals(3.0, Indicators.sma(xs, 5)!!, 1e-9)
        assertNull(Indicators.sma(xs, 6))
    }

    @Test
    fun `ema khoi tao bang sma roi lam muot`() {
        val xs = (1..10).map { it.toDouble() }
        val ema = Indicators.emaSeries(xs, 5)
        // 5 phần tử đầu chưa đủ chu kỳ trừ phần tử thứ 5 = SMA(1..5) = 3.0
        assertNull(ema[3])
        assertEquals(3.0, ema[4]!!, 1e-9)
        assertEquals(8.0, ema.last()!!, 1e-9)
    }

    @Test
    fun `roc tinh phan tram thay doi`() {
        val xs = listOf(100.0, 105.0, 110.0, 120.0)
        assertEquals(20.0, Indicators.roc(xs, 3)!!, 1e-9)    // 120 so với 100 (3 phiên trước)
        assertEquals(14.29, Indicators.roc(xs, 2)!!, 0.01)   // 120 so với 105 (2 phiên trước)
        assertEquals(9.09, Indicators.roc(xs, 1)!!, 0.01)    // 120 so với 110 (1 phiên trước)
    }

    @Test
    fun `macd duong khi gia tang deu`() {
        val up = (1..60).map { 100.0 + it }
        val m = Indicators.macd(up)!!
        assertTrue("MACD phải dương khi giá tăng đều", m.line > 0)
        // Với đà tăng tuyến tính, MACD đạt trạng thái dừng nên line hội tụ đúng bằng
        // signal (cả hai = 7.0) — không thể kỳ vọng line > signal ở đây.
        assertEquals(m.line, m.signal, 1e-6)
        assertEquals(0.0, m.histogram, 1e-6)
    }

    @Test
    fun `macd cat len tin hieu khi gia bat tang tro lai`() {
        val mixed = (0 until 40).map { 100.0 - it * 0.5 } + (0 until 25).map { 80.0 + it * 1.2 }
        val m = Indicators.macd(mixed)!!
        assertTrue("MACD phải vượt lên đường tín hiệu sau khi giá bật tăng", m.line > m.signal)
        assertTrue("Histogram phải dương", m.histogram > 0)
    }

    @Test
    fun `atr bang bien do khi khong co gap`() {
        val n = 20
        val highs = List(n) { 11.0 }
        val lows = List(n) { 10.0 }
        val closes = List(n) { 10.5 }
        assertEquals(1.0, Indicators.atr(highs, lows, closes, 14)!!, 1e-9)
    }

    @Test
    fun `rangePosition xac dinh vi tri trong bien do`() {
        val highs = List(20) { 110.0 }
        val lows = List(20) { 100.0 }
        val closes = List(19) { 105.0 } + 110.0
        assertEquals(1.0, Indicators.rangePosition(highs, lows, closes, 20)!!, 1e-9)
    }

    @Test
    fun `clamp gioi han trong khoang`() {
        assertEquals(0.0, Indicators.clamp(-50.0), 1e-9)
        assertEquals(100.0, Indicators.clamp(150.0), 1e-9)
        assertEquals(42.0, Indicators.clamp(42.0), 1e-9)
    }
}
