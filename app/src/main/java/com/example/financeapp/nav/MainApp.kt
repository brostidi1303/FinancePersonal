package com.example.financeapp.nav

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.financeapp.ui.main.BottomNavigationBar
import com.example.financeapp.ui.history.TransactionHistoryScreen
import com.example.financeapp.ui.add.AddTransactionHomeScreen
import com.example.financeapp.ui.add.CategoryManagementScreen
import com.example.financeapp.ui.add.CreateCategoryScreen
import com.example.financeapp.ui.home.DarkBackground
import com.example.financeapp.ui.home.FinanceApp
import com.example.financeapp.ui.main.MainScreenDemo
import com.example.financeapp.ui.report.StatisticsReportScreen
import com.example.financeapp.ui.setting.SettingsScreen
import com.example.financeapp.viewModel.FinanceViewModel

@Composable
fun MainApp(
    showAds: ((Boolean) -> Unit) -> Unit,
) {
    val navController = rememberNavController()
    val financeViewModel: FinanceViewModel = viewModel()
    var currentPage by remember { mutableIntStateOf(0) }
    var targetPage by remember { mutableIntStateOf(0) }

    // ✅ Lấy route hiện tại để ẩn/hiện BottomBar
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            // ✅ Chỉ hiển thị BottomBar khi ở Main screen
            if (currentRoute == Screen.Main.route) {
                BottomNavigationBar(
                    currentPage = currentPage,
                    onTabSelected = { page ->
                        targetPage = page
                    }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Main.route,
            modifier = Modifier.padding(
                // ✅ Chỉ apply padding khi ở Main screen
                if (currentRoute == Screen.Main.route) padding else PaddingValues(0.dp)
            )
        ) {
            composable(Screen.Main.route) {
                MainScreenDemo(
                    financeViewModel = financeViewModel,
                    onPageChanged = { currentPage = it },
                    targetPage = targetPage,
                    onNavigateToCategoryManagement = { // ✅ Navigate ra ngoài
                        navController.navigate(Screen.CategoryManagement.route)
                    }
                )
            }

            composable(Screen.Home.route) {
                FinanceApp(
                    onViewAll = { /* Xử lý chuyển trang */ },
                    financeViewModel = financeViewModel,
                )
            }

            composable(Screen.History.route) {
                TransactionHistoryScreen(
                    onBack = { navController.popBackStack() },
                    financeViewModel = financeViewModel
                )
            }

            composable(Screen.AddTransaction.route) {
                AddTransactionHomeScreen(
                    onDismiss = { navController.popBackStack() },
                    defaultIsIncome = false,
                    onAddTransaction = { _, _, _, _, _, _ -> navController.popBackStack() },
                    onEditCategories = {
                        navController.navigate(Screen.CategoryManagement.route)
                    }
                )
            }

            composable(Screen.Statistics.route) {
                StatisticsReportScreen(
                    onBack = { navController.popBackStack() },
                    financeViewModel = financeViewModel
                )
            }

            composable(Screen.Profile.route) {
                SettingsScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            // ✅ CategoryManagement nằm ngoài MainScreenDemo
            composable(Screen.CategoryManagement.route) {
                CategoryManagementScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToCreateCategory = {
                        navController.navigate(Screen.CreateCategory.route)
                    }
                )
            }

            // ✅ Thêm route CreateCategory
            composable(Screen.CreateCategory.route) {
                CreateCategoryScreen(
                    onBack = { navController.popBackStack() },
                    onSave = { name, color, iconId ->
                        // TODO: Lưu category mới vào ViewModel
                        // val newCategory = Category(name = name, color = color, icon = iconId)
                        // financeViewModel.addCategory(newCategory)
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Home : Screen("home")
    object History : Screen("history")
    object AddTransaction : Screen("add_transaction")
    object Statistics : Screen("statistics")
    object Profile : Screen("profile")
    object CategoryManagement : Screen("category_management")
    object CreateCategory : Screen("create_category")
}