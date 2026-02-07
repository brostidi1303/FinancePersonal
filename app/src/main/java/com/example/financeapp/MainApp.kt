package com.example.financeapp

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.financeapp.home.DarkBackground
import com.example.financeapp.home.FinanceApp
import com.example.financeapp.viewModel.FinanceViewModel

@Composable
fun MainApp(
    showAds: ((Boolean) -> Unit) -> Unit, // Tạo ViewModel
) {
    val navController = rememberNavController()
    val financeViewModel: FinanceViewModel = viewModel()
    // State cho navigation
    var currentPage by remember { mutableIntStateOf(0) }
    var targetPage by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            BottomNavigationBar(
                currentPage = currentPage,
                onTabSelected = { page ->
                    targetPage = page
                }
            )
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Main.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Main.route) {
                MainScreenDemo(
                    financeViewModel = financeViewModel, // Truyền ViewModel xuống
                    onPageChanged = { currentPage = it },
                    targetPage = targetPage
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
                AddTransactionScreen(
                    onDismiss = { navController.popBackStack() },
                    onSave = { _, _, _, _ -> navController.popBackStack() }
                )
            }

            composable(Screen.Statistics.route) {
                StatisticsReportScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.Profile.route) {
                /* ProfileScreen() */
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
}