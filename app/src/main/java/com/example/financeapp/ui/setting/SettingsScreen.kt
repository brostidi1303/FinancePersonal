package com.example.financeapp.ui.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

// Colors
private val DarkBackground = Color(0xFF0A0E1F)
private val CardBackground = Color(0xFF1A1F3A)
private val PrimaryBlue = Color(0xFF0D7EFF)
private val TextGray = Color(0xFF8F92A1)
private val SectionTitleGray = Color(0xFF6B7280)

@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    var isDarkMode by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Header
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
                    tint = Color.White
                )
            }

            Text(
                text = "Cài đặt & Cá nhân",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Card
            item {
                ProfileCard(
                    name = "Nguyễn Văn A",
                    userId = "ID: 12345678",
                    isPro = true,
                    onProfileClick = { /* Navigate to profile */ }
                )
            }

            // TÀI KHOẢN Section
            item {
                SectionTitle("TÀI KHOẢN")
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardBackground),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    SettingsItem(
                        icon = Icons.Default.Person,
                        iconColor = PrimaryBlue,
                        title = "Thông tin cá nhân",
                        onClick = { /* Navigate */ }
                    )

                    Divider(
                        color = DarkBackground,
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    SettingsItem(
                        icon = Icons.Default.Lock,
                        iconColor = Color(0xFF10B981),
                        title = "Bảo mật & Quyền riêng tư",
                        onClick = { /* Navigate */ }
                    )
                }
            }

            // ỨNG DỤNG Section
            item {
                SectionTitle("ỨNG DỤNG")
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardBackground),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    SettingsItem(
                        icon = Icons.Default.DateRange,
                        iconColor = Color(0xFFFFA500),
                        title = "Tiền tệ",
                        trailing = {
                            Text(
                                text = "VND",
                                fontSize = 14.sp,
                                color = TextGray
                            )
                        },
                        onClick = { /* Navigate */ }
                    )

                    Divider(
                        color = DarkBackground,
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    SettingsItem(
                        icon = Icons.Default.DateRange,
                        iconColor = Color(0xFF8B5CF6),
                        title = "Ngôn ngữ",
                        trailing = {
                            Text(
                                text = "Tiếng Việt",
                                fontSize = 14.sp,
                                color = TextGray
                            )
                        },
                        onClick = { /* Navigate */ }
                    )

                    Divider(
                        color = DarkBackground,
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    SettingsItem(
                        icon = Icons.Default.DateRange,
                        iconColor = Color(0xFF3B82F6),
                        title = "Chế độ tối",
                        trailing = {
                            Switch(
                                checked = isDarkMode,
                                onCheckedChange = { isDarkMode = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = PrimaryBlue,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color.Gray
                                )
                            )
                        },
                        onClick = { isDarkMode = !isDarkMode },
                        showArrow = false
                    )
                }
            }

            // DỮ LIỆU Section
            item {
                SectionTitle("DỮ LIỆU")
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardBackground),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    SettingsItem(
                        icon = Icons.Default.DateRange,
                        iconColor = Color(0xFF10B981),
                        title = "Xuất file Excel",
                        onClick = { /* Export */ }
                    )

                    Divider(
                        color = DarkBackground,
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    SettingsItem(
                        icon = Icons.Default.DateRange,
                        iconColor = Color(0xFFFF6B6B),
                        title = "Sao lưu & Phục hồi",
                        onClick = { /* Backup */ }
                    )
                }
            }

            // KHÁC Section
            item {
                SectionTitle("KHÁC")
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CardBackground),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    SettingsItem(
                        icon = Icons.Default.Info,
                        iconColor = Color(0xFF6B7280),
                        title = "Về ứng dụng",
                        onClick = { /* About */ }
                    )

                    Divider(
                        color = DarkBackground,
                        thickness = 1.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    SettingsItem(
                        icon = Icons.Default.Star,
                        iconColor = Color(0xFFEC4899),
                        title = "Đánh giá ứng dụng",
                        onClick = { /* Rate */ }
                    )
                }
            }

            // Logout Button
            item {
                TextButton(
                    onClick = { /* Logout */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Text(
                        text = "Đăng xuất",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFEF4444)
                    )
                }
            }

            // Version
            item {
                Text(
                    text = "Phiên bản 2.4.0 (Build 1082)",
                    fontSize = 12.sp,
                    color = TextGray,
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
fun ProfileCard(
    name: String,
    userId: String,
    isPro: Boolean,
    onProfileClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardBackground)
            .clickable(onClick = onProfileClick)
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFB380)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Avatar",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            // Name and Badge
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isPro) {
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
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "THÀNH VIÊN PRO",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Text(
                    text = userId,
                    fontSize = 13.sp,
                    color = TextGray
                )
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = SectionTitleGray,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
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
                // Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Title
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White
                )
            }

            // Trailing content
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (trailing != null) {
                    trailing()
                }

                if (showArrow) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = TextGray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}