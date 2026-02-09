package com.example.financeapp.ui.history

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financeapp.data.Transaction
import com.example.financeapp.ui.home.CardBackground
import com.example.financeapp.ui.home.DarkBackground
import com.example.financeapp.ui.home.GreenPositive
import com.example.financeapp.ui.home.PrimaryBlue
import com.example.financeapp.viewModel.FinanceViewModel
import java.text.NumberFormat
import java.util.*
import kotlin.math.abs

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

    // ✅ Lọc transactions theo searchQuery
    val filteredTransactions = remember(transactions.toList(), searchQuery) {
        if (searchQuery.isEmpty()) {
            transactions.toList()
        } else {
            transactions.filter { transaction ->
                transaction.title.contains(searchQuery, ignoreCase = true) ||
                        transaction.note.contains(searchQuery, ignoreCase = true) ||
                        transaction.amount.toString().contains(searchQuery)
            }
        }
    }

    // Nhóm transactions theo ngày
    val groupedTransactions = remember(filteredTransactions) {
        filteredTransactions.groupBy { it.date }  // ✅ Nhóm theo "Hôm nay" hoặc "dd/MM/yyyy"
    }

    // Tính tổng chi tiêu và thu nhập (dựa trên filteredTransactions)
    val totalExpense = filteredTransactions.filter { !it.isIncome }.sumOf { it.amount }
    val totalIncome = filteredTransactions.filter { it.isIncome }.sumOf { it.amount }

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
        if (filteredTransactions.isEmpty()) {
            // Hiển thị khi không có giao dịch (hoặc không tìm thấy kết quả)
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
                        text = if (searchQuery.isEmpty()) "Chưa có giao dịch" else "Không tìm thấy kết quả",
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                groupedTransactions.forEach { (date, transactionsForDate) ->
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,

                            ) {
                            DateGroupHeader(dateGroup = date, time = transactionsForDate.firstOrNull()?.time ?: "")
                        }
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
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp)),
        placeholder = {
            Text(
                text = "Tìm kiếm giao dịch",
                fontSize = 15.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = CardBackground,
            unfocusedContainerColor = CardBackground,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = PrimaryBlue,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        singleLine = true
    )
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
                    text = "- ${formatCurrency(abs(totalExpense))} đ",
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
fun DateGroupHeader(dateGroup: String, time: String) {
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
        Text(
            text = time,
            fontSize = 16.sp,
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
                        painter = painterResource(id = transaction.icon),
                        contentDescription = transaction.title,
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

                    // ✅ HIỂN THỊ TIME VÀ NOTE
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Time
                        Text(
                            text = transaction.time,
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.5f),
                            maxLines = 1
                        )

                        // Note (nếu có)
                        if (transaction.note.isNotEmpty()) {
                            Text(
                                text = "•",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                            Text(
                                text = transaction.note,
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.5f),
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "${if (transaction.isIncome) "+" else "-"} ${formatCurrency(abs(transaction.amount))} đ",
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