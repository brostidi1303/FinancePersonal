package com.example.financeapp.ui.stock.componets

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financeapp.ui.stock.AccentBlue
import com.example.financeapp.ui.stock.AccentCyan
import com.example.financeapp.ui.theme.AppTheme

// ─── History Line Chart ────────────────────────────────────────────────────
@Composable
fun HistoryLineChart(points: List<Float>, labels: List<String>) {
    val colors = AppTheme.colors
    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1400, easing = EaseOutCubic),
        label = "chart_anim"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        color = colors.cardBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.divider)
    ) {
        Box(modifier = Modifier.padding(12.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (points.size < 2) return@Canvas

                val minY = points.min()
                val maxY = points.max()
                val rangeY = (maxY - minY).coerceAtLeast(1f)
                val stepX = size.width / (points.size - 1)
                val chartH = size.height * 0.75f

                fun xAt(i: Int) = i * stepX
                fun yAt(v: Float) = chartH - ((v - minY) / rangeY) * chartH

                val path = Path()
                val drawCount = (points.size * animProgress).toInt().coerceAtLeast(2)

                points.take(drawCount).forEachIndexed { i, v ->
                    if (i == 0) path.moveTo(xAt(i), yAt(v))
                    else {
                        val cx = (xAt(i - 1) + xAt(i)) / 2
                        path.cubicTo(cx, yAt(points[i - 1]), cx, yAt(v), xAt(i), yAt(v))
                    }
                }

                val fillPath = Path().apply {
                    addPath(path)
                    lineTo(xAt(drawCount - 1), chartH)
                    lineTo(0f, chartH)
                    close()
                }
                drawPath(
                    fillPath,
                    Brush.verticalGradient(
                        listOf(AccentBlue.copy(alpha = 0.35f), Color.Transparent),
                        endY = chartH
                    )
                )

                drawPath(
                    path,
                    brush = Brush.horizontalGradient(listOf(AccentBlue, AccentCyan)),
                    style = Stroke(3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                val lastX = xAt(drawCount - 1)
                val lastY = yAt(points[drawCount - 1])
                drawCircle(AccentCyan, 5.dp.toPx(), Offset(lastX, lastY))
                drawCircle(Color.White, 2.5.dp.toPx(), Offset(lastX, lastY))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                labels.forEach { lbl ->
                    Text(
                        text = lbl,
                        fontSize = 9.sp,
                        color = if (lbl == "Hôm nay") AccentCyan else colors.textSecondary,
                        fontWeight = if (lbl == "Hôm nay") FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}