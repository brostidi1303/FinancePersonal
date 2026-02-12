package com.example.financeapp.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financeapp.R
import com.example.financeapp.data.Transaction
import com.example.financeapp.ui.history.formatCurrency
import com.example.financeapp.ui.theme.AppTheme
import com.example.financeapp.viewModel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.*

data class WeekSpending(
    val weekNumber: Int,
    val amount: Double,
    val label: String
)

@Composable
fun MonthlySpendingCard(financeViewModel: FinanceViewModel) {
    val transactions = financeViewModel.transactions
    val colors = AppTheme.colors
    // Tính toán dữ liệu tháng hiện tại
    val monthlyData = remember(transactions.size) {
        calculateMonthlySpending(transactions.toList())
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.cardBackground)
            .padding(20.dp)
    ) {
        Column {
            // Header với percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Monthly Spending",
                    fontSize = 16.sp,
                    color = colors.textPrimary
                )

                // Percentage badge
                if (monthlyData.percentageChange != null) {
                    val isNegative = monthlyData.percentageChange < 0
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isNegative) GreenPositive.copy(alpha = 0.2f)
                                else RedNegative.copy(alpha = 0.2f)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painterResource(R.drawable.trending),
                                contentDescription = null,
                                tint = if (isNegative) GreenPositive else RedNegative,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${if (isNegative) "" else "+"}${String.format("%.0f", monthlyData.percentageChange)}%",
                                fontSize = 12.sp,
                                color = if (isNegative) GreenPositive else RedNegative,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Total amount
            Text(
                text = "${formatCurrency(monthlyData.currentMonthTotal)} đ",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Chart
            SpendingChart(weeklyData = monthlyData.weeklySpending)

            Spacer(modifier = Modifier.height(12.dp))

            // Week labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                monthlyData.weeklySpending.forEach { week ->
                    Text(
                        text = week.label,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

data class MonthlySpendingData(
    val currentMonthTotal: Double,
    val lastMonthTotal: Double,
    val percentageChange: Double?,
    val weeklySpending: List<WeekSpending>
)

fun calculateMonthlySpending(transactions: List<Transaction>): MonthlySpendingData {
    val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)

    // ✅ UPDATED: Parse chỉ date, không cần time
    fun parseTransactionDate(transaction: Transaction): Calendar? {
        val dateString = transaction.date

        // Case 1: "Hôm nay"
        if (dateString == "Hôm nay") {
            return Calendar.getInstance()
        }

        // Case 2: "dd/MM/yyyy"
        val formats = listOf(
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
            SimpleDateFormat("dd 'Th'MM", Locale("vi"))
        )

        for (format in formats) {
            try {
                val date = format.parse(dateString)
                if (date != null) {
                    return Calendar.getInstance().apply { time = date }
                }
            } catch (e: Exception) {
                continue
            }
        }
        return null
    }

    // Filter transactions tháng hiện tại (chỉ expense)
    val currentMonthTransactions = transactions.filter { transaction ->
        if (transaction.isIncome) return@filter false

        val transactionCal = parseTransactionDate(transaction)
        transactionCal != null &&
                transactionCal.get(Calendar.MONTH) == currentMonth &&
                transactionCal.get(Calendar.YEAR) == currentYear
    }

    // Tính tổng tháng này
    val currentMonthTotal = currentMonthTransactions.sumOf { it.amount }

    // Tính tổng tháng trước
    val lastMonth = if (currentMonth == 0) 11 else currentMonth - 1
    val lastMonthYear = if (currentMonth == 0) currentYear - 1 else currentYear

    val lastMonthTransactions = transactions.filter { transaction ->
        if (transaction.isIncome) return@filter false

        val transactionCal = parseTransactionDate(transaction)
        transactionCal != null &&
                transactionCal.get(Calendar.MONTH) == lastMonth &&
                transactionCal.get(Calendar.YEAR) == lastMonthYear
    }

    val lastMonthTotal = lastMonthTransactions.sumOf { it.amount }

    // Tính % thay đổi
    val percentageChange = if (lastMonthTotal > 0) {
        ((currentMonthTotal - lastMonthTotal) / lastMonthTotal) * 100
    } else if (currentMonthTotal > 0) {
        100.0  // Tháng trước = 0, tháng này > 0 => +100%
    } else {
        null  // Cả 2 tháng đều = 0
    }

    // Chia theo 4 tuần
    val weeklySpending = mutableListOf<WeekSpending>()

    val lastDayOfMonth = Calendar.getInstance().apply {
        set(Calendar.YEAR, currentYear)
        set(Calendar.MONTH, currentMonth)
        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
    }

    val daysInMonth = lastDayOfMonth.get(Calendar.DAY_OF_MONTH)
    val daysPerWeek = daysInMonth / 4

    for (week in 1..4) {
        val weekStart = (week - 1) * daysPerWeek + 1
        val weekEnd = if (week == 4) daysInMonth else week * daysPerWeek

        // Filter transactions trong tuần này
        val weekTransactions = currentMonthTransactions.filter { transaction ->
            val transactionCal = parseTransactionDate(transaction)
            if (transactionCal == null) return@filter false

            val dayOfMonth = transactionCal.get(Calendar.DAY_OF_MONTH)
            dayOfMonth in weekStart..weekEnd
        }

        val weekTotal = weekTransactions.sumOf { it.amount }

        weeklySpending.add(
            WeekSpending(
                weekNumber = week,
                amount = weekTotal,
                label = "Week $week"
            )
        )
    }

    return MonthlySpendingData(
        currentMonthTotal = currentMonthTotal,
        lastMonthTotal = lastMonthTotal,
        percentageChange = percentageChange,
        weeklySpending = weeklySpending
    )
}

@Composable
fun SpendingChart(weeklyData: List<WeekSpending>) {
    // Nếu không có data, hiển thị flat line ở giữa
    val data = if (weeklyData.isEmpty() || weeklyData.all { it.amount == 0.0 }) {
        listOf(0.5f, 0.5f, 0.5f, 0.5f)
    } else {
        val maxAmount = weeklyData.maxOf { it.amount }
        if (maxAmount == 0.0) {
            listOf(0.5f, 0.5f, 0.5f, 0.5f)
        } else {
            // Normalize về 0.2 - 0.8 để chart đẹp hơn
            weeklyData.map { week ->
                0.2f + (week.amount / maxAmount * 0.6f).toFloat()
            }
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        val width = size.width
        val height = size.height
        val spacing = width / (data.size - 1)

        val path = Path().apply {
            data.forEachIndexed { index, value ->
                val x = index * spacing
                val y = height - (value * height)

                if (index == 0) {
                    moveTo(x, y)
                } else {
                    val prevX = (index - 1) * spacing
                    val prevY = height - (data[index - 1] * height)

                    // Control points cho smooth Bezier curve
                    val controlX1 = prevX + spacing / 2
                    val controlY1 = prevY
                    val controlX2 = x - spacing / 2
                    val controlY2 = y

                    cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                }
            }
        }

        drawPath(
            path = path,
            color = PrimaryBlue,
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}