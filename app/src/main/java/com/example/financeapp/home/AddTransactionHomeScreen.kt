package com.example.financeapp.home

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financeapp.R
import com.example.financeapp.Category
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun AddTransactionHomeScreen(
    onDismiss: () -> Unit,
    onAddTransaction: (Double, Boolean, String) -> Unit
) {
    var amount by remember { mutableStateOf("0") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var note by remember { mutableStateOf("") }
    var isNoteEditing by remember { mutableStateOf(false) }
    var isIncome by remember { mutableStateOf(true) }

    // Khởi tạo với thời gian hiện tại
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val context = LocalContext.current

// Format để hiển thị
    val dateLabel = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate.time)
    val timeLabel = SimpleDateFormat("HH:mm", Locale.getDefault()).format(selectedDate.time)

    val categories = remember {
        listOf(
            Category(1, "Ăn uống", Icons.Default.Star, Color(0xFF5B7FFF)),
            Category(2, "Cà phê", Icons.Default.Star, Color(0xFF4A5568)),
            Category(3, "Mua sắm", Icons.Default.ShoppingCart, Color(0xFF4A5568)),
            Category(4, "Di chuyển", Icons.Default.Star, Color(0xFF4A5568)),
            Category(5, "Hóa đơn", Icons.Default.DateRange, Color(0xFF4A5568)),
            Category(6, "Giải trí", Icons.Default.Star, Color(0xFF4A5568)),
            Category(7, "Y tế", Icons.Default.Add, Color(0xFF4A5568)),
            Category(8, "Khác", Icons.Default.Share, Color(0xFF4A5568))
        )
    }

    // Date Picker Dialog
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

    // Time Picker Dialog
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
            true // true = 24h format
        ).apply {
            setOnDismissListener { showTimePicker = false }
            show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
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
                        tint = Color.White
                    )
                }
                Text(
                    text = "Thêm Giao dịch",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            // Amount Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SỐ TIỀN",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                BasicTextField(
                    value = amount,
                    onValueChange = { newValue ->
                        // Loại bỏ tất cả dấu chấm
                        val digitsOnly = newValue.replace(".", "")

                        // Chỉ cho phép nhập số và không quá 15 ký tự
                        if (digitsOnly.all { it.isDigit() } && digitsOnly.length <= 15) {
                            // Format lại với dấu chấm ngăn cách hàng nghìn
                            amount = if (digitsOnly.isEmpty()) {
                                "0"
                            } else {
                                digitsOnly.toLongOrNull()?.let { number ->
                                    String.format("%,d", number).replace(",", ".")
                                } ?: amount
                            }
                        }
                    },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    cursorBrush = SolidColor(PrimaryBlue),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(modifier = Modifier.weight(1f, fill = false)) {
                                if (amount.isEmpty() || amount == "0") {
                                    Text(
                                        text = "0",
                                        fontSize = 48.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                innerTextField()
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "đ",
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                )
            }

            // Date and Time Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Ô chọn Ngày
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBackground)
                        .clickable { showDatePicker = true } // Click để chọn ngày
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isToday(selectedDate)) "Hôm nay" else dateLabel,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }

                // Ô chọn Giờ
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardBackground)
                        .clickable { showTimePicker = true } // Click để chọn giờ
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.clock),
                            contentDescription = null,
                            tint = Color(0xFFFF8C42),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = timeLabel,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Note Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardBackground)
                    .clickable { isNoteEditing = true }
                    .padding(16.dp)
            ) {
                if (isNoteEditing || note.isNotEmpty()) {
                    TextField(
                        value = note,
                        onValueChange = { note = it },
                        placeholder = {
                            Text(
                                text = "Thêm ghi chú...",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
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
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Thêm ghi chú...",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

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
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    TextButton(onClick = { /* TODO */ }) {
                        Text(
                            text = "Chỉnh sửa",
                            color = PrimaryBlue,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(200.dp)
                ) {
                    items(categories) { category ->
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

                    // Tạo chuỗi ngày tháng để lưu vào List
                    val finalDateLabel = if (isToday(selectedDate)) {
                        "Hôm nay, $timeLabel" // Kết quả: "Hôm nay, 10:45"
                    } else {
                        "$dateLabel, $timeLabel" // Kết quả: "06/02/2026, 10:45"
                    }
                    // Truyền thêm finalDateLabel vào hàm
                    onAddTransaction(amountValue, isIncome, finalDateLabel)
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

fun isToday(calendar: Calendar): Boolean {
    val today = Calendar.getInstance()
    return calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            calendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
}