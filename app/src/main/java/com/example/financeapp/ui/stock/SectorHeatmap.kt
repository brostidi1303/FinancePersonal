package com.example.financeapp.ui.stock

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financeapp.domain.stock.SectorScore
import com.example.financeapp.domain.stock.Treemap
import com.example.financeapp.ui.theme.AppTheme
import kotlin.math.pow

/** Màu trung tính tại mốc 50 điểm — ranh giới giữa yếu và khỏe. */
private val HeatNeutral = Color(0xFF4B5563)
private val HeatStrongGreen = Color(0xFF15803D)
private val HeatStrongRed = Color(0xFFB91C1C)

/**
 * Ngành có ít hơn ngần này mã sẽ không lên bản đồ.
 *
 * Lý do thực tế: với 19 ngành trên màn 360dp, ô của ngành 1 mã chỉ rộng ~31dp —
 * không đủ chỗ cho tên ngành nên ô chỉ còn trơ lại con số, vô nghĩa. Ngoài ra
 * "điểm trung bình ngành" của một ngành 1 mã thực chất chỉ là điểm của mã đó.
 */
private const val MIN_SYMBOLS_FOR_TILE = 2

/**
 * Hệ số làm dịu kích thước ô. 1.0 = diện tích đúng tỷ lệ số mã, càng nhỏ thì
 * chênh lệch càng được nén lại để ngành nhỏ vẫn đọc được.
 */
private const val SIZE_DAMPING = 0.8

/** Ngưỡng kích thước ô (dp) để quyết định hiển thị bao nhiêu chữ. */
private const val ROOMY_W = 84f
private const val ROOMY_H = 76f
private const val MEDIUM_W = 52f
private const val MEDIUM_H = 40f

/**
 * Thang màu phân kỳ quanh mốc 50 điểm: trên 50 ngả xanh, dưới 50 ngả đỏ.
 *
 * Dùng thang TUYỆT ĐỐI chứ không co giãn theo min/max của phiên — nhờ vậy khi cả
 * thị trường yếu thì bản đồ đỏ toàn bộ, và đó chính là thông tin cần thấy.
 */
internal fun sectorHeatColor(score: Double): Color {
    val t = ((score - 50.0) / 50.0).coerceIn(-0.85, 0.85)
    return if (t >= 0) lerp(HeatNeutral, HeatStrongGreen, t.toFloat())
    else lerp(HeatNeutral, HeatStrongRed, (-t).toFloat())
}

/** Rút gọn tên ngành để vừa ô nhỏ. */
internal fun shortSectorName(name: String): String = when (name) {
    "Bất động sản" -> "BĐS"
    "Khu công nghiệp" -> "KCN"
    "Thực phẩm & Đồ uống" -> "Thực phẩm"
    "Vận tải & Cảng biển" -> "Vận tải"
    "Hóa chất & Phân bón" -> "Hóa chất"
    "Xây dựng & VLXD" -> "Xây dựng"
    "Điện & Năng lượng" -> "Điện"
    else -> name
}

/**
 * Bản đồ nhiệt ngành: diện tích ô theo số mã, màu theo điểm trung bình.
 */
@Composable
fun SectorHeatmap(
    sectors: List<SectorScore>,
    selected: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 260.dp
) {
    if (sectors.isEmpty()) return
    val colors = AppTheme.colors

    val shown = sectors.filter { it.symbolCount >= MIN_SYMBOLS_FOR_TILE }
        .ifEmpty { sectors }
    val hidden = sectors - shown.toSet()

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Bản đồ ngành",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textSecondary
            )
            HeatLegend()
        }
        Spacer(Modifier.height(8.dp))

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(12.dp))
        ) {
            val w = maxWidth.value
            val h = maxHeight.value
            val tiles = remember(shown, w, h) {
                val weights = shown.map { it.symbolCount.toDouble().pow(SIZE_DAMPING) }
                Treemap.layout(weights, w, h)
            }

            tiles.forEach { tile ->
                val sector = shown[tile.index]
                SectorTile(
                    sector = sector,
                    isSelected = sector.sector == selected,
                    tileWidth = tile.width,
                    tileHeight = tile.height,
                    onClick = { onSelect(sector.sector) },
                    modifier = Modifier
                        .offset(x = tile.x.dp, y = tile.y.dp)
                        .size(width = tile.width.dp, height = tile.height.dp)
                )
            }
        }

        Spacer(Modifier.height(6.dp))
        Text(
            buildString {
                append(
                    if (selected != null) "Đang lọc theo ngành $selected — bấm lại để bỏ lọc."
                    else "Ô càng lớn = ngành càng nhiều mã. Bấm một ô để lọc danh sách bên dưới."
                )
                if (hidden.isNotEmpty()) {
                    append(" Không hiện ngành dưới $MIN_SYMBOLS_FOR_TILE mã: ")
                    append(hidden.joinToString(", ") { it.sector })
                    append(".")
                }
            },
            fontSize = 10.sp,
            color = colors.textSecondary
        )
    }
}

@Composable
private fun SectorTile(
    sector: SectorScore,
    isSelected: Boolean,
    tileWidth: Float,
    tileHeight: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Đo trên kích thước ô THẬT, không phải vùng còn lại sau padding —
    // nếu đo sau padding thì ô vừa đủ chỗ vẫn bị coi là chật và mất tên ngành.
    val roomy = tileWidth >= ROOMY_W && tileHeight >= ROOMY_H
    val medium = tileWidth >= MEDIUM_W && tileHeight >= MEDIUM_H

    Box(modifier = modifier.padding(1.dp)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(6.dp))
                .background(sectorHeatColor(sector.averageScore))
                .then(
                    if (isSelected) Modifier.border(
                        BorderStroke(2.dp, Color.White),
                        RoundedCornerShape(6.dp)
                    ) else Modifier
                )
                .clickable(onClick = onClick)
                .padding(horizontal = 3.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    shortSectorName(sector.sector),
                    fontSize = if (roomy) 12.sp else if (medium) 10.sp else 8.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    lineHeight = if (roomy) 14.sp else 11.sp
                )
                Text(
                    "${sector.averageScore.toInt()}",
                    fontSize = if (roomy) 22.sp else if (medium) 16.sp else 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (roomy) {
                    Text(
                        "${sector.symbolCount} mã",
                        fontSize = 9.sp,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
            }
        }
    }
}

/** Dải màu chú thích: đỏ (yếu) → xám (trung tính 50) → xanh (khỏe). */
@Composable
private fun HeatLegend() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Yếu", fontSize = 9.sp, color = AppTheme.colors.textSecondary)
        Spacer(Modifier.width(4.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(3.dp))
                .height(8.dp)
                .width(72.dp)
        ) {
            listOf(15, 30, 45, 55, 70, 85).forEach { s ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(sectorHeatColor(s.toDouble()))
                )
            }
        }
        Spacer(Modifier.width(4.dp))
        Text("Khỏe", fontSize = 9.sp, color = AppTheme.colors.textSecondary)
    }
}
