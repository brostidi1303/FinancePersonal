package com.example.financeapp.ui.add

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financeapp.data.Category
import com.example.financeapp.data.Transaction
import com.example.financeapp.ui.home.CardBackground
import com.example.financeapp.ui.home.CategoryItem1
import com.example.financeapp.ui.home.DarkBackground
import com.example.financeapp.ui.home.PrimaryBlue
import com.example.financeapp.ui.theme.AppTheme.colors
import com.example.financeapp.viewModel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun EditTransactionScreen(
    transaction: Transaction,
    financeViewModel: FinanceViewModel,
    onDismiss: () -> Unit,
    onUpdate: (Transaction) -> Unit,
    onDelete: () -> Unit,
    onEditCategories: () -> Unit = {},
) {
    val context = LocalContext.current
    val categories = financeViewModel.categories

    // ── State khởi tạo từ dữ liệu transaction ──
    var amount by remember {
        mutableStateOf(
            transaction.amount.toLong()
                .let { String.format("%,d", it).replace(",", ".") }
        )
    }
    var selectedCategory by remember {
        mutableStateOf<Category?>(categories.find { it.name == transaction.title })
    }
    var note by remember { mutableStateOf(transaction.note) }

    // Parse date
    var selectedDate by remember {
        mutableStateOf(
            try {
                Calendar.getInstance().apply {
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .parse(transaction.date)?.let { time = it }
                    // Parse thêm giờ
                    val parts = transaction.time.split(":")
                    if (parts.size == 2) {
                        set(Calendar.HOUR_OF_DAY, parts[0].toInt())
                        set(Calendar.MINUTE, parts[1].toInt())
                    }
                }
            } catch (_: Exception) { Calendar.getInstance() }
        )
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val dateLabel = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate.time)
    val timeLabel = SimpleDateFormat("HH:mm", Locale.getDefault()).format(selectedDate.time)

    // ── DatePicker → mở TimePicker sau ──
    if (showDatePicker) {
        DatePickerDialog(
            context,
            { _, year, month, day ->
                val updated = selectedDate.clone() as Calendar
                updated.set(year, month, day)
                showDatePicker = false
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        updated.set(Calendar.HOUR_OF_DAY, hour)
                        updated.set(Calendar.MINUTE, minute)
                        selectedDate = updated
                    },
                    updated.get(Calendar.HOUR_OF_DAY),
                    updated.get(Calendar.MINUTE),
                    true
                ).show()
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setOnDismissListener { showDatePicker = false }
            show()
        }
    }

    // ── Error Dialog ──
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("Đồng ý", color = PrimaryBlue)
                }
            },
            title = { Text("Thiếu thông tin", color = Color.White) },
            text = { Text(errorMessage, color = Color.White.copy(alpha = 0.8f)) },
            containerColor = CardBackground
        )
    }

    // ── Delete Dialog ──
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xóa giao dịch", color = Color.White) },
            text = {
                Text(
                    "Bạn có chắc chắn muốn xóa giao dịch này?",
                    color = Color.White.copy(alpha = 0.8f)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("Xóa", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Hủy", color = PrimaryBlue)
                }
            },
            containerColor = CardBackground
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {

        // ══════════════════════════════════════
        //  HEADER
        // ══════════════════════════════════════
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Nút Hủy
            TextButton(onClick = onDismiss) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                "Chỉnh sửa Giao dịch",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

        // ══════════════════════════════════════
        //  NỘI DUNG
        // ══════════════════════════════════════
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── SỐ TIỀN ──
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Số tiền",
                        fontSize = 14.sp,
                        color = colors.textSecondary  // ✅ Sửa theme
                    )
                    Spacer(Modifier.height(8.dp))
                    BasicTextField(
                        value = amount,
                        onValueChange = { newValue ->
                            val digits = newValue.replace(".", "")
                            if (digits.all { it.isDigit() } && digits.length <= 15) {
                                amount = if (digits.isEmpty()) ""
                                else digits.toLongOrNull()?.let {
                                    String.format("%,d", it).replace(",", ".")
                                } ?: amount
                            }
                        },
                        textStyle = TextStyle(
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,  // ✅ Sửa theme
                            textAlign = TextAlign.Center
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        cursorBrush = SolidColor(PrimaryBlue),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            // ✅ THAY ĐỔI: Dùng Box thay vì Row để căn giữa toàn bộ
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                // ✅ Row này chỉ chứa số và "đ" - sát nhau
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (amount.isEmpty()) {
                                            Text(
                                                "0",
                                                fontSize = 42.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.textSecondary,  // ✅ Sửa theme
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                        innerTextField()
                                    }
                                    Text(
                                        "đ",
                                        fontSize = 42.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary,  // ✅ Sửa theme
                                        modifier = Modifier.offset(x = (-10).dp)  // ✅ Giảm từ 4.dp xuống 2.dp
                                    )
                                }
                            }
                        }
                    )
                }
            }

            // ── NGÀY & GIỜ ──
            item {
                EditInfoRow(
                    iconVector = Icons.Default.DateRange,
                    label = "Ngày & Giờ",
                    value = "$dateLabel $timeLabel",
                    onClick = { showDatePicker = true }
                )
            }

            // ── VÍ TIỀN ──
            item {
                EditInfoRow(
                    iconVector = Icons.Default.Settings,
                    label = "Ví tiền",
                    value = "Ví tiền mặt",
                    onClick = {}
                )
            }

            // ── GHI CHÚ ──
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardBackground)
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            EditIconBox(Icons.Default.Edit)
                            Text(
                                "Ghi chú",
                                fontSize = 16.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        TextField(
                            value = note,
                            onValueChange = { note = it },
                            placeholder = {
                                Text(
                                    "Thêm ghi chú...",
                                    color = Color.White.copy(0.3f),
                                    fontSize = 14.sp
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = PrimaryBlue,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(
                                fontSize = 14.sp,
                                color = Color.White,
                                lineHeight = 20.sp
                            ),
                            minLines = 2
                        )
                    }
                }
            }

            // ── HẠNG MỤC ──
            item {
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
            }
        }
        // Action Buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Edit Button
            Button(
                onClick = {
                    val amountValue = amount.replace(".", "").toDoubleOrNull() ?: 0.0
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
                            onUpdate(
                                transaction.copy(
                                    title = selectedCategory!!.name,
                                    amount = amountValue,
                                    icon = selectedCategory!!.icon,
                                    iconColor = selectedCategory!!.color,
                                    date = dateLabel,
                                    time = timeLabel,
                                    note = note
                                )
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cập nhật",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Delete, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Xóa giao dịch này", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ══════════════════════════════════════
//  COMPONENTS
// ══════════════════════════════════════

@Composable
private fun EditIconBox(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF2A3050)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color.White.copy(0.8f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun EditCategoryItem(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .then(
                if (isSelected)
                    Modifier.border(2.dp, PrimaryBlue, RoundedCornerShape(16.dp))
                else Modifier
            )
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isSelected) category.color.copy(0.2f)
                    else Color.White.copy(0.08f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = category.icon),
                contentDescription = null,
                tint = if (isSelected) category.color else Color.White.copy(0.55f),
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = category.name,
            fontSize = 12.sp,
            color = if (isSelected) Color.White else Color.White.copy(0.55f),
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 15.sp
        )
    }
}

@Composable
fun EditInfoRow(
    iconVector: ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EditIconBox(iconVector)
                Text(
                    label,
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(value, fontSize = 14.sp, color = Color.White.copy(0.6f))
                Icon(
                    Icons.Default.Settings,
                    contentDescription = null,
                    tint = Color.White.copy(0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}