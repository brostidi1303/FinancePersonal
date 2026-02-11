package com.example.financeapp.ui.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.example.financeapp.data.Transaction
import com.example.financeapp.ui.history.TransactionHistoryScreen
import com.example.financeapp.ui.add.AddTransactionHomeScreen
import com.example.financeapp.ui.home.FinanceApp
import com.example.financeapp.ui.report.StatisticsReportScreen
import com.example.financeapp.ui.setting.SettingsScreen
import com.example.financeapp.viewModel.FinanceViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreenDemo(
    financeViewModel: FinanceViewModel, // Nhận ViewModel thay vì từng state riêng lẻ
    onPageChanged: (Int) -> Unit,
    targetPage: Int,
    onNavigateToCategoryManagement: () -> Unit = {}
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 5 })
    val scope = rememberCoroutineScope()
    val transactions = financeViewModel.transactions

    // Scroll đến page khi click vào BottomBar
    LaunchedEffect(targetPage) {
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    // Thông báo khi page thay đổi
    LaunchedEffect(pagerState.currentPage) {
        onPageChanged(pagerState.currentPage)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = true
        ) { page ->
            when (page) {
                0 -> FinanceApp(
                    financeViewModel = financeViewModel,
                    onViewAll = { scope.launch { pagerState.animateScrollToPage(1) } },
                )

                1 -> TransactionHistoryScreen(
                    onBack = { scope.launch { pagerState.animateScrollToPage(0) } },
                    financeViewModel = financeViewModel
                )

                2 -> AddTransactionHomeScreen(
                    onDismiss = { scope.launch { pagerState.animateScrollToPage(0) } },
                    defaultIsIncome = false,
                    financeViewModel = financeViewModel,
                    onAddTransaction = { amount, category, isIncome, date, time, note -> // ✅ THÊM category
                        val newTransaction = Transaction(
                            id = transactions.size + 1,
                            title = category.name,
                            date = date,           // ✅ Chỉ date: "Hôm nay" hoặc "dd/MM/yyyy"
                            time = time,           // ✅ Chỉ time: "HH:mm"
                            amount = amount,
                            icon = category.icon,
                            iconColor = category.color,
                            isIncome = isIncome,
                            note = note
                        )

                        // Lưu vào ViewModel
                        financeViewModel.addTransaction(newTransaction)

                        // Quay về trang Home
                        scope.launch { pagerState.animateScrollToPage(0) }
                    },
                    onEditCategories = onNavigateToCategoryManagement
                )

                3 -> StatisticsReportScreen(
                    onBack = { scope.launch { pagerState.animateScrollToPage(0) } },
                    financeViewModel = financeViewModel
                )

                4 -> SettingsScreen(
                    onBack = { scope.launch { pagerState.animateScrollToPage(0) } }
                )
            }
        }
    }
}