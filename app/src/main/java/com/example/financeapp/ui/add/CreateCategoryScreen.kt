package com.example.financeapp.ui.add

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financeapp.R
import com.example.financeapp.data.Category
import com.example.financeapp.ui.home.CardBackground
import com.example.financeapp.ui.home.DarkBackground
import com.example.financeapp.ui.home.PrimaryBlue
import com.example.financeapp.ui.theme.AppTheme

// Danh sách màu sắc
val categoryColors = listOf(
    Color(0xFFE74C3C), // Đỏ
    Color(0xFFFF9500), // Cam
    Color(0xFFF1C40F), // Vàng
    Color(0xFF2ECC71), // Xanh lá
    Color(0xFF3498DB), // Xanh dương
    Color(0xFF9B59B6), // Tím
    Color(0xFFE91E63), // Hồng
    Color(0xFF00BCD4), // Cyan
    Color(0xFF5E6FE8), // Xanh tím
    Color(0xFFFF5252), // Đỏ hồng
    Color(0xFFFF9800), // Cam vàng
    Color(0xFF9E9E9E)  // Xám
)

// Danh sách icon
data class CategoryIcon(
    val id: Int,
    val resourceId: Int? = null,
    val imageVector: ImageVector? = null
)

@Composable
fun CreateCategoryScreen(
    onBack: () -> Unit,
    categoryToEdit: Category? = null, // ✅ Thêm tham số này để nhận dữ liệu cần sửa
    onSave: (Category) -> Unit
) {
    val colors = AppTheme.colors  // ✅ Lấy colors từ theme
    // ✅ Khởi tạo state dựa trên categoryToEdit (nếu có)
    var categoryName by remember { mutableStateOf(categoryToEdit?.name ?: "") }
    var selectedColor by remember { mutableStateOf(categoryToEdit?.color ?: categoryColors[3]) }

    // Danh sách icons (sử dụng các icon có sẵn trong project)
    val categoryIcons = remember {
        listOf(
            CategoryIcon(1, R.drawable.application),
            CategoryIcon(2, R.drawable.application),
            CategoryIcon(3, R.drawable.application),
            CategoryIcon(4, R.drawable.application),
            CategoryIcon(5, R.drawable.application),
            CategoryIcon(6, R.drawable.application),
            CategoryIcon(7, R.drawable.application),
            CategoryIcon(8, R.drawable.application),
            CategoryIcon(9, R.drawable.application),
            CategoryIcon(10, R.drawable.application),
            CategoryIcon(11, R.drawable.application),
            CategoryIcon(12, R.drawable.application),
            CategoryIcon(13, R.drawable.application),
            CategoryIcon(14, R.drawable.application),
            CategoryIcon(15, R.drawable.application)
        )
    }

    // Tìm icon ID tương ứng từ resource ID
    val initialIconId = remember {
        if (categoryToEdit != null) {
            categoryIcons.find { it.resourceId == categoryToEdit.icon }?.id
        } else null
    }

    var selectedIconId by remember { mutableStateOf(initialIconId) }

    // Mặc định chọn icon đầu tiên
    LaunchedEffect(Unit) {
        if (selectedIconId == null && categoryIcons.isNotEmpty()) {
            selectedIconId = categoryIcons[0].id
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text(
                    text = "Hủy",
                    color =  colors.textPrimary,
                    fontSize = 16.sp
                )
            }

            Text(
                text = if (categoryToEdit != null) "Chỉnh sửa Danh mục" else "Tạo Danh mục mới",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )

            TextButton(
                onClick = {
                    if (categoryName.isNotEmpty() && selectedIconId != null) {
                        val iconResource = categoryIcons.find { it.id == selectedIconId }?.resourceId
                        if (iconResource != null) {
                            // ✅ Tạo object Category (giữ ID cũ nếu là sửa, tạo ID mới hoặc 0 nếu là thêm)
                            val finalCategory = Category(
                                id = categoryToEdit?.id ?: 0, // ID sẽ được xử lý ở ViewModel nếu là 0
                                name = categoryName,
                                icon = iconResource,
                                color = selectedColor
                            )
                            onSave(finalCategory)
                        }
                    }
                },
                enabled = categoryName.isNotEmpty() && selectedIconId != null
            ) {
                Text(
                    text = "Lưu",
                    color = if (categoryName.isNotEmpty() && selectedIconId != null) PrimaryBlue else colors.textSecondary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Icon Preview
        Box(
            modifier = Modifier
                .size(140.dp)
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(32.dp))
                .background(selectedColor.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            selectedIconId?.let { iconId ->
                val icon = categoryIcons.find { it.id == iconId }
                icon?.resourceId?.let { resId ->
                    Icon(
                        painter = painterResource(id = resId),
                        contentDescription = null,
                        tint = selectedColor,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Category Name Display
        Text(
            text = categoryName.ifEmpty { "Sức khỏe" },
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Input Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            // Tên danh mục
            Text(
                text = "TÊN DANH MỤC",
                fontSize = 12.sp,
                color = colors.textSecondary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            TextField(
                value = categoryName,
                onValueChange = { categoryName = it },
                placeholder = {
                    Text(
                        text = "Sức khỏe",
                        color = colors.textSecondary
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colors.cardBackground,  // ✅ Dùng theme color
                    unfocusedContainerColor = colors.cardBackground,  // ✅ Dùng theme color
                    focusedTextColor = colors.textPrimary,  // ✅ Dùng theme color
                    unfocusedTextColor = colors.textPrimary,  // ✅ Dùng theme color
                    cursorColor = PrimaryBlue,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Chọn màu sắc
            Text(
                text = "CHỌN MÀU SẮC",
                fontSize = 12.sp,
                color =  colors.textSecondary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.height(140.dp)
            ) {
                items(categoryColors) { color ->
                    ColorItem(
                        color = color,
                        isSelected = selectedColor == color,
                        onClick = { selectedColor = color }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Chọn biểu tượng
            Text(
                text = "CHỌN BIỂU TƯỢNG",
                fontSize = 12.sp,
                color = colors.textSecondary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(300.dp)
            ) {
                items(categoryIcons) { icon ->
                    IconItem(
                        icon = icon,
                        isSelected = selectedIconId == icon.id,
                        onClick = { selectedIconId = icon.id }
                    )
                }
            }
        }
    }
}

@Composable
fun ColorItem(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color)
            .then(
                if (isSelected) {
                    Modifier.border(3.dp, colors.textPrimary, CircleShape)
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick)
    )
}

@Composable
fun IconItem(
    icon: CategoryIcon,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) CardBackground.copy(alpha = 0.8f)
                else CardBackground.copy(alpha = 0.4f)
            )
            .clickable(onClick = onClick)
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, PrimaryBlue, RoundedCornerShape(16.dp))
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        icon.resourceId?.let { resId ->
            Icon(
                painter = painterResource(id = resId),
                contentDescription = null,
                tint = colors.textPrimary.copy(alpha = 0.8f),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}