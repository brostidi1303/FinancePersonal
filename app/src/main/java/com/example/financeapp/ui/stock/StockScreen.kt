package com.example.financeapp.ui.stock

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.financeapp.data.Anomaly
import com.example.financeapp.ui.theme.AppTheme
import com.example.financeapp.viewModel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin
private val WarningOrange = Color(0xFFF57C00)

// ─── Semantic Colors ───────────────────────────────────────────────────────
private val AccentBlue    = Color(0xFF2979FF)
private val AccentCyan    = Color(0xFF00BFFF)
private val PositiveGreen = Color(0xFF4FC3F7)
private val NegativeRed   = Color(0xFFEF5350)

// ─── Main Screen ───────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockScreen(viewModel: FinanceViewModel, onBack: () -> Unit) {
    val marketRegime by viewModel.marketRegime.collectAsStateWithLifecycle()
    val marketHistory by viewModel.marketRegimeHistory.collectAsStateWithLifecycle()
    val anomalies by viewModel.anomalies.collectAsStateWithLifecycle()
    val selectedAnomalyDate by viewModel.selectedAnomalyDate.collectAsStateWithLifecycle()
    val narrative by viewModel.narrative.collectAsStateWithLifecycle()
    val selectedNarrativeDate by viewModel.selectedNarrativeDate.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isHistoryLoading by viewModel.isHistoryLoading.collectAsStateWithLifecycle()
    val isAnomaliesLoading by viewModel.isAnomaliesLoading.collectAsStateWithLifecycle()
    val isNarrativeLoading by viewModel.isNarrativeLoading.collectAsStateWithLifecycle()

    // ✅ DatePicker state
    var showDatePickerAnomaly by remember { mutableStateOf(false) }
    var showDatePickerNarrative by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val colors = AppTheme.colors
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = AccentCyan)
        } else {
            marketRegime?.let { data ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = colors.textPrimary
                                )
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    "Phân tích Thị trường",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    "Cập nhật: ${data.date}",
                                    fontSize = 16.sp,
                                    color = colors.textSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(60.dp))
                        }
                    }

                    // ── Gauge ──────────────────────────────────────────
                    item {
                        GaugeSection(score = data.score.toFloat())
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // ── Regime Badge ───────────────────────────────────
                    item {
                        RegimeBadge(regime = data.regime)
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // ── Nhận định card ─────────────────────────────────
                    item {
                        NhanDinhCard(description = data.description)
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // ── Chi tiết thành phần ────────────────────────────
                    item {
                        SectionHeader("CHI TIẾT THÀNH PHẦN")
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    item {
                        ComponentRow(
                            icon = "📈",
                            label = "Xu hướng",
                            value = data.components.trend,
                            maxAbs = 100.0
                        )
                    }

                    item {
                        ComponentRow(
                            icon = "≡",
                            label = "Độ rộng",
                            value = data.components.breadth,
                            maxAbs = 100.0
                        )
                    }

                    item {
                        ComponentRow(
                            icon = "🌐",
                            label = "Khối ngoại",
                            value = data.components.foreign,
                            maxAbs = 100.0
                        )
                    }

                    item {
                        ComponentRow(
                            icon = "📉",
                            label = "Biến động",
                            value = data.components.volatility,
                            maxAbs = 100.0
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // ── Lịch sử 7 ngày ────────────────────────────────
                    item {
                        SectionHeader("LỊCH SỬ THỊ TRƯỜNG (7 NGÀY)")
                        Spacer(modifier = Modifier.height(12.dp))

                        if (isHistoryLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = AccentCyan,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        } else {
                            marketHistory?.let { history ->
                                // Lấy 7 ngày gần nhất và sắp xếp theo thứ tự tăng dần
                                val last7Days = history.history
                                    .sortedBy { it.date } // Sắp xếp theo ngày tăng dần
                                    .takeLast(7)

                                if (last7Days.isNotEmpty()) {
                                    val points = last7Days.map { it.score.toFloat() }
                                    val labels = last7Days.mapIndexed { index, item ->
                                        if (index == last7Days.size - 1) {
                                            "Hôm nay"
                                        } else {
                                            formatDateLabel(item.date)
                                        }
                                    }

                                    HistoryLineChart(points = points, labels = labels)
                                } else {
                                    // Hiển thị message nếu không có dữ liệu
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(160.dp)
                                            .padding(horizontal = 16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "Không có dữ liệu lịch sử",
                                            color = colors.textSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            } ?: run {
                                // Fallback nếu API chưa trả về
                                HistoryLineChart(
                                    points = listOf(data.score.toFloat()),
                                    labels = listOf("Hôm nay")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    // ✅ NEW: Tin hiệu bất thường với Date Picker ──────
                    item {
                        SectionHeader("TÍN HIỆU BẤT THƯỜNG (TOP 3)")
                        Spacer(modifier = Modifier.height(12.dp))
//                        // Date picker button
//                        Row(
//                            modifier = Modifier
//                                .clip(RoundedCornerShape(8.dp))
//                                .background(colors.cardBackground)
//                                .clickable { showDatePicker = true }
//                                .padding(horizontal = 10.dp, vertical = 6.dp),
//                            verticalAlignment = Alignment.CenterVertically,
//                            horizontalArrangement = Arrangement.spacedBy(6.dp)
//                        ) {
//                            Icon(
//                                imageVector = Icons.Default.DateRange,
//                                contentDescription = "Chọn ngày",
//                                tint = AccentBlue,
//                                modifier = Modifier.size(16.dp)
//                            )
//                            Text(
//                                text = if (selectedAnomalyDate != null) {
//                                    formatDisplayDate(selectedAnomalyDate!!)
//                                } else {
//                                    "Hôm nay"
//                                },
//                                fontSize = 12.sp,
//                                color = colors.textPrimary,
//                                fontWeight = FontWeight.Medium
//                            )
//                        }
//                        Spacer(modifier = Modifier.height(12.dp))
                        if (isAnomaliesLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = AccentCyan,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        } else {
                            anomalies?.let { anomaliesData ->
                                val top3 = anomaliesData.anomalies.take(3)

                                if (top3.isNotEmpty()) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // Hiển thị ngày của dữ liệu
                                        Text(
                                            text = "Dữ liệu ngày: ${formatDisplayDate(anomaliesData.date)}",
                                            fontSize = 11.sp,
                                            color = colors.textSecondary,
                                            fontWeight = FontWeight.Normal,
                                            modifier = Modifier.clip(RoundedCornerShape(8.dp))
                                            .background(colors.cardBackground)
                                            .clickable { showDatePickerAnomaly = true }
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        )

                                        top3.forEach { anomaly ->
                                            AnomalyCard(anomaly = anomaly)
                                        }
                                    }
                                } else {
                                    // Empty state
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column() {
                                            // Hiển thị ngày của dữ liệu
                                            Text(
                                                text = "Dữ liệu ngày: ${formatDisplayDate(anomaliesData.date)}",
                                                fontSize = 11.sp,
                                                color = colors.textSecondary,
                                                fontWeight = FontWeight.Normal,
                                                modifier = Modifier.clip(RoundedCornerShape(8.dp))
                                                    .background(colors.cardBackground)
                                                    .clickable { showDatePickerAnomaly = true }
                                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Surface(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp),
                                                color = colors.cardBackground,
                                                border = androidx.compose.foundation.BorderStroke(
                                                    1.dp,
                                                    colors.divider
                                                )
                                            ) {

                                                Column(
                                                    modifier = Modifier.padding(32.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Text(
                                                        "📭",
                                                        fontSize = 32.sp
                                                    )
                                                    Text(
                                                        "Không có tín hiệu bất thường",
                                                        color = colors.textPrimary,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    Text(
                                                        if (selectedAnomalyDate != null) {
                                                            "Ngày ${formatDisplayDate(selectedAnomalyDate!!)} không có dữ liệu"
                                                        } else {
                                                            "Không có dữ liệu cho ngày này"
                                                        },
                                                        color = colors.textSecondary,
                                                        fontSize = 12.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            } ?: run {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Đang tải dữ liệu...",
                                        color = colors.textSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    item {
                        // Truyền data và loading state vào UI
                        AiAnalysisSection(
                            narrative = narrative,
                            isLoading = isNarrativeLoading,
                            onClick = { showDatePickerNarrative = true }
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            } ?: Text(
                text = "Không có dữ liệu hoặc lỗi kết nối",
                color = NegativeRed
            )
        }

        // ✅ DatePicker Dialog
        if (showDatePickerAnomaly) {
            DatePickerDialog(
                onDismissRequest = { showDatePickerAnomaly = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val calendar = Calendar.getInstance().apply {
                                    timeInMillis = millis
                                }
                                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val targetDate = dateFormat.format(calendar.time)

                                // Call API với ngày được chọn
                                viewModel.fetchAnomalies(targetDate)
                            }
                            showDatePickerAnomaly = false
                        }
                    ) {
                        Text("Xác nhận", color = AccentBlue)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            // Reset về hôm nay
                            viewModel.resetAnomaliesDate()
                            showDatePickerAnomaly = false
                        }
                    ) {
                        Text("Hôm nay", color = colors.textSecondary)
                    }
                }
            ) {
                DatePicker(
                    state = datePickerState,
                    colors = DatePickerDefaults.colors(
                        containerColor = colors.cardBackground,
                        titleContentColor = colors.textPrimary,
                        headlineContentColor = colors.textPrimary,
                        weekdayContentColor = colors.textSecondary,
                        subheadContentColor = colors.textPrimary,
                        yearContentColor = colors.textPrimary,
                        currentYearContentColor = AccentBlue,
                        selectedYearContentColor = Color.White,
                        selectedYearContainerColor = AccentBlue,
                        dayContentColor = colors.textPrimary,
                        selectedDayContentColor = Color.White,
                        selectedDayContainerColor = AccentBlue,
                        todayContentColor = AccentBlue,
                        todayDateBorderColor = AccentBlue
                    )
                )
            }
        }

        // ✅ DatePicker Dialog
        if (showDatePickerNarrative) {
            DatePickerDialog(
                onDismissRequest = { showDatePickerNarrative = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val calendar = Calendar.getInstance().apply {
                                    timeInMillis = millis
                                }
                                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val targetDate = dateFormat.format(calendar.time)

                                // Call API với ngày được chọn
                                viewModel.fetchNarrative(targetDate)
                            }
                            showDatePickerNarrative = false
                        }
                    ) {
                        Text("Xác nhận", color = AccentBlue)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            // Reset về hôm nay
                            viewModel.resetNarrativeDate()
                            showDatePickerNarrative = false
                        }
                    ) {
                        Text("Hôm nay", color = colors.textSecondary)
                    }
                }
            ) {
                DatePicker(
                    state = datePickerState,
                    colors = DatePickerDefaults.colors(
                        containerColor = colors.cardBackground,
                        titleContentColor = colors.textPrimary,
                        headlineContentColor = colors.textPrimary,
                        weekdayContentColor = colors.textSecondary,
                        subheadContentColor = colors.textPrimary,
                        yearContentColor = colors.textPrimary,
                        currentYearContentColor = AccentBlue,
                        selectedYearContentColor = Color.White,
                        selectedYearContainerColor = AccentBlue,
                        dayContentColor = colors.textPrimary,
                        selectedDayContentColor = Color.White,
                        selectedDayContainerColor = AccentBlue,
                        todayContentColor = AccentBlue,
                        todayDateBorderColor = AccentBlue
                    )
                )
            }
        }
    }
}

/**
 * Format date từ "2026-02-09" thành "09/02"
 */
private fun formatDateLabel(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}

/**
 * Format date từ "2026-02-09" thành "09/02/2026"
 */
private fun formatDisplayDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}

// ─── Gauge ─────────────────────────────────────────────────────────────────
@Composable
fun GaugeSection(score: Float) {
    val colors = AppTheme.colors
    val animScore by animateFloatAsState(
        targetValue = score,
        animationSpec = tween(1200, easing = EaseOutCubic),
        label = "gauge"
    )

    Box(
        // Căn dưới cùng ở giữa. Nhờ vậy Text sẽ tự động nằm ngay sát đáy của nửa hình tròn.
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier
            .width(220.dp)
            .padding(vertical = 12.dp)
    ) {
        // 1. Canvas chỉ chiếm tỉ lệ 2:1 (Rộng 2 - Cao 1) -> Vừa khít nửa vòng tròn
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f)
        ) {
            val strokeWidth = 18.dp.toPx()

            // Bounding box cho hình tròn ĐẦY ĐỦ.
            // Trừ đi strokeWidth để nét vẽ không bị cắt lẹm ở 2 cạnh
            val arcSize = size.width - strokeWidth
            val arcOffset = Offset(strokeWidth / 2f, strokeWidth / 2f)

            // Vẽ thanh nền (Track)
            drawArc(
                color = colors.divider,
                startAngle = 180f, // Bắt đầu từ bên trái
                sweepAngle = 180f, // Kéo dài nửa vòng (180 độ)
                useCenter = false,
                style = Stroke(strokeWidth, cap = StrokeCap.Round),
                size = Size(arcSize, arcSize), // Bắt buộc là hình vuông để ra cung tròn chuẩn
                topLeft = arcOffset
            )

            val sweep = (animScore / 100f).coerceIn(0f, 1f) * 180f

            // Tâm của Gradient phải là tâm của hình tròn ĐẦY ĐỦ (size.width / 2 cho cả X và Y)
            val gradient = Brush.sweepGradient(
                0.0f to Color(0xFF1565C0),
                0.3f to Color(0xFF1976D2),
                0.6f to AccentCyan,
                1.0f to AccentCyan,
                center = Offset(size.width / 2f, size.width / 2f)
            )

            // Vẽ thanh giá trị (Gradient)
            drawArc(
                brush = gradient,
                startAngle = 180f,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(strokeWidth, cap = StrokeCap.Round),
                size = Size(arcSize, arcSize),
                topLeft = arcOffset
            )
        }

        // 2. Column chứa Text
        // Do Box dùng BottomCenter, Column này đã ở ngay mép dưới.
        // Ta chỉ cần đẩy nó lùi lên trên 1 chút xíu bằng offset cho đẹp mắt.
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = (-8).dp)
        ) {
            Text(
                text = String.format("%.2f", score),
                fontSize = 38.sp,
                fontWeight = FontWeight.Black,
                color = colors.textPrimary
            )
            Text(
                text = "CHỈ SỐ",
                fontSize = 10.sp,
                letterSpacing = 2.sp,
                color = colors.textSecondary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ─── Regime Badge ──────────────────────────────────────────────────────────
@Composable
fun RegimeBadge(regime: String) {
    val (label, color) = when (regime.uppercase()) {
        "BULLISH"    -> "BULLISH (TĂNG GIÁ)"  to Color(0xFF43A047)
        "BEARISH"    -> "BEARISH (GIẢM GIÁ)"  to NegativeRed
        "TRANSITION" -> "TRANSITION (CHUYỂN ĐỔI)" to AccentBlue
        "NEUTRAL"    -> "NEUTRAL (TRUNG LẬP)" to Color(0xFFFFA726)
        else         -> regime.uppercase()      to Color(0xFF9E9E9E)
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(color, CircleShape)
            )
            Text(
                text = label,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                letterSpacing = 0.5.sp
            )
        }
    }
}

// ─── Nhận định Card ────────────────────────────────────────────────────────
@Composable
fun NhanDinhCard(description: String) {
    val colors = AppTheme.colors
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        color = colors.cardBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.divider)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = AccentBlue.copy(alpha = 0.2f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("ℹ", fontSize = 14.sp, color = AccentBlue)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Nhận định",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    description,
                    fontSize = 16.sp,
                    color = colors.textSecondary,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

// ─── Anomaly Card ──────────────────────────────────────────────────────────
@Composable
fun AnomalyCard(anomaly: Anomaly) {
    val colors = AppTheme.colors
    val confidence = (anomaly.confidence * 100).toInt()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colors.cardBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.divider)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Header: Symbol + Confidence
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = anomaly.symbol,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = AccentBlue.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "$confidence%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AccentBlue,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Headline
            Text(
                text = anomaly.headline,
                fontSize = 13.sp,
                color = colors.textPrimary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Details Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Vùng mua
                anomaly.details.entryZone?.let { zone ->
                    if (zone.size >= 2) {
                        DetailItem(
                            label = "VÙNG MUA",
                            value = "${String.format("%.2f", zone[0])} - ${
                                String.format(
                                    "%.2f",
                                    zone[1]
                                )
                            }",
                            color = colors.textSecondary
                        )
                    }
                }

                // Mục tiêu
                anomaly.details.target?.let { target ->
                    DetailItem(
                        label = "MỤC TIÊU",
                        value = String.format("%.2f", target),
                        color = PositiveGreen
                    )
                }

                // Dừng lỗ
                anomaly.details.stopLoss?.let { stopLoss ->
                    DetailItem(
                        label = "DỪNG LỖ",
                        value = String.format("%.2f", stopLoss),
                        color = NegativeRed
                    )
                }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            color = AppTheme.colors.textSecondary,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

// ─── AI Analysis Section (NEW UI) ──────────────────────────────────────────
@Composable
fun AiAnalysisSection(narrative: com.example.financeapp.data.NarrativeResponse?, isLoading: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors

    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader("NHẬN ĐỊNH THỊ TRƯỜNG (AI)")
        Spacer(modifier = Modifier.height(12.dp))
        // Hiển thị ngày của dữ liệu
        Box(modifier = Modifier.padding(start = 16.dp)){
            Text(
                text = "Dữ liệu ngày: ${formatDisplayDate(narrative!!.date)}",
                fontSize = 11.sp,
                color = colors.textSecondary,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.clip(RoundedCornerShape(8.dp))
                    .background(colors.cardBackground)
                    .clickable{
                        onClick()
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            color = colors.cardBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.divider)
        ) {
            if (isLoading) {
                // Hiệu ứng Loading
                Box(
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentCyan, modifier = Modifier.size(32.dp))
                }
            } else if (narrative != null) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // --- Dòng 1: Header (Icon AI + Badge Confidence) ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Cụm Icon & Text AI
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.Transparent,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("🧠", fontSize = 18.sp)
                                }
                            }
                            Text(
                                text = "AI Analysis",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                        }

                        // Xử lý logic màu sắc cho Confidence Badge
                        val (confText, confColor) = when (narrative.confidence.uppercase()) {
                            "HIGH" -> "HIGH CONFIDENCE" to PositiveGreen
                            "LOW" -> "LOW CONFIDENCE" to NegativeRed
                            else -> "MEDIUM CONFIDENCE" to WarningOrange
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = colors.background,
                            border = androidx.compose.foundation.BorderStroke(1.dp, confColor.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = confText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = confColor,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- Dòng 2: Nội dung nhận định AI ---
                    Text(
                        text = narrative.narrative,
                        fontSize = 15.sp,
                        color = colors.textPrimary,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = colors.divider, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // --- Dòng 3: Footer (Báo cáo liên quan) ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("📄", fontSize = 14.sp)
                            Text(
                                text = "Các bài báo liên quan: ${narrative.supportingArticles.size ?: 0}",
                                fontSize = 13.sp,
                                color = colors.textSecondary
                            )
                        }

                        Text(
                            text = "Xem tất cả >",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = AccentBlue,
                            modifier = Modifier.clickable { /* Xử lý chuyển trang báo */ }
                        )
                    }
                }
            } else {
                // Trạng thái trống (Không có data)
                Box(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Không có dữ liệu nhận định cho ngày này",
                        color = colors.textSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

// ─── Section Header ────────────────────────────────────────────────────────
@Composable
fun SectionHeader(title: String) {
    val colors = AppTheme.colors
    Text(
        text = title,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        color = colors.textSecondary,
        letterSpacing = 1.5.sp,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    )
}

// ─── Component Row ─────────────────────────────────────────────────────────
@Composable
fun ComponentRow(icon: String, label: String, value: Double, maxAbs: Double) {
    val colors = AppTheme.colors
    val ratio = (value.coerceIn(-maxAbs, maxAbs) / maxAbs).toFloat()
    val barColor = if (value >= 0) PositiveGreen else NegativeRed

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 16.dp),
        shape = RoundedCornerShape(10.dp),
        color = colors.cardBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.divider)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(icon, fontSize = 14.sp)
                    Text(label, fontSize = 15.sp, color = colors.textPrimary, fontWeight = FontWeight.Medium)
                }
                Text(
                    text = if (value % 1.0 == 0.0) value.toLong().toString() else String.format("%.2f", value),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = barColor
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .background(colors.divider, RoundedCornerShape(2.dp))
            ) {
                val barFraction = kotlin.math.abs(ratio).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(barFraction)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(listOf(barColor.copy(alpha = 0.5f), barColor)),
                            RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    }
}

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