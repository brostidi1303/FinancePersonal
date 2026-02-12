package com.example.financeapp.ui.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financeapp.ui.theme.AppTheme
import com.example.financeapp.viewModel.FinanceViewModel

private val PrimaryBlue = Color(0xFF0D7EFF)

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    financeViewModel: FinanceViewModel  // ✅ Nhận ViewModel để đọc/ghi isDarkMode
) {
    // ✅ Đọc isDarkMode từ ViewModel — tự động recompose khi thay đổi
    val isDarkMode by financeViewModel.isDarkMode.collectAsState()

    // ✅ Màu sắc thay đổi theo theme hiện tại
    val colors = AppTheme.colors
    val textGray = if (colors.isDark) Color(0xFF8F92A1) else Color(0xFF9CA3AF)
    val sectionGray = if (colors.isDark) Color(0xFF6B7280) else Color(0xFFADB5BD)
    val dividerColor = if (colors.isDark) colors.background else Color(0xFFF0F2F4)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)  // ✅ Dùng theme color
    ) {
        // ── HEADER ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.textPrimary  // ✅
                )
            }
            Text(
                text = "Cài đặt & Cá nhân",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,  // ✅
                modifier = Modifier.align(Alignment.Center)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── PROFILE CARD ──
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.cardBackground)  // ✅
                        .clickable { }
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFB380)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person, null,
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "Nguyễn Văn A",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary  // ✅
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(PrimaryBlue)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Star, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Text("THÀNH VIÊN PRO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                            Text("ID: 12345678", fontSize = 13.sp, color = textGray)
                        }
                    }
                }
            }

            // ── TÀI KHOẢN ──
            item {
                Text("TÀI KHOẢN", fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                    color = sectionGray, letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp))
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.cardBackground)  // ✅
                ) {
                    SettingsRow(Icons.Default.Person, PrimaryBlue, "Thông tin cá nhân",
                        colors.textPrimary, textGray, onClick = {})
                    HorizontalDivider(color = dividerColor, modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsRow(Icons.Default.Lock, Color(0xFF10B981), "Bảo mật & Quyền riêng tư",
                        colors.textPrimary, textGray, onClick = {})
                }
            }

            // ── ỨNG DỤNG ──
            item {
                Text("ỨNG DỤNG", fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                    color = sectionGray, letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp))
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.cardBackground)  // ✅
                ) {
                    SettingsRow(
                        icon = Icons.Default.Settings,
                        iconColor = Color(0xFFFFA500),
                        title = "Tiền tệ",
                        textColor = colors.textPrimary,
                        trailingTextColor = textGray,
                        trailing = { Text("VND", fontSize = 14.sp, color = textGray) },
                        onClick = {}
                    )
                    HorizontalDivider(color = dividerColor, modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsRow(
                        icon = Icons.Default.Settings,
                        iconColor = Color(0xFF8B5CF6),
                        title = "Ngôn ngữ",
                        textColor = colors.textPrimary,
                        trailingTextColor = textGray,
                        trailing = { Text("Tiếng Việt", fontSize = 14.sp, color = textGray) },
                        onClick = {}
                    )
                    HorizontalDivider(color = dividerColor, modifier = Modifier.padding(horizontal = 16.dp))
                    // ✅ SWITCH CHẾ ĐỘ TỐI — gọi ViewModel khi toggle
                    SettingsRow(
                        icon = Icons.Default.Settings,
                        iconColor = Color(0xFF3B82F6),
                        title = "Chế độ tối",
                        textColor = colors.textPrimary,
                        trailingTextColor = textGray,
                        trailing = {
                            Switch(
                                checked = isDarkMode,
                                onCheckedChange = { financeViewModel.setDarkMode(it) },  // ✅ gọi ViewModel
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = PrimaryBlue,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color.Gray
                                )
                            )
                        },
                        showArrow = false,
                        onClick = { financeViewModel.toggleDarkMode() }  // ✅ click row cũng toggle
                    )
                }
            }

            // ── DỮ LIỆU ──
            item {
                Text("DỮ LIỆU", fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                    color = sectionGray, letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp))
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.cardBackground)  // ✅
                ) {
                    SettingsRow(Icons.Default.Settings, Color(0xFF10B981), "Xuất file Excel",
                        colors.textPrimary, textGray, onClick = {})
                    HorizontalDivider(color = dividerColor, modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsRow(Icons.Default.Settings, Color(0xFFFF6B6B), "Sao lưu & Phục hồi",
                        colors.textPrimary, textGray, onClick = {})
                }
            }

            // ── KHÁC ──
            item {
                Text("KHÁC", fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                    color = sectionGray, letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp))
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.cardBackground)  // ✅
                ) {
                    SettingsRow(Icons.Default.Info, Color(0xFF6B7280), "Về ứng dụng",
                        colors.textPrimary, textGray, onClick = {})
                    HorizontalDivider(color = dividerColor, modifier = Modifier.padding(horizontal = 16.dp))
                    SettingsRow(Icons.Default.Star, Color(0xFFEC4899), "Đánh giá ứng dụng",
                        colors.textPrimary, textGray, onClick = {})
                }
            }

            item {
                TextButton(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                ) {
                    Text("Đăng xuất", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFFEF4444))
                }
            }

            item {
                Text(
                    text = "Phiên bản 2.4.0 (Build 1082)",
                    fontSize = 12.sp,
                    color = textGray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 80.dp)
                        .wrapContentWidth(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    textColor: Color,
    trailingTextColor: Color,
    trailing: @Composable (() -> Unit)? = null,
    onClick: () -> Unit,
    showArrow: Boolean = true
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
                }
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.Normal, color = textColor)
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                trailing?.invoke()
                if (showArrow) {
                    Icon(
                        Icons.Default.Settings, null,
                        tint = trailingTextColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}