package com.example.financeflow.ui.home.models

import java.util.Date

data class Expense(
    val category: String,
    val amount: Double,
    val isIncome: Boolean,
    val date: Date
)
