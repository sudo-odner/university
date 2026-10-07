package com.example.lab3.domain.model

data class Transaction(
    val id: String,
    val title: String,
    val merchant: String,
    val amount: Double,
    val currency: String = "₽",
    val type: TransactionType,
    val category: Category,
    val dateIso: String,
    val time: String,
    val accountId: String,
    val accountName: String,
    val comment: String? = null,
    val cashbackAmount: Double = 0.0
)
