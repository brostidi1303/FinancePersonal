package com.example.financeapp.ui.main

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financeapp.R
import com.example.financeapp.ui.home.PrimaryBlue
import com.example.financeapp.ui.theme.AppTheme

@Composable
fun navigationBarItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = PrimaryBlue,
    selectedTextColor = PrimaryBlue,
    unselectedIconColor = AppTheme.colors.textSecondary,  // ✅ Dùng theme color
    unselectedTextColor = AppTheme.colors.textSecondary,  // ✅ Dùng theme color
    indicatorColor = Color.Transparent
)

@Composable
fun BottomNavigationBar(
    currentPage: Int,
    onTabSelected: (Int) -> Unit
) {
    val colors = AppTheme.colors  // ✅ Lấy colors từ theme

    NavigationBar(
        containerColor = colors.cardBackground,  // ✅ Dùng theme color thay vì CardBackground
        modifier = Modifier.height(70.dp)
    ) {
        NavigationBarItem(
            selected = currentPage == 0,
            onClick = { onTabSelected(0) },
            icon = { Icon(Icons.Default.Home, null, modifier = Modifier.size(21.dp)) },
            label = { Text("Home", fontSize = 11.sp) },
            colors = navigationBarItemColors()
        )

        NavigationBarItem(
            selected = currentPage == 1,
            onClick = { onTabSelected(1) },
            icon = { Icon(painterResource(R.drawable.history), null, modifier = Modifier.size(21.dp)) },
            label = { Text("History", fontSize = 11.sp) },
            colors = navigationBarItemColors()
        )

        NavigationBarItem(
            selected = currentPage == 2,
            onClick = { onTabSelected(2) },
            icon = { Icon(Icons.Default.Add, null, modifier = Modifier.size(21.dp)) },
            label = { Text("Add", fontSize = 11.sp) },
            colors = navigationBarItemColors()
        )

        NavigationBarItem(
            selected = currentPage == 3,
            onClick = { onTabSelected(3) },
            icon = { Icon(painterResource(R.drawable.statistics), null, modifier = Modifier.size(21.dp)) },
            label = { Text("Report", fontSize = 11.sp) },
            colors = navigationBarItemColors()
        )

        NavigationBarItem(
            selected = currentPage == 4,
            onClick = { onTabSelected(4) },
            icon = { Icon(Icons.Default.Settings, null, modifier = Modifier.size(21.dp)) },
            label = { Text("Profile", fontSize = 11.sp) },  // ✅ Sửa label từ "Report" thành "Profile"
            colors = navigationBarItemColors()
        )
    }
}