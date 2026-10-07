package com.example.lab3.domain.model

data class FinancialOverview(
    val totalBalance: Double,
    val monthlyIncome: Double,
    val monthlyExpense: Double,
    val accounts: List<Account>,
    val recentTransactions: List<Transaction>,
    val categoryExpenses: List<CategoryExpense>
)
