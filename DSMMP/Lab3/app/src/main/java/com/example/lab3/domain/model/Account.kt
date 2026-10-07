package com.example.lab3.domain.model

enum class AccountType(val title: String) {
    DEBIT_CARD("Дебетовая карта"),
    SAVINGS("Накопительный счёт"),
    CREDIT_CARD("Кредитная карта"),
    INVESTMENT("Брокерский счёт")
}

data class Account(
    val id: String,
    val name: String,
    val accountNumberMasked: String,
    val balance: Double,
    val currency: String = "₽",
    val type: AccountType,
    val cardColorIndex: Int = 0
)
