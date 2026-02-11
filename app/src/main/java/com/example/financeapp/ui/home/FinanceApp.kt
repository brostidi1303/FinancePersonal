package com.example.financeapp.ui.home

import android.util.Log
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financeapp.R
import com.example.financeapp.data.Category
import com.example.financeapp.data.Transaction
import com.example.financeapp.ui.add.AddTransactionHomeScreen
import com.example.financeapp.ui.history.formatCurrency
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
                defaultIsIncome = true,
                financeViewModel = financeViewModel,
                onAddTransaction = { amount, category, isIncome, date, time, note ->  // ✅ Tách date và time
                    val newTransaction = Transaction(
                        id = transactions.size + 1,
                        title = category.name,
                        date = date,           // ✅ Chỉ date: "Hôm nay" hoặc "dd/MM/yyyy"
                        time = time,           // ✅ Chỉ time: "HH:mm"
                        amount = amount,
                        icon = category.icon,
                        iconColor = category.color,
                        isIncome = isIncome,
                        note = note
                    )

                    financeViewModel.addTransaction(newTransaction)
                    showAddTransaction = false
                },
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
    val percentage = when {
        // ⭐ CASE ĐẶC BIỆT: totalBalance == initialBalance
        initialBalance > 0.0 && totalBalance == initialBalance -> {
            100.0  // ✅ Hiển thị +100%
        }
        // Chưa có gì
        initialBalance == 0.0 -> {
            0.0
        }
        // Tăng so với ban đầu
        totalBalance > initialBalance -> {
            ((totalBalance - initialBalance) / initialBalance * 100)
        }
        // Giảm so với ban đầu
        else -> {
            -((initialBalance - totalBalance) / initialBalance * 100)
        }
    }

    Log.d("BalanceCard", "totalBalance: $totalBalance, initialBalance: $initialBalance, percentage: $percentage")

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
                // Hiển thị phần trăm (chỉ khi có initial balance)
                if (initialBalance >= 0.0) {
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
                        .clip(CircleShape)
                        .background(transaction.iconColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = transaction.icon),
                        contentDescription = transaction.title,
                        tint = transaction.iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier,
                    verticalArrangement = Arrangement.SpaceEvenly
                ){
                    Text(
                        text = transaction.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    // ✅ HIỂN THỊ DATE VÀ TIME
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Note (nếu có)
                        if (transaction.note.isNotEmpty()) {
                            Text(
                                text = transaction.note,
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "•",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }

                        // Date
                        Text(
                            text = transaction.date,
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )

                        Text(
                            text = "•",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )

                        // Time
                        Text(
                            text = transaction.time,
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    }
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
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                // Khi được chọn, dùng màu của category làm nền. Khi không chọn, dùng màu tối của card.
                .background(if (isSelected) category.color else CardBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = category.icon),
                contentDescription = category.name,
                // QUAN TRỌNG: Khi nền đã có màu category, icon nên là màu Trắng để nổi bật.
                // Khi chưa chọn, icon có thể là màu xám hoặc trắng mờ.
                tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = category.name,
            fontSize = 11.sp,
            // Chữ cũng có thể đổi màu khi chọn để người dùng dễ nhận biết
            color = if (isSelected) category.color else Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            maxLines = 1,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}