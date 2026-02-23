package com.example.financeapp.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.financeapp.data.APP_CATEGORIES
import com.example.financeapp.data.Category
import com.example.financeapp.data.Transaction
import com.example.financeapp.ui.home.CardBackground
import com.example.financeapp.ui.home.DarkBackground
import com.example.financeapp.ui.home.GreenPositive
import com.example.financeapp.ui.home.PrimaryBlue
import com.example.financeapp.ui.theme.AppTheme
import com.example.financeapp.ui.theme.AppTheme.colors
import com.example.financeapp.viewModel.FinanceViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@Composable
fun TransactionHistoryScreen(
    onBack: () -> Unit,
    financeViewModel: FinanceViewModel,
    onTransactionClick: (Transaction) -> Unit
) {
    val colors = AppTheme.colors
    var searchQuery by remember { mutableStateOf("") }
    var selectedTimeFilter by remember { mutableStateOf("Tháng này") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }

    // ✅ SỬA LỖI 1: Đổi tên biến để không bị trùng
    var showCategoryMenu by remember { mutableStateOf(false) }

    val transactions = financeViewModel.transactions

    val filteredTransactions = remember(transactions.toList(), searchQuery, selectedCategory) {
        var result = transactions.toList()

        if (searchQuery.isNotEmpty()) {
            result = result.filter { transaction ->
                transaction.title.contains(searchQuery, ignoreCase = true) ||
                        transaction.note.contains(searchQuery, ignoreCase = true) ||
                        transaction.amount.toString().contains(searchQuery)
            }
        }

        val category = selectedCategory
        if (category != null) {
            result = result.filter { transaction ->
                transaction.title == category.name
            }
        }

        result
    }

    val groupedTransactions = remember(filteredTransactions) {
        filteredTransactions.groupBy { it.date }
    }

    val totalExpense = filteredTransactions.filter { !it.isIncome }.sumOf { it.amount }
    val totalIncome = filteredTransactions.filter { it.isIncome }.sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        HistoryHeader(onBack = onBack)

        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it }
        )

        // ✅ SỬA LỖI 2: Bọc FilterChipsRow trong Box để DropdownMenu có anchor
        Box {
            FilterChipsRow(
                selectedTimeFilter = selectedTimeFilter,
                selectedCategory = selectedCategory,
                onTimeFilterClick = { /* Show time filter dialog */ },
                onCategoryFilterClick = {
                    showCategoryMenu = true  // ✅ Set thành true
                },
                onAccountFilterClick = { /* Show account filter dialog */ }
            )

            // ✅ DropdownMenu phải nằm trong cùng Box với anchor (FilterChipsRow)
            CategoryDropdownMenu(
                expanded = showCategoryMenu,
                onDismiss = { showCategoryMenu = false },
                selectedCategory = selectedCategory,
                onCategorySelected = { category ->
                    selectedCategory = category
                    showCategoryMenu = false  // ✅ Đóng menu sau khi chọn
                }
            )
        }

        SummaryCards(
            totalExpense = totalExpense,
            totalIncome = totalIncome
        )

        if (filteredTransactions.isEmpty()) {
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
                        tint = colors.textSecondary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = when {
                            searchQuery.isNotEmpty() -> "Không tìm thấy kết quả"
                            selectedCategory != null -> "Không có giao dịch trong danh mục này"
                            else -> "Chưa có giao dịch"
                        },
                        color = colors.textSecondary,
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
                            DateGroupHeader(
                                dateGroup = date,
                                time = transactionsForDate.firstOrNull()?.time ?: ""
                            )
                        }
                    }

                    items(transactionsForDate) { transaction ->
                        HistoryTransactionItem(
                            transaction,
                            onClick = { onTransactionClick(transaction) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    selectedCategory: Category?,
    onCategorySelected: (Category?) -> Unit
) {
    val colors = AppTheme.colors
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .width(250.dp)
            .background(colors.cardBackground)
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
    ) {
        // Option: Tất cả danh mục
        DropdownMenuItem(
            text = {
                Text("Tất cả danh mục", color = colors.textPrimary)
            },
            leadingIcon = {
                Icon(Icons.Default.List, null, tint = colors.textPrimary)
            },
            trailingIcon = {
                if (selectedCategory == null) {
                    Icon(Icons.Default.Check, null, tint = PrimaryBlue)
                }
            },
            onClick = {
                onCategorySelected(null)
            }
        )

        Divider(color = colors.divider)

        // Danh sách Category
        APP_CATEGORIES.forEach { category ->
            DropdownMenuItem(
                text = {
                    Text(category.name, color = colors.textPrimary)
                },
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(category.color.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = category.icon),
                            contentDescription = null,
                            tint = category.color,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                trailingIcon = {
                    if (selectedCategory?.id == category.id) {
                        Icon(Icons.Default.Check, null, tint = PrimaryBlue)
                    }
                },
                onClick = {
                    onCategorySelected(category)
                }
            )
        }
    }
}

@Composable
fun HistoryHeader(onBack: () -> Unit) {
    val colors = AppTheme.colors
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
        Text(
            text = "Lịch sử giao dịch",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )
        Spacer(modifier = Modifier.width(48.dp))
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    val colors = AppTheme.colors
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
                color = colors.textSecondary
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = colors.textSecondary,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = colors.cardBackground,
            unfocusedContainerColor = colors.cardBackground,
            focusedTextColor = colors.textPrimary,
            unfocusedTextColor = colors.textPrimary,
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
    selectedCategory: Category?,
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
            text = selectedCategory?.name ?: "Danh mục",
            isSelected = selectedCategory != null,
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
    val colors = AppTheme.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) PrimaryBlue else colors.cardBackground)
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
                color = colors.textSecondary
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = colors.textSecondary,
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
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(colors.cardBackground)
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "TỔNG CHI TIÊU",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary,
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

        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(colors.cardBackground)
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "TỔNG THU NHẬP",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary,
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
    // ✅ Tính toán lại format từ dd/MM/yyyy
    val displayDate = getDisplayDateFromStored(dateGroup)
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = displayDate,  // ✅ "Hôm nay" hoặc "Thứ Hai" hoặc "05/02/2026"
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )
        Text(
            text = time,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )
    }
}

@Composable
fun HistoryTransactionItem(
    transaction: Transaction,
    onClick: () -> Unit = {}
) {
    val colors = AppTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.cardBackground)
            .padding(16.dp)
            .clickable(onClick = onClick)
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
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = transaction.time,
                            fontSize = 13.sp,
                            color = colors.textSecondary,
                            maxLines = 1
                        )

                        if (transaction.note.isNotEmpty()) {
                            Text(
                                text = "•",
                                fontSize = 13.sp,
                                color = colors.textSecondary
                            )
                            Text(
                                text = transaction.note,
                                fontSize = 13.sp,
                                color = colors.textSecondary,
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

// ✅ HÀM MỚI: Parse và format lại date
fun getDisplayDateFromStored(storedDate: String): String {
    val today = Calendar.getInstance()

    // Parse "dd/MM/yyyy" thành Calendar
    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = sdf.parse(storedDate)

        if (date != null) {
            val calendar = Calendar.getInstance().apply { time = date }

            // Kiểm tra hôm nay
            if (isToday(calendar)) {
                return "Hôm nay"
            }

            // Kiểm tra trong tuần hiện tại
            if (isInCurrentWeek(calendar, today)) {
                return getDayOfWeekName(calendar)
            }

            // Ngoài tuần → Trả về dd/MM/yyyy
            storedDate
        } else {
            storedDate
        }
    } catch (e: Exception) {
        // Nếu parse lỗi, trả về nguyên bản
        storedDate
    }
}

// ✅ Copy các hàm helper
fun isToday(calendar: Calendar): Boolean {
    val today = Calendar.getInstance()
    return calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
}

fun isInCurrentWeek(calendar: Calendar, today: Calendar): Boolean {
    // Lấy ngày đầu tuần (Thứ Hai)
    val startOfWeek = today.clone() as Calendar
    startOfWeek.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    startOfWeek.set(Calendar.HOUR_OF_DAY, 0)
    startOfWeek.set(Calendar.MINUTE, 0)
    startOfWeek.set(Calendar.SECOND, 0)
    startOfWeek.set(Calendar.MILLISECOND, 0)

    // Lấy ngày cuối tuần (Chủ Nhật)
    val endOfWeek = startOfWeek.clone() as Calendar
    endOfWeek.add(Calendar.DAY_OF_WEEK, 6)
    endOfWeek.set(Calendar.HOUR_OF_DAY, 23)
    endOfWeek.set(Calendar.MINUTE, 59)
    endOfWeek.set(Calendar.SECOND, 59)

    // Kiểm tra calendar có nằm trong khoảng [startOfWeek, endOfWeek] không
    return calendar.timeInMillis >= startOfWeek.timeInMillis &&
            calendar.timeInMillis <= endOfWeek.timeInMillis
}

fun getDayOfWeekName(calendar: Calendar): String {
    return when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> "Thứ Hai"
        Calendar.TUESDAY -> "Thứ Ba"
        Calendar.WEDNESDAY -> "Thứ Tư"
        Calendar.THURSDAY -> "Thứ Năm"
        Calendar.FRIDAY -> "Thứ Sáu"
        Calendar.SATURDAY -> "Thứ Bảy"
        Calendar.SUNDAY -> "Chủ Nhật"
        else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
    }
}