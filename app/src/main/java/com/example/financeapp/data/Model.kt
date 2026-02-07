package com.example.financeapp.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class Transaction(
    val id: Int,
    val title: String,
    val date: String,
    val amount: Double,
    val icon: ImageVector,
    val iconColor: Color,
    val isIncome: Boolean
)

data class TransactionDetail(
    val id: Int,
    val title: String,
    val subtitle: String,
    val date: String,
    val dateGroup: String,
    val amount: Double,
    val icon: ImageVector,
    val iconColor: Color,
    val isIncome: Boolean
)