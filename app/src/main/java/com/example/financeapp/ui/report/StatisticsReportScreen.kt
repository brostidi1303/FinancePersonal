package com.example.financeapp.ui.report

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financeapp.data.APP_CATEGORIES
import com.example.financeapp.ui.history.formatCurrency
import com.example.financeapp.ui.home.CardBackground
import com.example.financeapp.ui.home.DarkBackground
import com.example.financeapp.ui.home.PrimaryBlue
import com.example.financeapp.viewModel.FinanceViewModel

data class CategoryExpense(
    val id: Int,
    val name: String,
    val amount: Double,
    val percentage: Float,
    val color: Color,
    val icon: Int
)

@Composable
fun StatisticsReportScreen(
    onBack: () -> Unit,
    financeViewModel: FinanceViewModel
) {
    var selectedMonth by remember { mutableStateOf("Tháng này") }

    val transactions = financeViewModel.transactions

    // ✅ Tính toán dữ liệu thật từ transactions
    val categoryExpenses = remember(transactions.toList()) {
        calculateCategoryExpenses(transactions.toList())
    }

    val totalExpense = categoryExpenses.sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Header
        StatisticsHeader(onBack = onBack)

        if (categoryExpenses.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Chưa có dữ liệu chi tiêu",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    // Total spending section
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Chi tiêu tháng này",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${formatCurrency(totalExpense)} đ",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Month selector
                        MonthSelector(
                            selectedMonth = selectedMonth,
                            onMonthChange = { selectedMonth = it }
                        )
                    }
                }

                item {
                    // Donut Chart
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        DonutChart(
                            categories = categoryExpenses,
                            modifier = Modifier.size(280.dp)
                        )
                    }
                }

                item {
                    // Category breakdown header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Chi tiết danh mục",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                items(categoryExpenses) { category ->
                    CategoryItem(category)
                }
            }
        }
    }
}

// ✅ HÀM TÍNH TOÁN CATEGORY EXPENSES TỪ TRANSACTIONS
fun calculateCategoryExpenses(transactions: List<com.example.financeapp.data.Transaction>): List<CategoryExpense> {
    // Lọc chỉ lấy expense (không phải income)
    val expenses = transactions.filter { !it.isIncome }

    if (expenses.isEmpty()) {
        return emptyList()
    }

    // Tính tổng chi tiêu
    val totalExpense = expenses.sumOf { it.amount }

    // Nhóm theo category (title)
    val groupedByCategory = expenses.groupBy { it.title }

    // Tạo CategoryExpense cho mỗi nhóm
    val categoryExpenses = groupedByCategory.map { (categoryName, transactionsInCategory) ->
        val categoryTotal = transactionsInCategory.sumOf { it.amount }
        val percentage = (categoryTotal / totalExpense).toFloat()

        // Tìm category info từ APP_CATEGORIES
        val categoryInfo = APP_CATEGORIES.find { it.name == categoryName }

        CategoryExpense(
            id = categoryInfo?.id ?: 0,
            name = categoryName,
            amount = categoryTotal,
            percentage = percentage,
            color = categoryInfo?.color ?: Color.Gray,
            icon = categoryInfo?.icon ?: com.example.financeapp.R.drawable.application
        )
    }

    // Sắp xếp theo amount giảm dần
    return categoryExpenses.sortedByDescending { it.amount }
}

@Composable
fun StatisticsHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = "Báo cáo Thống kê",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        IconButton(onClick = { /* Open calendar */ }) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Calendar",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun MonthSelector(
    selectedMonth: String,
    onMonthChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(CardBackground)
            .clickable { /* Open month picker */ }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = selectedMonth,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun DonutChart(
    categories: List<CategoryExpense>,
    modifier: Modifier = Modifier
) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = size.minDimension
            val radius = canvasSize / 2
            val strokeWidth = 60.dp.toPx()
            val centerX = size.width / 2
            val centerY = size.height / 2

            var startAngle = -90f

            categories.forEach { category ->
                val sweepAngle = 360f * category.percentage * animatedProgress.value

                drawArc(
                    color = category.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(
                        centerX - radius + strokeWidth / 2,
                        centerY - radius + strokeWidth / 2
                    ),
                    size = Size(
                        (radius - strokeWidth / 2) * 2,
                        (radius - strokeWidth / 2) * 2
                    ),
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round
                    )
                )

                startAngle += sweepAngle
            }
        }

        // Center text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "TỔNG CỘNG",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.6f),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "100%",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun CategoryItem(category: CategoryExpense) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(category.color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = category.icon),
                        contentDescription = null,
                        tint = category.color,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = category.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(category.color)
                        )
                        Text(
                            text = "${(category.percentage * 100).toInt()}%",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Text(
                text = "${formatCurrency(category.amount)} đ",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}