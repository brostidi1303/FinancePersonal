//package com.example.financeapp
//
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyRow
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.input.KeyboardType
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.example.financeapp.data.APP_CATEGORIES
//import com.example.financeapp.data.Category
//import com.example.financeapp.data.Wallet
//import com.example.financeapp.home.CardBackground
//import com.example.financeapp.home.DarkBackground
//import com.example.financeapp.home.PrimaryBlue
//import java.text.SimpleDateFormat
//import java.util.*
//
//
//@Composable
//fun AddTransactionScreen(
//    onDismiss: () -> Unit,
//    onSave: (amount: Double, category: Category, date: String, wallet: Wallet) -> Unit
//) {
//    var amount by remember { mutableStateOf("150000") }
//    var selectedCategory by remember { mutableStateOf<Category?>(null) }
//    var showDatePicker by remember { mutableStateOf(false) }
//    var selectedDate by remember { mutableStateOf(getCurrentDate()) }
//    var showWalletPicker by remember { mutableStateOf(false) }
//    var selectedWallet by remember { mutableStateOf(getDefaultWallet()) }
//
//
//    // Set default category
//    LaunchedEffect(Unit) {
//        selectedCategory = APP_CATEGORIES.first()
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(DarkBackground)
//    ) {
//        Column(
//            modifier = Modifier.fillMaxSize()
//        ) {
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(20.dp),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                IconButton(onClick = onDismiss) {
//                    Icon(
//                        imageVector = Icons.Default.Close,
//                        contentDescription = "Close",
//                        tint = Color.White
//                    )
//                }
//                Text(
//                    text = "Thêm Giao dịch",
//                    fontSize = 20.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = Color.White
//                )
//                Spacer(modifier = Modifier.width(48.dp))
//            }
//
//            Spacer(modifier = Modifier.height(32.dp))
//
//            // Amount Input Field
//            Column(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Text(
//                    text = "Số tiền",
//                    fontSize = 16.sp,
//                    color = Color.White.copy(alpha = 0.6f)
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//
//                // Editable Amount TextField
//                OutlinedTextField(
//                    value = formatAmount(amount),
//                    onValueChange = { newValue ->
//                        // Chỉ cho phép nhập số
//                        val cleaned = newValue.replace(".", "").replace(",", "")
//                        if (cleaned.isEmpty()) {
//                            amount = "0"
//                        } else if (cleaned.all { it.isDigit() }) {
//                            amount = cleaned
//                        }
//                    },
//                    textStyle = TextStyle(
//                        fontSize = 48.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = Color.White,
//                        textAlign = TextAlign.Center
//                    ),
//                    keyboardOptions = KeyboardOptions(
//                        keyboardType = KeyboardType.Number
//                    ),
//                    colors = OutlinedTextFieldDefaults.colors(
//                        focusedBorderColor = PrimaryBlue,
//                        unfocusedBorderColor = Color.Transparent,
//                        cursorColor = PrimaryBlue,
//                        focusedTextColor = Color.White,
//                        unfocusedTextColor = Color.White
//                    ),
//                    singleLine = true,
//                    suffix = {
//                        Text(
//                            text = "đ",
//                            fontSize = 48.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = Color.White.copy(alpha = 0.6f)
//                        )
//                    },
//                    modifier = Modifier
//                        .fillMaxWidth(0.9f)
//                        .padding(horizontal = 16.dp)
//                )
//            }
//
//            Spacer(modifier = Modifier.height(32.dp))
//
//            // Category Selection
//            CategorySection(
//                categories = APP_CATEGORIES,
//                selectedCategory = selectedCategory,
//                onCategorySelected = { selectedCategory = it }
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Date Selection
//            SelectionCard(
//                icon = Icons.Default.DateRange,
//                iconColor = Color(0xFFE74C3C),
//                title = "Ngày giao dịch",
//                value = selectedDate,
//                onClick = { showDatePicker = true }
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Wallet Selection
//            SelectionCard(
//                icon = Icons.Default.DateRange,
//                iconColor = Color(0xFF3498DB),
//                title = "Ví tiền",
//                value = selectedWallet.name,
//                onClick = { showWalletPicker = true }
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Save Button
//            Button(
//                onClick = {
//                    // Save transaction
//                    selectedCategory?.let { category ->
//                        val amountValue = amount.toDoubleOrNull() ?: 0.0
//                        onSave(amountValue, category, selectedDate, selectedWallet)
//                        onDismiss()
//                    }
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 20.dp)
//                    .height(56.dp),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = PrimaryBlue
//                ),
//                shape = RoundedCornerShape(16.dp)
//            ) {
//                Text(
//                    text = "Lưu",
//                    fontSize = 18.sp,
//                    fontWeight = FontWeight.Bold
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun CategorySection(
//    categories: List<Category>,
//    selectedCategory: Category?,
//    onCategorySelected: (Category) -> Unit
//) {
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 20.dp)
//    ) {
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(
//                text = "Danh mục",
//                fontSize = 16.sp,
//                fontWeight = FontWeight.Medium,
//                color = Color.White
//            )
//            Text(
//                text = "Xem tất cả",
//                fontSize = 14.sp,
//                color = PrimaryBlue,
//                modifier = Modifier.clickable { }
//            )
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        LazyRow(
//            horizontalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            items(categories) { category ->
//                CategoryItem(
//                    category = category,
//                    isSelected = selectedCategory?.id == category.id,
//                    onClick = { onCategorySelected(category) }
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun CategoryItem(
//    category: Category,
//    isSelected: Boolean,
//    onClick: () -> Unit
//) {
//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally,
//        modifier = Modifier.clickable(onClick = onClick)
//    ) {
//        Box(
//            modifier = Modifier
//                .size(64.dp)
//                .clip(CircleShape)
//                .background(if (isSelected) category.color else CardBackground)
//                .border(
//                    width = if (isSelected) 2.dp else 0.dp,
//                    color = if (isSelected) category.color else Color.Transparent,
//                    shape = CircleShape
//                ),
//            contentAlignment = Alignment.Center
//        ) {
//            Icon(
//                // Nếu iconRes là Int (R.drawable.xxx)
//                painter = painterResource(id = category.icon),
//                contentDescription = category.name,
//                // Icon hỗ trợ thuộc tính tint trực tiếp
//                tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
//                modifier = Modifier.size(28.dp)
//            )
//        }
//        Spacer(modifier = Modifier.height(8.dp))
//        Text(
//            text = category.name,
//            fontSize = 13.sp,
//            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
//            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
//        )
//    }
//}
//
//
//
//// Helper function để format số có dấu chấm phân cách hàng nghìn
//fun formatAmount(amount: String): String {
//    if (amount.isEmpty() || amount == "0") return "0"
//    val cleaned = amount.replace(".", "")
//    return cleaned.reversed().chunked(3).joinToString(".").reversed()
//}
//
//fun getCurrentDate(): String {
//    val sdf = SimpleDateFormat("'Hôm nay', dd 'Th'MM", Locale("vi", "VN"))
//    return sdf.format(Date())
//}
//
//fun getDefaultWallet(): Wallet {
//    return Wallet(1, "Tiền mặt", 5000000.0)
//}