package com.example.financeapp.ui.add

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financeapp.data.APP_CATEGORIES
import com.example.financeapp.data.Category
import com.example.financeapp.ui.home.CardBackground
import com.example.financeapp.ui.home.CategoryItem1
import com.example.financeapp.ui.home.DarkBackground
import com.example.financeapp.ui.home.PrimaryBlue
import com.example.financeapp.ui.theme.AppTheme
import com.example.financeapp.viewModel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun AddTransactionHomeScreen(
    onDismiss: () -> Unit,
    defaultIsIncome: Boolean = true,
    onAddTransaction: (Double, Category, Boolean, String, String, String) -> Unit,
    onEditCategories: () -> Unit = {},
    financeViewModel: FinanceViewModel  // ✅ THÊM: Nhận ViewModel
) {
    val colors = AppTheme.colors
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var note by remember { mutableStateOf("") }
    var isNoteEditing by remember { mutableStateOf(false) }
    var isIncome by remember { mutableStateOf(defaultIsIncome) }

    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // State cho Dialog thông báo lỗi
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val dateLabel = getDateDisplayText(selectedDate)

    // dateToSave: Để lưu vào database (luôn là "dd/MM/yyyy")
    val dateToSave = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        .format(selectedDate.time)

    val timeLabel = SimpleDateFormat("HH:mm", Locale.getDefault()).format(selectedDate.time)
// ✅ SỬA: Lấy categories từ ViewModel thay vì APP_CATEGORIES
    val categories = financeViewModel.categories
    // --- DIALOG THÔNG BÁO LỖI ---
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("Đồng ý", color = PrimaryBlue)
                }
            },
            title = { Text("Thiếu thông tin") },
            text = { Text(errorMessage) },
            containerColor = CardBackground,
            titleContentColor = Color.White,
            textContentColor = Color.White.copy(alpha = 0.8f)
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                showDatePicker = false
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setOnDismissListener { showDatePicker = false }
            show()
        }
    }

    if (showTimePicker) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedDate.set(Calendar.MINUTE, minute)
                showTimePicker = false
            },
            selectedDate.get(Calendar.HOUR_OF_DAY),
            selectedDate.get(Calendar.MINUTE),
            true
        ).apply {
            setOnDismissListener { showTimePicker = false }
            show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = colors.textPrimary
                    )
                }
                Text(
                    text = "Thêm Giao dịch",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            // Amount Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 15.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SỐ TIỀN",
                    fontSize = 14.sp,
                    color = colors.textSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                BasicTextField(
                    value = amount,
                    onValueChange = { newValue ->
                        val digitsOnly = newValue.replace(".", "")
                        if (digitsOnly.all { it.isDigit() } && digitsOnly.length <= 15) {
                            amount = if (digitsOnly.isEmpty()) ""
                            else digitsOnly.toLongOrNull()?.let {
                                String.format("%,d", it).replace(",", ".")
                            } ?: amount
                        }
                    },
                    textStyle = TextStyle(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    cursorBrush = SolidColor(PrimaryBlue),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                if (amount.isEmpty()) {
                                    Text(
                                        text = "500.000",
                                        fontSize = 48.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textSecondary,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                innerTextField()
                            }

                            Text(
                                text = "đ",
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        }
                    }
                )
            }

            // Date Selection - HIỂN THỊ THEO LOGIC MỚI
            SelectionCard(
                icon = Icons.Default.DateRange,
                iconColor = Color(0xFFE74C3C),
                title = "Ngày giao dịch",
                value = dateLabel,  // ✅ Sử dụng dateLabel đã được format
                onClick = { showDatePicker = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Time Selection
            SelectionCard(
                icon = Icons.Default.DateRange,
                iconColor = Color(0xFF3498DB),
                title = "Thời gian giao dịch",
                value = timeLabel,
                onClick = { showTimePicker = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.cardBackground)
                    .clickable { isNoteEditing = true }
                    .padding(8.dp)
            ) {
                TextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = {
                        Text(
                            text = "Thêm ghi chú...",
                            color = colors.textSecondary,
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        cursorColor = PrimaryBlue,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        color = colors.textPrimary
                    )
                )
            }

            // Category Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Danh mục",
                        fontSize = 16.sp,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    TextButton(onClick = onEditCategories) {
                        Text(
                            text = "Chỉnh sửa",
                            color = PrimaryBlue,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ✅ SỬA: Dùng categories từ ViewModel
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(200.dp)
                ) {
                    items(categories) { category ->  // ✅ Thay APP_CATEGORIES thành categories
                        CategoryItem1(
                            category = category,
                            isSelected = selectedCategory == category,
                            onClick = { selectedCategory = category }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Save Button
            Button(
                onClick = {
                    val amountValue = amount.replace(".", "").toDoubleOrNull() ?: 0.0

                    // ✅ SỬ DỤNG dateLabel ĐÃ ĐƯỢC FORMAT
                    val finalDateLabel = dateLabel
                    val finalTimeLabel = timeLabel

                    when {
                        amountValue <= 0 -> {
                            errorMessage = "Vui lòng nhập số tiền hợp lệ."
                            showErrorDialog = true
                        }
                        selectedCategory == null -> {
                            errorMessage = "Vui lòng chọn một danh mục."
                            showErrorDialog = true
                        }
                        else -> {
                            // Nếu mọi thứ ok, mới gọi callback
                            onAddTransaction(
                                amountValue,
                                selectedCategory!!,
                                isIncome,
                                dateToSave,  // ✅ "05/02/2026" thay vì "Thứ Năm"
                                timeLabel,
                                note
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Lưu",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// Các hàm helper giữ nguyên
fun getDateDisplayText(calendar: Calendar): String {
    val today = Calendar.getInstance()

    if (isToday(calendar)) {
        return "Hôm nay"
    }

    if (isInCurrentWeek(calendar, today)) {
        return getDayOfWeekName(calendar)
    }

    return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
}

fun isInCurrentWeek(calendar: Calendar, today: Calendar): Boolean {
    val startOfWeek = today.clone() as Calendar
    startOfWeek.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    startOfWeek.set(Calendar.HOUR_OF_DAY, 0)
    startOfWeek.set(Calendar.MINUTE, 0)
    startOfWeek.set(Calendar.SECOND, 0)
    startOfWeek.set(Calendar.MILLISECOND, 0)

    val endOfWeek = startOfWeek.clone() as Calendar
    endOfWeek.add(Calendar.DAY_OF_WEEK, 6)
    endOfWeek.set(Calendar.HOUR_OF_DAY, 23)
    endOfWeek.set(Calendar.MINUTE, 59)
    endOfWeek.set(Calendar.SECOND, 59)

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

fun isToday(calendar: Calendar): Boolean {
    val today = Calendar.getInstance()
    return calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
}

@Composable
fun SelectionCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.cardBackground)
            .clickable(onClick = onClick)
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
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = title,
                    fontSize = 16.sp,
                    color = colors.textPrimary
                )
            }

            Text(
                text = value,
                fontSize = 14.sp,
                color = colors.textSecondary
            )
        }
    }
}