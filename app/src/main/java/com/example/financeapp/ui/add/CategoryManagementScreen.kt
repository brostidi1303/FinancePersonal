package com.example.financeapp.ui.add

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.financeapp.data.Category
import com.example.financeapp.ui.home.CardBackground
import com.example.financeapp.ui.home.DarkBackground
import com.example.financeapp.ui.home.PrimaryBlue
import com.example.financeapp.viewModel.FinanceViewModel

@Composable
fun CategoryManagementScreen(
    onBack: () -> Unit,
    // ✅ Thay đổi: Callback này giờ nhận vào Category? (null = tạo mới, có giá trị = sửa)
    onNavigateToCreateOrEdit: (Category?) -> Unit,
    financeViewModel: FinanceViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) } // Chỉ giữ lại state xóa

    val categories = financeViewModel.categories

    // Lọc categories theo search
    val filteredCategories = remember(searchQuery, categories.size) {
        if (searchQuery.isEmpty()) {
            categories.toList()
        } else {
            categories.filter {
                it.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    val expenseCategories = filteredCategories.filter { it.name != "Tiền lương" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(24.dp))
            }
            Text(text = "Danh mục", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.width(48.dp))
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 2.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(expenseCategories) { category ->
                CategoryManagementItem(
                    category = category,
                    // ✅ Click Edit: Điều hướng sang màn hình tạo với dữ liệu category
                    onEdit = { onNavigateToCreateOrEdit(category) },
                    // ✅ Click Delete: Hiện Dialog xác nhận xóa
                    onDelete = { categoryToDelete = category }
                )
            }
        }

        // Add Category Button
        Button(
            // ✅ Click Add: Điều hướng sang màn hình tạo với tham số null
            onClick = { onNavigateToCreateOrEdit(null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Thêm danh mục mới", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }

    // Delete Confirmation Dialog (Giữ nguyên, chỉ gọi ViewModel để xóa)
    if (categoryToDelete != null) {
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text(text = "Xóa danh mục", color = Color.White) },
            text = { Text(text = "Bạn có chắc chắn muốn xóa danh mục \"${categoryToDelete!!.name}\"?", color = Color.White.copy(alpha = 0.8f)) },
            confirmButton = {
                TextButton(onClick = {
                    financeViewModel.removeCategory(categoryToDelete!!)
                    categoryToDelete = null
                }) {
                    Text("Xóa", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) {
                    Text("Hủy", color = PrimaryBlue)
                }
            },
            containerColor = CardBackground
        )
    }
}

// CategoryManagementItem giữ nguyên
@Composable
fun CategoryManagementItem(category: Category, onEdit: () -> Unit, onDelete: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(category.color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(painter = painterResource(id = category.icon), contentDescription = null, tint = category.color, modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = category.name, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.White)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onEdit, modifier = Modifier.size(40.dp)) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}