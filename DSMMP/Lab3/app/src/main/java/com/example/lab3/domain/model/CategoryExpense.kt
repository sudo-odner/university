package com.example.lab3.domain.model

data class CategoryExpense(
    val category: Category,
    val amount: Double,
    val percentage: Float,
    val transactionCount: Int
)
