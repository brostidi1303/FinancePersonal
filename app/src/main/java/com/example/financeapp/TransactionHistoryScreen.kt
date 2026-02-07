package com.example.financeapp

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
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financeapp.data.Transaction
import com.example.financeapp.home.CardBackground
import com.example.financeapp.home.DarkBackground
import com.example.financeapp.home.GreenPositive
import com.example.financeapp.home.PrimaryBlue
import com.example.financeapp.viewModel.FinanceViewModel
import java.text.NumberFormat
import java.util.*

enum class FilterType {
    TIME, CATEGORY, ACCOUNT
}

@Composable
fun TransactionHistoryScreen(
    onBack: () -> Unit,
    financeViewModel: FinanceViewModel // NHẬN TRANSACTIONS TỪ MAINAPP
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTimeFilter by remember { mutableStateOf("Tháng này") }
    var selectedCategoryFilter by remember { mutableStateOf("Danh mục") }
    val transactions = financeViewModel.transactions
    // Nhóm transactions theo ngày
    val groupedTransactions = remember(transactions.toList()) {
        transactions.groupBy { it.date }
    }

    // Tính tổng chi tiêu và thu nhập
    val totalExpense = transactions.filter { !it.isIncome }.sumOf { it.amount }
    val totalIncome = transactions.filter { it.isIncome }.sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Header with back button
        HistoryHeader(onBack = onBack)

        // Search bar
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it }
        )

        // Filter chips
        FilterChipsRow(
            selectedTimeFilter = selectedTimeFilter,
            selectedCategoryFilter = selectedCategoryFilter,
            onTimeFilterClick = { /* Show time filter dialog */ },
            onCategoryFilterClick = { /* Show category filter dialog */ },
            onAccountFilterClick = { /* Show account filter dialog */ }
        )

        // Summary cards
        SummaryCards(
            totalExpense = totalExpense,
            totalIncome = totalIncome
        )

        // Transaction list grouped by date
        if (transactions.isEmpty()) {
            // Hiển thị khi không có giao dịch
            Box(
                modifier = Modifier
                    .fillMaxWidth()
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
                        text = "Chưa có giao dịch",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                groupedTransactions.forEach { (date, transactionsForDate) ->
                    item {
                        DateGroupHeader(dateGroup = date)
                    }

                    items(transactionsForDate) { transaction ->
                        HistoryTransactionItem(transaction)
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
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

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "Lịch sử Giao dịch",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = if (query.isEmpty()) "Tìm kiếm giao dịch" else query,
                fontSize = 15.sp,
                color = if (query.isEmpty()) Color.White.copy(alpha = 0.5f) else Color.White
            )
        }
    }
}

@Composable
fun FilterChipsRow(
    selectedTimeFilter: String,
    selectedCategoryFilter: String,
    onTimeFilterClick: () -> Unit,
    onCategoryFilterClick: () -> Unit,
    onAccountFilterClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FilterChip(
            text = selectedTimeFilter,
            isSelected = true,
            onClick = onTimeFilterClick
        )

        FilterChip(
            text = selectedCategoryFilter,
            isSelected = false,
            onClick = onCategoryFilterClick
        )
    }
}

@Composable
fun FilterChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) PrimaryBlue else CardBackground)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                color = Color.White
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun SummaryCards(
    totalExpense: Double,
    totalIncome: Double
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Total Expense Card
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(CardBackground)
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "TỔNG CHI TIÊU",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.6f),
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "- ${formatCurrency(kotlin.math.abs(totalExpense))} đ",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
            }
        }

        // Total Income Card
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(CardBackground)
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "TỔNG THU NHẬP",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.6f),
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "+ ${formatCurrency(totalIncome)} đ",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = GreenPositive
                )
            }
        }
    }
}

@Composable
fun DateGroupHeader(dateGroup: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = dateGroup,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun HistoryTransactionItem(transaction: Transaction) {
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

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaction.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = transaction.date,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "${if (transaction.isIncome) "+" else "-"} ${formatCurrency(kotlin.math.abs(transaction.amount))} đ",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (transaction.isIncome) GreenPositive else Color(0xFFFF6B6B)
            )
        }
    }
}

fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    return format.format(amount)
}