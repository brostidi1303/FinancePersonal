package com.example.financeapp.domain.stock

import kotlin.math.max
import kotlin.math.min

/**
 * Thuật toán squarified treemap (Bruls, Huizing & van Wijk).
 *
 * Chia một hình chữ nhật thành các ô có DIỆN TÍCH tỷ lệ với trọng số, đồng thời
 * ưu tiên cho các ô gần vuông nhất có thể — ô quá dẹt thì không đọc được chữ.
 *
 * Thuần Kotlin, không phụ thuộc Compose, để kiểm thử bằng unit test.
 */
object Treemap {

    /** Một ô đã được định vị trong hệ toạ độ gốc ở góc trên trái. */
    data class Tile(
        val index: Int,
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float
    )

    /**
     * @param weights trọng số của từng ô, theo đúng thứ tự đầu vào. Giá trị <= 0 bị bỏ qua.
     * @return danh sách ô, mỗi ô giữ [Tile.index] trỏ về vị trí trong [weights].
     */
    fun layout(weights: List<Double>, width: Float, height: Float): List<Tile> {
        if (width <= 0f || height <= 0f) return emptyList()

        val items = weights
            .mapIndexed { i, w -> i to w }
            .filter { it.second > 0.0 }
            .sortedByDescending { it.second }
        if (items.isEmpty()) return emptyList()

        val total = items.sumOf { it.second }
        val area = width.toDouble() * height.toDouble()
        // Quy đổi trọng số thành diện tích thật để phần còn lại luôn khớp hình chữ nhật còn trống.
        val areas = items.map { it.second / total * area }

        val out = ArrayList<Tile>(items.size)
        var x = 0.0
        var y = 0.0
        var w = width.toDouble()
        var h = height.toDouble()

        val row = ArrayList<Double>()
        val rowIdx = ArrayList<Int>()

        fun flushRow() {
            if (row.isEmpty()) return
            val sum = row.sum()
            if (w >= h) {
                // Dải dọc bám mép trái
                val stripW = if (h > 0) sum / h else 0.0
                var cy = y
                for (k in row.indices) {
                    val tileH = if (stripW > 0) row[k] / stripW else 0.0
                    out += Tile(rowIdx[k], x.toFloat(), cy.toFloat(), stripW.toFloat(), tileH.toFloat())
                    cy += tileH
                }
                x += stripW
                w -= stripW
            } else {
                // Dải ngang bám mép trên
                val stripH = if (w > 0) sum / w else 0.0
                var cx = x
                for (k in row.indices) {
                    val tileW = if (stripH > 0) row[k] / stripH else 0.0
                    out += Tile(rowIdx[k], cx.toFloat(), y.toFloat(), tileW.toFloat(), stripH.toFloat())
                    cx += tileW
                }
                y += stripH
                h -= stripH
            }
            row.clear()
            rowIdx.clear()
        }

        for (i in areas.indices) {
            val a = areas[i]
            val side = min(w, h)
            if (row.isEmpty()) {
                row += a
                rowIdx += items[i].first
                continue
            }
            val currentWorst = worstRatio(row, side)
            row += a
            val nextWorst = worstRatio(row, side)
            if (nextWorst > currentWorst) {
                // Thêm ô này làm dải xấu đi -> chốt dải hiện tại rồi mở dải mới
                row.removeAt(row.size - 1)
                flushRow()
                row += a
                rowIdx += items[i].first
            } else {
                rowIdx += items[i].first
            }
        }
        flushRow()

        return out
    }

    /**
     * Tỷ lệ cạnh tệ nhất trong một dải nếu đặt [row] dọc theo cạnh dài [side].
     * Càng gần 1 càng vuông.
     */
    private fun worstRatio(row: List<Double>, side: Double): Double {
        if (row.isEmpty() || side <= 0.0) return Double.MAX_VALUE
        val sum = row.sum()
        if (sum <= 0.0) return Double.MAX_VALUE
        val maxArea = row.max()
        val minArea = row.min()
        if (minArea <= 0.0) return Double.MAX_VALUE
        val s2 = sum * sum
        val w2 = side * side
        return max(w2 * maxArea / s2, s2 / (w2 * minArea))
    }
}
