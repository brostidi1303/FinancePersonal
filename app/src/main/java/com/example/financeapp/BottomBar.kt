package com.example.financeapp

import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financeapp.home.CardBackground
import com.example.financeapp.home.PrimaryBlue

@Composable
fun navigationBarItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = PrimaryBlue,
    selectedTextColor = PrimaryBlue,
    unselectedIconColor = Color.White,
    unselectedTextColor = Color.White,
    indicatorColor = Color.Transparent
)

@Composable
fun BottomNavigationBar(
    currentPage: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = CardBackground,
        modifier = Modifier.height(70.dp)
    ) {
        NavigationBarItem(
            selected = currentPage == 0,
            onClick = { onTabSelected(0) },
            icon = { Icon(Icons.Default.Home, null) },
            label = { Text("Home", fontSize = 11.sp) },
            colors = navigationBarItemColors()
        )

        NavigationBarItem(
            selected = currentPage == 1,
            onClick = { onTabSelected(1) },
            icon = { Icon(Icons.Default.List, null) },
            label = { Text("History", fontSize = 11.sp) },
            colors = navigationBarItemColors()
        )

        NavigationBarItem(
            selected = currentPage == 2,
            onClick = { onTabSelected(2) },
            icon = { Icon(Icons.Default.Add, null) },
            label = { Text("Add", fontSize = 11.sp) },
            colors = navigationBarItemColors()
        )

        NavigationBarItem(
            selected = currentPage == 3,
            onClick = { onTabSelected(3) },
            icon = { Icon(Icons.Default.Info, null) },
            label = { Text("Report", fontSize = 11.sp) },
            colors = navigationBarItemColors()
        )
    }
}