package com.example.financeapp

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import com.example.financeapp.data.Transaction
import com.example.financeapp.home.AddTransactionHomeScreen
import com.example.financeapp.home.FinanceApp
import com.example.financeapp.viewModel.FinanceViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreenDemo(
    financeViewModel: FinanceViewModel, // Nhận ViewModel thay vì từng state riêng lẻ
    onPageChanged: (Int) -> Unit,
    targetPage: Int,
) {
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 4 })
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
                    onAddTransaction = { amount, category, isIncome, formattedDate, note -> // ✅ THÊM category
                        // Tạo transaction mới với thông tin từ category
                        val newTransaction = Transaction(
                            id = transactions.size + 1,
                            title = category.name,        // ✅ Lấy tên từ category
                            date = formattedDate,
                            amount = amount,
                            icon = category.icon,         // ✅ Lấy icon từ category
                            iconColor = category.color,   // ✅ Lấy màu từ category
                            isIncome = isIncome,
                            note = note
                        )

                        // Lưu vào ViewModel
                        financeViewModel.addTransaction(newTransaction)

                        // Quay về trang Home
                        scope.launch { pagerState.animateScrollToPage(0) }
                    }
                )

                3 -> StatisticsReportScreen(
                    onBack = { scope.launch { pagerState.animateScrollToPage(0) } }
                )
            }
        }
    }
}