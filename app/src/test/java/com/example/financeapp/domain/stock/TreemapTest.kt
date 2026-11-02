package com.example.financeapp.domain.stock

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class TreemapTest {

    private val W = 360f
    private val H = 260f

    /** Số mã mỗi ngành lấy từ một lần quét thật. */
    private val realWeights = listOf(
        18.0, 14.0, 10.0, 9.0, 6.0, 6.0, 5.0, 5.0, 4.0, 4.0,
        3.0, 3.0, 3.0, 3.0, 3.0, 1.0, 1.0, 1.0
    )

    private fun Treemap.Tile.area() = width * height

    @Test
    fun `moi o deu duoc dat`() {
        val tiles = Treemap.layout(realWeights, W, H)
        assertEquals(realWeights.size, tiles.size)
        assertEquals(realWeights.indices.toSet(), tiles.map { it.index }.toSet())
    }

    @Test
    fun `tong dien tich bang dien tich hinh goc`() {
        val tiles = Treemap.layout(realWeights, W, H)
        assertEquals(W * H, tiles.sumOf { it.area().toDouble() }.toFloat(), W * H * 0.001f)
    }

    @Test
    fun `dien tich ti le voi trong so`() {
        val tiles = Treemap.layout(realWeights, W, H).associateBy { it.index }
        val total = realWeights.sum()
        realWeights.forEachIndexed { i, w ->
            val expected = (w / total * W * H).toFloat()
            assertEquals("Ô $i sai diện tích", expected, tiles.getValue(i).area(), expected * 0.01f)
        }
    }

    @Test
    fun `cac o khong chong len nhau`() {
        val tiles = Treemap.layout(realWeights, W, H)
        for (i in tiles.indices) for (j in i + 1 until tiles.size) {
            val a = tiles[i]
            val b = tiles[j]
            val overlapW = min(a.x + a.width, b.x + b.width) - max(a.x, b.x)
            val overlapH = min(a.y + a.height, b.y + b.height) - max(a.y, b.y)
            val overlap = max(0f, overlapW) * max(0f, overlapH)
            assertTrue("Ô ${a.index} và ${b.index} chồng nhau ($overlap)", overlap < 0.5f)
        }
    }

    @Test
    fun `cac o nam tron trong khung`() {
        Treemap.layout(realWeights, W, H).forEach { t ->
            assertTrue("Ô ${t.index} tràn trái/trên", t.x >= -0.01f && t.y >= -0.01f)
            assertTrue("Ô ${t.index} tràn phải", t.x + t.width <= W + 0.01f)
            assertTrue("Ô ${t.index} tràn dưới", t.y + t.height <= H + 0.01f)
        }
    }

    @Test
    fun `o lon hon thi trong so lon hon`() {
        val tiles = Treemap.layout(realWeights, W, H).associateBy { it.index }
        // Ngành 18 mã phải có ô lớn hơn ngành 1 mã
        assertTrue(tiles.getValue(0).area() > tiles.getValue(17).area())
    }

    @Test
    fun `ti le canh du vuong de doc duoc chu`() {
        val tiles = Treemap.layout(realWeights, W, H)
        // Squarified phải giữ phần lớn ô không quá dẹt
        val ratios = tiles.map { max(it.width, it.height) / min(it.width, it.height) }
        val bad = ratios.count { it > 4f }
        assertTrue("Có $bad ô quá dẹt (tỷ lệ > 4:1)", bad <= 2)
    }

    @Test
    fun `bo qua trong so khong duong`() {
        val tiles = Treemap.layout(listOf(5.0, 0.0, 3.0, -1.0), W, H)
        assertEquals(2, tiles.size)
        assertEquals(setOf(0, 2), tiles.map { it.index }.toSet())
    }

    @Test
    fun `tra ve rong khi khung khong hop le`() {
        assertTrue(Treemap.layout(listOf(1.0, 2.0), 0f, H).isEmpty())
        assertTrue(Treemap.layout(emptyList(), W, H).isEmpty())
    }

    @Test
    fun `mot o chiem toan bo khung`() {
        val tiles = Treemap.layout(listOf(7.0), W, H)
        assertEquals(1, tiles.size)
        assertEquals(W, tiles[0].width, 0.01f)
        assertEquals(H, tiles[0].height, 0.01f)
    }

    @Test
    fun `trong so bang nhau cho dien tich bang nhau`() {
        val tiles = Treemap.layout(List(4) { 1.0 }, 200f, 200f)
        val areas = tiles.map { it.area() }
        assertTrue(areas.all { abs(it - 10_000f) < 1f })
    }
}
