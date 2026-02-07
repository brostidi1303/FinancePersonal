package com.example.financeapp.home

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.financeapp.Category
import com.example.financeapp.PreferencesManager
import com.example.financeapp.R
import com.example.financeapp.data.Transaction
import com.example.financeapp.formatCurrency
import com.example.financeapp.viewModel.FinanceViewModel

// Color scheme
val DarkBackground = Color(0xFF0A0E27)
val CardBackground = Color(0xFF1A1F3A)
val PrimaryBlue = Color(0xFF587EF1)
val LightBlue = Color(0xFF6B8DE3)
val GreenPositive = Color(0xFF00C48C)
val RedNegative = Color(0xFFFF6B6B)

@Composable
fun FinanceApp(
    onViewAll: () -> Unit,
    financeViewModel: FinanceViewModel
) {
    val context = LocalContext.current
    var showAddTransaction by remember { mutableStateOf(false) }

    // Collect state từ ViewModel
    val totalBalance by financeViewModel.totalBalance.collectAsState()
    val initialBalance by financeViewModel.initialBalance.collectAsState()
    val transactions = financeViewModel.transactions

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            item { HeaderSection() }
            item {
                BalanceCard(
                    totalBalance = totalBalance,
                    initialBalance = initialBalance,
                    onAddClick = { showAddTransaction = true }
                )
            }
            item { MonthlySpendingCard(financeViewModel = financeViewModel) }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Recent Transactions",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (transactions.isNotEmpty()) {
                        TextButton(onClick = { onViewAll() }) {
                            Text("View All", color = PrimaryBlue)
                        }
                    }
                }
            }

            if (transactions.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Record",
                            color = Color.Gray,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                items(transactions) { transaction ->
                    TransactionItem(transaction)
                }
            }
        }

        if (showAddTransaction) {
            AddTransactionHomeScreen(
                onDismiss = { showAddTransaction = false },
                onAddTransaction = { amount, isIncome, formattedDate -> // Nhận thêm formattedDate
                    // Tạo transaction mới với ngày giờ thực tế đã chọn
                    val newTransaction = Transaction(
                        id = transactions.size + 1,
                        title = if (isIncome) "Thu nhập" else "Chi tiêu",
                        date = formattedDate, // LƯU NGÀY GIỜ THỰC TẾ Ở ĐÂY
                        amount = amount,
                        icon = if (isIncome) Icons.Default.Add else Icons.Default.ShoppingCart,
                        iconColor = if (isIncome) GreenPositive else RedNegative,
                        isIncome = isIncome
                    )

                    // Lưu vào ViewModel/List của bạn
                    financeViewModel.addTransaction(newTransaction)

                    showAddTransaction = false
                }
            )
        }
    }
}

@Composable
fun HeaderSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8B4A0)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color(0xFF8B5E3C),
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Welcome back,",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Nguyen Tien Dat",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Box(
            modifier = Modifier
                .size(45.dp)
                .clip(CircleShape)
                .background(CardBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = Color.White
            )
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-8).dp, y = 8.dp)
                    .clip(CircleShape)
                    .background(RedNegative)
            )
        }
    }
}

@Composable
fun BalanceCard(
    totalBalance: Double,
    initialBalance: Double,
    onAddClick: () -> Unit
) {
    // LOGIC MỚI: Tính phần trăm thay đổi
    val percentage = when {
        // Trường hợp 1: Chưa có tiền ban đầu (initialBalance = 0)
        initialBalance == 0.0 -> {
            if (totalBalance > 0.0) {
                100.0  // ✅ Có tiền rồi → Hiển thị +100%
            } else {
                0.0    // Vẫn chưa có gì → 0%
            }
        }
        // Trường hợp 2: Đã có initial balance, tính % thay đổi
        totalBalance >= initialBalance -> {
            ((totalBalance - initialBalance) / initialBalance * 100)
        }
        // Trường hợp 3: Số dư giảm so với ban đầu
        else -> {
            -((initialBalance - totalBalance) / initialBalance * 100)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(PrimaryBlue, LightBlue)
                )
            )
            .padding(24.dp)
    ) {
        Column {
            Text(
                text = "Total Balance",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${formatCurrency(totalBalance)}đ",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hiển thị phần trăm (chỉ khi có totalBalance > 0)
                if (totalBalance > 0.0) {
                    Row(
                        modifier = Modifier
                            .width(80.dp)
                            .height(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.3f)),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.trending),
                            contentDescription = "Trend",
                            tint = if (percentage >= 0) GreenPositive else RedNegative,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${if (percentage >= 0) "+" else ""}${String.format("%.1f", percentage)}%",
                            fontSize = 14.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Các nút action
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onAddClick,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.3f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = { /* TODO: Implement send money */ },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.3f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlySpendingCard(financeViewModel: FinanceViewModel) {
    // Tính tổng chi tiêu trong tháng từ ViewModel
    val monthlyExpense = financeViewModel.getTotalExpense()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CardBackground)
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Monthly Spending",
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(RedNegative.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = RedNegative,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "-5%",
                            fontSize = 12.sp,
                            color = RedNegative,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${formatCurrency(monthlyExpense)} đ",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(20.dp))

            SpendingChart()

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("Week 1", "Week 2", "Week 3", "Week 4").forEach { week ->
                    Text(
                        text = week,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun SpendingChart() {
    val data = remember {
        listOf(0.4f, 0.7f, 0.5f, 0.6f, 0.3f, 0.5f, 0.4f, 0.8f, 0.3f, 0.5f)
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

@Composable
fun TransactionItem(transaction: Transaction) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(transaction.iconColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = transaction.icon,
                        contentDescription = null,
                        tint = transaction.iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = transaction.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Text(
                        text = transaction.date,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }

            Text(
                text = "${if (transaction.isIncome) "+" else ""}${formatCurrency(transaction.amount)} đ",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (transaction.isIncome) GreenPositive else Color.White
            )
        }
    }
}

@Composable
fun CategoryItem1(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(if (isSelected) category.color else CardBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.name,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = category.name,
            fontSize = 11.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}