package com.example.financeapp.nav

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.financeapp.data.Category
import com.example.financeapp.data.Transaction
import com.example.financeapp.ui.main.BottomNavigationBar
import com.example.financeapp.ui.history.TransactionHistoryScreen
import com.example.financeapp.ui.add.AddTransactionHomeScreen
import com.example.financeapp.ui.add.CategoryManagementScreen
import com.example.financeapp.ui.add.CreateCategoryScreen
import com.example.financeapp.ui.add.EditTransactionScreen
import com.example.financeapp.ui.add.TransactionDetailScreen
import com.example.financeapp.ui.home.DarkBackground
import com.example.financeapp.ui.home.FinanceApp
import com.example.financeapp.ui.main.MainScreenDemo
import com.example.financeapp.ui.report.StatisticsReportScreen
import com.example.financeapp.ui.setting.SettingsScreen
import com.example.financeapp.ui.stock.StockScreen
import com.example.financeapp.ui.theme.AppTheme
import com.example.financeapp.ui.theme.FinanceAppTheme
import com.example.financeapp.viewModel.FinanceViewModel

@Composable
fun MainApp(
    showAds: ((Boolean) -> Unit) -> Unit,
) {
    val navController = rememberNavController()
    val financeViewModel = hiltViewModel<FinanceViewModel>()
    var currentPage by remember { mutableIntStateOf(0) }
    var targetPage by remember { mutableIntStateOf(0) }
    var categoryEditing by remember { mutableStateOf<Category?>(null) }
    // ✅ THÊM: State để lưu transaction được chọn
    val isDarkMode by financeViewModel.isDarkMode.collectAsState()
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }
    // Lấy route hiện tại để ẩn/hiện BottomBar
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    FinanceAppTheme(isDark = isDarkMode) {
        val colors = AppTheme.colors  // ✅ Lấy colors từ theme
        Scaffold(
            containerColor = colors.background,
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
                        },
                        onNavigateToTransactionDetail = { transaction ->
                            selectedTransaction = transaction
                            navController.navigate(Screen.TransactionDetail.route)
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
                        financeViewModel = financeViewModel,
                        // ✅ SỬA: Thêm onTransactionClick
                        onTransactionClick = { transaction ->
                            selectedTransaction = transaction
                            navController.navigate(Screen.TransactionDetail.route)
                        }
                    )
                }

                composable(Screen.AddTransaction.route) {
                    AddTransactionHomeScreen(
                        onDismiss = { navController.popBackStack() },
                        defaultIsIncome = false,
                        financeViewModel = financeViewModel,
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

                composable(Screen.Stock.route) {
                    StockScreen(
                        financeViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                // ✅ SỬA: Truyền financeViewModel vào SettingsScreen
                composable(Screen.Profile.route) {
                    SettingsScreen(
                        onBack = { navController.popBackStack() },
                        financeViewModel = financeViewModel  // ✅
                    )
                }

                composable("category_management") {
                    CategoryManagementScreen(
                        onBack = { navController.popBackStack() },
                        onNavigateToCreateOrEdit = { category ->
                            categoryEditing = category // Lưu category cần sửa vào biến tạm
                            navController.navigate("create_category")
                        },
                        financeViewModel = financeViewModel
                    )
                }

                composable("create_category") {
                    CreateCategoryScreen(
                        onBack = { navController.popBackStack() },
                        categoryToEdit = categoryEditing, // Truyền category vào
                        onSave = { updatedCategory ->
                            if (categoryEditing == null) {
                                // Logic tạo mới
                                financeViewModel.addCategory(updatedCategory.copy(id = (financeViewModel.categories.maxOfOrNull { it.id } ?: 0) + 1))
                            } else {
                                // Logic cập nhật
                                financeViewModel.updateCategory(categoryEditing!!, updatedCategory)
                            }
                            navController.popBackStack()
                        }
                    )
                }

                // ✅ THÊM: Route cho TransactionDetail
                composable(Screen.TransactionDetail.route) {
                    selectedTransaction?.let { transaction ->
                        TransactionDetailScreen(
                            transaction = transaction,
                            onBack = { navController.popBackStack() },
                            onEdit = {
                                editingTransaction = transaction   // ← lưu transaction cần edit
                                navController.navigate(Screen.EditTransaction.route)  // ← navigate đúng chỗ
                            },
                            onDelete = {
                                financeViewModel.removeTransaction(transaction)
                                navController.popBackStack()
                            }
                        )
                    }
                }

                composable(Screen.EditTransaction.route) {
                    editingTransaction?.let { transaction ->
                        EditTransactionScreen(
                            transaction = transaction,
                            financeViewModel = financeViewModel,
                            onDismiss = { navController.popBackStack() },
                            onUpdate = { updatedTransaction ->
                                financeViewModel.updateTransaction(transaction, updatedTransaction)
                                selectedTransaction = updatedTransaction
                                navController.popBackStack()
                            },
                            onEditCategories = {
                                navController.navigate(Screen.CategoryManagement.route)
                            },
                            onDelete = {
                                financeViewModel.removeTransaction(transaction)
                                navController.popBackStack() // thoát Edit
                                navController.popBackStack() // thoát Detail
                            }
                        )
                    }
                }
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
    object TransactionDetail : Screen("transaction_detail")  // ✅ THÊM
    object EditTransaction : Screen("edit_transaction")
    object Stock : Screen("stock")
}