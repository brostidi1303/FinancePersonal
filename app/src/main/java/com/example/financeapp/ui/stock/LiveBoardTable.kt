package com.example.financeapp.ui.stock

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financeapp.data.market.BoardSort
import com.example.financeapp.data.market.LiveBoard
import com.example.financeapp.data.market.LiveQuote
import com.example.financeapp.data.market.PriceState
import com.example.financeapp.ui.theme.AppTheme
import java.text.DecimalFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

private val intFormat = DecimalFormat("#,###")

/** Màu theo quy ước bảng giá Việt Nam: tím trần, xanh lơ sàn, vàng tham chiếu. */
private val CeilingPurple = Color(0xFFD946EF)
private val FloorCyan = Color(0xFF22D3EE)
private val ReferenceYellow = Color(0xFFEAB308)

private fun priceColor(state: PriceState): Color = when (state) {
    PriceState.CEILING -> CeilingPurple
    PriceState.FLOOR -> FloorCyan
    PriceState.REFERENCE -> ReferenceYellow
    PriceState.UP -> PositiveGreen
    PriceState.DOWN -> NegativeRed
}

/** Rút gọn số lớn: 1.234.000.000 -> "1,2 tỷ". */
private fun compactValue(v: Double): String {
    val abs = kotlin.math.abs(v)
    return when {
        abs >= 1_000_000_000 -> String.format(Locale.US, "%.1f tỷ", v / 1_000_000_000)
        abs >= 1_000_000 -> String.format(Locale.US, "%.0f tr", v / 1_000_000)
        abs >= 1_000 -> String.format(Locale.US, "%.0fk", v / 1_000)
        else -> intFormat.format(v)
    }
}

private fun compactVolume(v: Double): String = when {
    v >= 1_000_000 -> String.format(Locale.US, "%.1fM", v / 1_000_000)
    v >= 1_000 -> String.format(Locale.US, "%.0fK", v / 1_000)
    else -> intFormat.format(v)
}

/**
 * Bảng giá toàn sàn HOSE trong phiên.
 *
 * Khác hai tab 3/7 ngày: đây là DỮ LIỆU THÔ đang diễn ra, không chấm điểm và
 * không dự báo — chỉ phản ánh những gì thị trường đang làm ngay lúc này.
 */
@Composable
fun LiveBoardTable(
    board: LiveBoard?,
    quotes: List<LiveQuote>,
    sort: BoardSort,
    onSortChange: (BoardSort) -> Unit,
    isLoading: Boolean,
    error: String?,
    initialRows: Int = 30,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    Column(modifier = modifier.fillMaxWidth()) {

        when {
            board == null && isLoading -> {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = AccentBlue, strokeWidth = 2.dp)
                    Spacer(Modifier.height(12.dp))
                    Text("Đang tải bảng giá...", fontSize = 12.sp, color = colors.textSecondary)
                }
            }

            board == null && error != null -> {
                Text(error, fontSize = 13.sp, color = NegativeRed)
            }

            board != null -> {
                // Các dòng này KHÔNG lazy (đang nằm trong một item của LazyColumn cha),
                // nên mặc định chỉ dựng initialRows; bấm xem thêm mới dựng hết 400+ dòng.
                var showAll by remember { mutableStateOf(false) }
                val shownCount = if (showAll) quotes.size else minOf(initialRows, quotes.size)

                MarketBreadth(board)
                Spacer(Modifier.height(12.dp))

                SortSelector(sort, onSortChange)
                Spacer(Modifier.height(10.dp))

                TableHeader()
                HorizontalDivider(color = colors.divider)

                quotes.take(shownCount).forEach { q ->
                    QuoteRow(q)
                    HorizontalDivider(color = colors.divider.copy(alpha = 0.4f))
                }

                if (quotes.isEmpty()) {
                    Text(
                        "Bảng giá đang trống.",
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else if (quotes.size > shownCount) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAll = true }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Xem toàn bộ ${quotes.size} mã",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AccentBlue
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    buildString {
                        append("Hiển thị $shownCount/${quotes.size} mã toàn sàn HOSE. ")
                        append(updatedAgo(board.updatedAt))
                        if (board.session.isLive) append(" · tự làm mới mỗi 20 giây")
                        else append(" · đã dừng tự làm mới vì phiên không mở")
                    },
                    fontSize = 10.sp,
                    color = colors.textSecondary
                )
                if (error != null) {
                    Text(
                        "Lần làm mới gần nhất lỗi: $error",
                        fontSize = 10.sp,
                        color = WarningOrange
                    )
                }
            }
        }
    }
}

private fun updatedAgo(updatedAt: Long): String {
    val secs = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - updatedAt)
    return when {
        secs < 5 -> "Vừa cập nhật"
        secs < 60 -> "Cập nhật $secs giây trước"
        else -> "Cập nhật ${secs / 60} phút trước"
    }
}

/** Độ rộng thị trường: bao nhiêu mã tăng / giảm / trần / sàn. */
@Composable
private fun MarketBreadth(board: LiveBoard) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.cardBackground)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "HOSE · ${board.session.label}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (board.session.isLive) PositiveGreen else colors.divider)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    if (board.session.isLive) "ĐANG MỞ" else "ĐÓNG",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (board.session.isLive) Color.White else colors.textSecondary
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            BreadthCell("Tăng", "${board.advancing}", PositiveGreen, Modifier.weight(1f))
            BreadthCell("Giảm", "${board.declining}", NegativeRed, Modifier.weight(1f))
            BreadthCell("Trần", "${board.ceilingCount}", CeilingPurple, Modifier.weight(1f))
            BreadthCell("Sàn", "${board.floorCount}", FloorCyan, Modifier.weight(1f))
        }

        Spacer(Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            BreadthCell(
                "Tổng GTGD", compactValue(board.totalTradedValue),
                colors.textPrimary, Modifier.weight(1f)
            )
            BreadthCell(
                "Khối ngoại ròng", compactValue(board.totalForeignNet),
                if (board.totalForeignNet >= 0) PositiveGreen else NegativeRed,
                Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BreadthCell(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = AppTheme.colors.textSecondary)
        Spacer(Modifier.height(2.dp))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun SortSelector(selected: BoardSort, onSelect: (BoardSort) -> Unit) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        BoardSort.entries.forEach { s ->
            val active = s == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (active) AccentBlue else colors.cardBackground)
                    .clickable { onSelect(s) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    s.label,
                    fontSize = 11.sp,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                    color = if (active) Color.White else colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun TableHeader() {
    val c = AppTheme.colors.textSecondary
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Mã", fontSize = 10.sp, color = c, modifier = Modifier.weight(1.0f))
        Text("Giá", fontSize = 10.sp, color = c, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
        Text("+/-%", fontSize = 10.sp, color = c, textAlign = TextAlign.End, modifier = Modifier.weight(0.9f))
        Text("KL", fontSize = 10.sp, color = c, textAlign = TextAlign.End, modifier = Modifier.weight(0.8f))
        Text("GTGD", fontSize = 10.sp, color = c, textAlign = TextAlign.End, modifier = Modifier.weight(0.9f))
        Text("NN ròng", fontSize = 10.sp, color = c, textAlign = TextAlign.End, modifier = Modifier.weight(1.1f))
    }
}

@Composable
private fun QuoteRow(q: LiveQuote) {
    val colors = AppTheme.colors
    val pc = priceColor(q.state)

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1.0f)) {
            Text(q.symbol, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            Text(
                shortSectorName(q.sector),
                fontSize = 9.sp,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            intFormat.format(q.price),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = pc,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
        Text(
            String.format(Locale.US, "%+.2f", q.changePercent),
            fontSize = 12.sp,
            color = pc,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.9f)
        )
        Text(
            compactVolume(q.volume),
            fontSize = 11.sp,
            color = colors.textSecondary,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.8f)
        )
        Text(
            compactValue(q.tradedValue),
            fontSize = 11.sp,
            color = colors.textSecondary,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(0.9f)
        )
        Text(
            compactValue(q.foreignNetValue),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = if (q.foreignNetValue > 0) PositiveGreen
            else if (q.foreignNetValue < 0) NegativeRed
            else colors.textSecondary,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.1f)
        )
    }
}
