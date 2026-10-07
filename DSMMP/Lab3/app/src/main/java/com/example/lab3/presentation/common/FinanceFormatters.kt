package com.example.lab3.presentation.common

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object FinanceFormatters {
    private val symbols = DecimalFormatSymbols(Locale("ru", "RU")).apply {
        groupingSeparator = ' '
        decimalSeparator = ','
    }

    private val amountFormat = DecimalFormat("#,##0.00", symbols)
    private val integerAmountFormat = DecimalFormat("#,##0", symbols)

    fun formatMoney(amount: Double, currency: String = "₽"): String {
        return "${amountFormat.format(amount)} $currency"
    }

    fun formatShortMoney(amount: Double, currency: String = "₽"): String {
        return "${integerAmountFormat.format(amount)} $currency"
    }

    fun formatPercent(percentage: Float): String {
        return String.format(Locale("ru", "RU"), "%.1f%%", percentage)
    }
}
