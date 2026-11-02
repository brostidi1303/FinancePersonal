package com.example.financeapp.domain.stock

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Chỉ báo phân tích kỹ thuật thuần (không phụ thuộc Android).
 * RSI dùng phương pháp làm mượt Wilder — đã đối chiếu khớp bộ số mẫu gốc của Wilder.
 */
object Indicators {

    /** Trung bình động đơn giản tại phiên cuối. */
    fun sma(xs: List<Double>, n: Int): Double? {
        if (n <= 0 || xs.size < n) return null
        return xs.takeLast(n).sum() / n
    }

    /** Chuỗi EMA; các phần tử trước khi đủ chu kỳ là null. */
    fun emaSeries(xs: List<Double>, n: Int): List<Double?> {
        if (n <= 0 || xs.size < n) return List(xs.size) { null }
        val k = 2.0 / (n + 1)
        val out = arrayOfNulls<Double>(xs.size)
        var prev = xs.take(n).sum() / n
        out[n - 1] = prev
        for (i in n until xs.size) {
            prev = xs[i] * k + prev * (1 - k)
            out[i] = prev
        }
        return out.toList()
    }

    /** RSI Wilder. Trả null nếu không đủ dữ liệu. */
    fun rsi(closes: List<Double>, n: Int = 14): Double? {
        if (closes.size < n + 1) return null
        var gains = 0.0
        var losses = 0.0
        for (i in 1..n) {
            val d = closes[i] - closes[i - 1]
            gains += max(d, 0.0)
            losses += max(-d, 0.0)
        }
        var avgGain = gains / n
        var avgLoss = losses / n
        for (i in n + 1 until closes.size) {
            val d = closes[i] - closes[i - 1]
            avgGain = (avgGain * (n - 1) + max(d, 0.0)) / n
            avgLoss = (avgLoss * (n - 1) + max(-d, 0.0)) / n
        }
        if (avgLoss == 0.0) return 100.0
        return 100.0 - 100.0 / (1.0 + avgGain / avgLoss)
    }

    data class Macd(
        val line: Double,
        val signal: Double,
        val histogram: Double,
        val previousHistogram: Double
    )

    fun macd(closes: List<Double>, fast: Int = 12, slow: Int = 26, signal: Int = 9): Macd? {
        val ef = emaSeries(closes, fast)
        val es = emaSeries(closes, slow)
        val line = closes.indices.mapNotNull { i ->
            val a = ef.getOrNull(i); val b = es.getOrNull(i)
            if (a != null && b != null) a - b else null
        }
        if (line.size < signal + 1) return null
        val sig = emaSeries(line, signal)
        val s1 = sig.lastOrNull() ?: return null
        val s0 = sig.getOrNull(sig.size - 2) ?: return null
        val m1 = line.last()
        val m0 = line[line.size - 2]
        return Macd(m1, s1, m1 - s1, m0 - s0)
    }

    /** Tỷ lệ thay đổi giá sau n phiên, tính theo %. */
    fun roc(closes: List<Double>, n: Int): Double? {
        if (closes.size < n + 1) return null
        val base = closes[closes.size - 1 - n]
        if (base == 0.0) return null
        return (closes.last() / base - 1.0) * 100.0
    }

    /** Average True Range (Wilder) — dùng để đặt cắt lỗ / mục tiêu theo biến động thực. */
    fun atr(highs: List<Double>, lows: List<Double>, closes: List<Double>, n: Int = 14): Double? {
        if (closes.size < n + 1) return null
        val trs = (1 until closes.size).map { i ->
            maxOf(
                highs[i] - lows[i],
                abs(highs[i] - closes[i - 1]),
                abs(lows[i] - closes[i - 1])
            )
        }
        if (trs.size < n) return null
        var a = trs.take(n).sum() / n
        for (i in n until trs.size) a = (a * (n - 1) + trs[i]) / n
        return a
    }

    /** Vị trí giá hiện tại trong biên độ n phiên: 0 = đáy, 1 = đỉnh. */
    fun rangePosition(highs: List<Double>, lows: List<Double>, closes: List<Double>, n: Int = 20): Double? {
        if (closes.size < n) return null
        val hi = highs.takeLast(n).max()
        val lo = lows.takeLast(n).min()
        if (hi == lo) return 0.5
        return (closes.last() - lo) / (hi - lo)
    }

    fun clamp(x: Double, lo: Double = 0.0, hi: Double = 100.0): Double = min(hi, max(lo, x))
}
