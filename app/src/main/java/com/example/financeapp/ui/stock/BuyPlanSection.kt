package com.example.financeapp.ui.stock

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.financeapp.domain.stock.BuyPlan
import com.example.financeapp.domain.stock.Horizon
import com.example.financeapp.domain.stock.MarketRegime
import com.example.financeapp.domain.stock.StockAnalysis
import com.example.financeapp.ui.theme.AppTheme
import com.example.financeapp.viewModel.BuyPlanViewModel
import com.example.financeapp.viewModel.StockViewTab
import java.text.DecimalFormat

private val priceFormat = DecimalFormat("#,###")

private fun scoreColor(score: Double): Color = when {
    score >= 75 -> PositiveGreen
    score >= 60 -> Color(0xFF8BC34A)
    score >= 45 -> WarningOrange
    else -> NegativeRed
}

private fun regimeColor(regime: MarketRegime): Color = when (regime) {
    MarketRegime.RISK_ON -> PositiveGreen
    MarketRegime.NEUTRAL -> WarningOrange
    MarketRegime.RISK_OFF -> NegativeRed
}

/**
 * Bảng xếp hạng kế hoạch mua theo khung 3 ngày / 7 ngày.
 *
 * Dữ liệu lấy trực tiếp từ API công khai của VNDirect và SSI, độc lập với backend
 * nội bộ — nên phần này vẫn chạy khi các mục khác của tab Stock lỗi kết nối.
 */
@Composable
fun BuyPlanSection(
    modifier: Modifier = Modifier,
    viewModel: BuyPlanViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = AppTheme.colors

    LaunchedEffect(Unit) { viewModel.load() }

    // Chỉ chạy vòng làm mới khi khối này còn trên màn hình và đang ở tab bảng giá,
    // tránh việc ViewModel vẫn gọi mạng sau khi người dùng chuyển sang tab khác.
    LaunchedEffect(state.tab) {
        if (state.tab == StockViewTab.LIVE) viewModel.startBoardUpdates()
    }
    DisposableEffect(Unit) {
        onDispose { viewModel.stopBoardUpdates() }
    }

    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Sàng lọc kế hoạch mua",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    "Phân tích kỹ thuật tự động, không phải khuyến nghị đầu tư",
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
            }
            IconButton(
                onClick = {
                    if (state.tab == StockViewTab.LIVE) viewModel.refreshBoard()
                    else viewModel.load(forceRefresh = true)
                },
                enabled = !state.isLoading && !state.isBoardLoading
            ) {
                Icon(Icons.Default.Refresh, "Tải lại", tint = AccentBlue)
            }
        }

        Spacer(Modifier.height(12.dp))

        state.result?.let { result ->
            MarketRegimeBanner(result.context.regime, result.context.indexClose)
            Spacer(Modifier.height(12.dp))
        }

        TabSelector(state.tab) { viewModel.setTab(it) }
        Spacer(Modifier.height(12.dp))

        when {
            state.tab == StockViewTab.LIVE -> {
                LiveBoardTable(
                    board = state.board,
                    quotes = state.sortedQuotes,
                    sort = state.boardSort,
                    onSortChange = { viewModel.setBoardSort(it) },
                    isLoading = state.isBoardLoading,
                    error = state.boardError
                )
            }

            state.isLoading && state.result == null -> LoadingBox(colors.textSecondary)

            state.error != null && state.result == null -> ErrorBox(state.error!!) {
                viewModel.load(forceRefresh = true)
            }

            state.result != null -> {
                val result = state.result!!
                SectorHeatmap(
                    sectors = state.sectors,
                    selected = state.selectedSector,
                    onSelect = { viewModel.selectSector(it) }
                )
                Spacer(Modifier.height(16.dp))

                val stocks = state.rankedStocks.take(10)
                if (stocks.isEmpty()) {
                    Text(
                        "Không có mã nào đạt tiêu chí lọc.",
                        fontSize = 13.sp,
                        color = colors.textSecondary
                    )
                } else {
                    stocks.forEachIndexed { index, stock ->
                        StockPlanCard(
                            rank = index + 1,
                            stock = stock,
                            horizon = state.horizon,
                            expanded = state.expandedSymbol == stock.symbol,
                            onToggle = { viewModel.toggleExpanded(stock.symbol) }
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                }

                Spacer(Modifier.height(8.dp))
                Disclaimer(result.analyses.size, result.skipped.values.sum())
            }
        }
    }
}

@Composable
private fun LoadingBox(textColor: Color) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = AccentBlue, strokeWidth = 2.dp)
        Spacer(Modifier.height(12.dp))
        Text("Đang quét thị trường...", fontSize = 12.sp, color = textColor)
    }
}

@Composable
private fun ErrorBox(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NegativeRed.copy(alpha = 0.12f))
            .padding(16.dp)
    ) {
        Text(message, fontSize = 13.sp, color = NegativeRed)
        Spacer(Modifier.height(8.dp))
        Text(
            "Thử lại",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = AccentBlue,
            modifier = Modifier.clickable(onClick = onRetry)
        )
    }
}

@Composable
private fun MarketRegimeBanner(regime: MarketRegime, indexClose: Double) {
    val color = regimeColor(regime)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "VNINDEX ${priceFormat.format(indexClose)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary
            )
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(color)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(regime.label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(regime.note, fontSize = 11.sp, color = AppTheme.colors.textSecondary)
    }
}

@Composable
private fun TabSelector(selected: StockViewTab, onSelect: (StockViewTab) -> Unit) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(colors.cardBackground)
            .padding(4.dp)
    ) {
        StockViewTab.entries.forEach { h ->
            val active = h == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (active) AccentBlue else Color.Transparent)
                    .clickable { onSelect(h) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    h.label,
                    fontSize = 13.sp,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                    color = if (active) Color.White else colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun StockPlanCard(
    rank: Int,
    stock: StockAnalysis,
    horizon: Horizon,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    val colors = AppTheme.colors
    val plan = stock.planFor(horizon)
    val sc = scoreColor(plan.score)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.cardBackground)
            .clickable(onClick = onToggle)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "$rank",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textSecondary,
                modifier = Modifier.width(22.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(stock.symbol, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                Text(stock.sector, fontSize = 11.sp, color = colors.textSecondary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    priceFormat.format(stock.price),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
                Text(
                    String.format("%+.2f%%", stock.changePercent),
                    fontSize = 11.sp,
                    color = if (stock.changePercent >= 0) PositiveGreen else NegativeRed
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${plan.score.toInt()}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = sc
                )
                Text("điểm", fontSize = 9.sp, color = colors.textSecondary)
            }
            Icon(
                if (expanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.textSecondary
            )
        }

        Spacer(Modifier.height(8.dp))
        ScoreBar(plan.score, sc)
        Spacer(Modifier.height(4.dp))
        Text(
            "Mức đồng thuận chỉ báo: ${plan.confidenceLabel}",
            fontSize = 11.sp,
            color = colors.textSecondary
        )

        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                HorizontalDivider(color = colors.divider)
                Spacer(Modifier.height(10.dp))

                PlanLevels(plan)
                Spacer(Modifier.height(12.dp))

                if (plan.strengths.isNotEmpty()) {
                    Text("Điểm mạnh", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PositiveGreen)
                    Spacer(Modifier.height(4.dp))
                    plan.strengths.distinct().forEach {
                        Text("+  $it", fontSize = 12.sp, color = colors.textPrimary)
                    }
                    Spacer(Modifier.height(10.dp))
                }

                if (plan.weaknesses.isNotEmpty()) {
                    Text("Điểm yếu", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NegativeRed)
                    Spacer(Modifier.height(4.dp))
                    plan.weaknesses.distinct().forEach {
                        Text("−  $it", fontSize = 12.sp, color = colors.textPrimary)
                    }
                    Spacer(Modifier.height(10.dp))
                }

                Text("Chi tiết chấm điểm", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = colors.textSecondary)
                Spacer(Modifier.height(6.dp))
                plan.factors.forEach { f ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "${f.label}  (×${String.format("%.2f", f.weight)})",
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                        Text(
                            "${f.score.toInt()}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = scoreColor(f.score)
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
                Text(
                    "RSI ${stock.rsi?.let { String.format("%.1f", it) } ?: "—"}   ·   " +
                        "KL so với TB20 ×${stock.volumeRatio?.let { String.format("%.2f", it) } ?: "—"}   ·   " +
                        "GTGD ${priceFormat.format(stock.tradedValue / 1_000_000_000.0)} tỷ",
                    fontSize = 10.sp,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun ScoreBar(score: Double, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(5.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(AppTheme.colors.divider)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth((score / 100.0).toFloat().coerceIn(0f, 1f))
                .fillMaxHeight()
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
    }
}

@Composable
private fun PlanLevels(plan: BuyPlan) {
    val colors = AppTheme.colors
    Row(modifier = Modifier.fillMaxWidth()) {
        LevelCell("Vùng mua", "${priceFormat.format(plan.entryLow)}–${priceFormat.format(plan.entryHigh)}", AccentBlue, Modifier.weight(1.3f))
        LevelCell("Cắt lỗ", priceFormat.format(plan.stopLoss), NegativeRed, Modifier.weight(1f))
        LevelCell("Mục tiêu", priceFormat.format(plan.target), PositiveGreen, Modifier.weight(1f))
        LevelCell(
            "Lãi/Lỗ",
            plan.riskReward?.let { String.format("%.1f", it) } ?: "—",
            colors.textPrimary,
            Modifier.weight(0.7f)
        )
    }
}

@Composable
private fun LevelCell(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = AppTheme.colors.textSecondary)
        Spacer(Modifier.height(2.dp))
        Text(
            value,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun Disclaimer(analyzed: Int, skipped: Int) {
    Text(
        "Đã phân tích $analyzed mã, loại $skipped mã do thanh khoản thấp hoặc quá mua. " +
            "Điểm số là mức đồng thuận giữa các chỉ báo kỹ thuật trên dữ liệu quá khứ, " +
            "KHÔNG phải xác suất sinh lời và không phải khuyến nghị đầu tư. " +
            "Mọi quyết định mua bán là của bạn.",
        fontSize = 10.sp,
        color = AppTheme.colors.textSecondary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}
