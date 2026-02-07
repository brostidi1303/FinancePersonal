package com.example.financeapp.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.financeapp.R

data class Transaction(
    val id: Int,
    val title: String,
    val date: String,        // Format: "dd/MM/yyyy" hoặc "Hôm nay"
    val time: String,        // Format: "HH:mm"
    val amount: Double,
    val icon: Int,
    val iconColor: Color,
    val isIncome: Boolean,
    val note: String = ""
)

data class Category(
    val id: Int,
    val name: String,
    val icon: Int,
    val color: Color
)

data class Wallet(
    val id: Int,
    val name: String,
    val balance: Double
)

val APP_CATEGORIES = listOf(
    Category(1, "Ăn uống", R.drawable.restaurant, Color(0xFF5B7FFF)), // Xanh dương
    Category(2, "Cà phê", R.drawable.coffee, Color(0xFF795548)),    // Nâu
    Category(3, "Mua sắm", R.drawable.grocery_store, Color(0xFFE91E63)), // Hồng
    Category(4, "Di chuyển", R.drawable.plane, Color(0xFF00C48C)),   // Xanh lá
    Category(5, "Hóa đơn", R.drawable.invoice, Color(0xFFFF9800)),   // Cam
    Category(6, "Giải trí", R.drawable.theater, Color(0xFF9C27B0)),  // Tím
    Category(7, "Y tế", R.drawable.health_care, Color(0xFFF44336)),  // Đỏ
    Category(8, "Khác", R.drawable.application, Color(0xFF607D8B))   // Xám xanh
)