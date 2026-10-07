package com.example.lab3.data.datasource

import com.example.lab3.domain.model.Account
import com.example.lab3.domain.model.AccountType
import com.example.lab3.domain.model.Category
import com.example.lab3.domain.model.CategoryExpense
import com.example.lab3.domain.model.FinancialOverview
import com.example.lab3.domain.model.Transaction
import com.example.lab3.domain.model.TransactionType

object FinanceMockDataSource {

    fun createDefaultAccounts(): List<Account> = listOf(
        Account(
            id = "acc_1",
            name = "Основная зарплатная",
            accountNumberMasked = "•• 4821",
            balance = 284350.50,
            currency = "₽",
            type = AccountType.DEBIT_CARD,
            cardColorIndex = 0
        ),
        Account(
            id = "acc_2",
            name = "Накопительный счёт",
            accountNumberMasked = "•• 9104",
            balance = 650000.00,
            currency = "₽",
            type = AccountType.SAVINGS,
            cardColorIndex = 1
        ),
        Account(
            id = "acc_3",
            name = "Кредитная 120 дней",
            accountNumberMasked = "•• 3317",
            balance = 45200.00,
            currency = "₽",
            type = AccountType.CREDIT_CARD,
            cardColorIndex = 2
        ),
        Account(
            id = "acc_4",
            name = "Инвесткопилка",
            accountNumberMasked = "•• 7720",
            balance = 125430.80,
            currency = "₽",
            type = AccountType.INVESTMENT,
            cardColorIndex = 3
        )
    )

    fun createDefaultTransactions(): List<Transaction> = listOf(
        Transaction(
            id = "tx_1",
            title = "Продукты на неделю",
            merchant = "ВкусВилл",
            amount = 3420.50,
            currency = "₽",
            type = TransactionType.EXPENSE,
            category = Category.SUPERMARKETS,
            dateIso = "Сегодня, 6 октября",
            time = "19:42",
            accountId = "acc_1",
            accountName = "Основная зарплатная •• 4821",
            comment = "Семейные покупки, экологические продукты",
            cashbackAmount = 102.61
        ),
        Transaction(
            id = "tx_2",
            title = "Обед с коллегами",
            merchant = "Кофемания",
            amount = 1850.00,
            currency = "₽",
            type = TransactionType.EXPENSE,
            category = Category.RESTAURANTS,
            dateIso = "Сегодня, 6 октября",
            time = "14:15",
            accountId = "acc_1",
            accountName = "Основная зарплатная •• 4821",
            comment = "Деловой ланч в центре",
            cashbackAmount = 55.50
        ),
        Transaction(
            id = "tx_3",
            title = "Зачисление аванса",
            merchant = "ООО ТехИнновации",
            amount = 145000.00,
            currency = "₽",
            type = TransactionType.INCOME,
            category = Category.SALARY,
            dateIso = "Вчера, 5 октября",
            time = "11:00",
            accountId = "acc_1",
            accountName = "Основная зарплатная •• 4821",
            comment = "Выплата заработной платы за первую половину месяца",
            cashbackAmount = 0.0
        ),
        Transaction(
            id = "tx_4",
            title = "Заправка автомобиля АИ-95",
            merchant = "Лукойл АЗС №412",
            amount = 2650.00,
            currency = "₽",
            type = TransactionType.EXPENSE,
            category = Category.TRANSPORT,
            dateIso = "4 октября",
            time = "08:30",
            accountId = "acc_1",
            accountName = "Основная зарплатная •• 4821",
            comment = "Полный бак перед поездкой",
            cashbackAmount = 79.50
        ),
        Transaction(
            id = "tx_5",
            title = "Витамины и медикаменты",
            merchant = "Аптека Ригла",
            amount = 4120.00,
            currency = "₽",
            type = TransactionType.EXPENSE,
            category = Category.HEALTH,
            dateIso = "3 октября",
            time = "18:22",
            accountId = "acc_1",
            accountName = "Основная зарплатная •• 4821",
            comment = "Курс омега-3 и витамин D",
            cashbackAmount = 123.60
        ),
        Transaction(
            id = "tx_6",
            title = "Подписка и билеты в кино",
            merchant = "Кинопоиск & Premier",
            amount = 1290.00,
            currency = "₽",
            type = TransactionType.EXPENSE,
            category = Category.ENTERTAINMENT,
            dateIso = "2 октября",
            time = "21:10",
            accountId = "acc_3",
            accountName = "Кредитная 120 дней •• 3317",
            comment = "Премьера фильма с друзьями",
            cashbackAmount = 64.50
        ),
        Transaction(
            id = "tx_7",
            title = "Оплата ЖКУ и электричества",
            merchant = "МосОблЕИРЦ",
            amount = 6840.00,
            currency = "₽",
            type = TransactionType.EXPENSE,
            category = Category.HOUSING,
            dateIso = "1 октября",
            time = "10:14",
            accountId = "acc_1",
            accountName = "Основная зарплатная •• 4821",
            comment = "Квитанция за сентябрь",
            cashbackAmount = 68.40
        ),
        Transaction(
            id = "tx_8",
            title = "Дивиденды по акциям",
            merchant = "ПАО Сбербанк Дивиденды",
            amount = 8450.00,
            currency = "₽",
            type = TransactionType.INCOME,
            category = Category.INVESTMENTS,
            dateIso = "28 сентября",
            time = "16:40",
            accountId = "acc_4",
            accountName = "Инвесткопилка •• 7720",
            comment = "Квартальная выплата дивидендов",
            cashbackAmount = 0.0
        )
    )

    fun calculateCategoryExpenses(transactions: List<Transaction>): List<CategoryExpense> {
        val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
        val totalExpense = expenses.sumOf { it.amount }
        if (totalExpense <= 0.0) return emptyList()

        return expenses.groupBy { it.category }
            .map { (cat, list) ->
                val amount = list.sumOf { it.amount }
                val percentage = ((amount / totalExpense) * 100).toFloat()
                CategoryExpense(
                    category = cat,
                    amount = amount,
                    percentage = percentage,
                    transactionCount = list.size
                )
            }
            .sortedByDescending { it.amount }
    }

    fun createInitialOverview(): FinancialOverview {
        val accounts = createDefaultAccounts()
        val transactions = createDefaultTransactions()
        val totalBalance = accounts.sumOf { it.balance }
        val monthlyIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val monthlyExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val categoryExpenses = calculateCategoryExpenses(transactions)

        return FinancialOverview(
            totalBalance = totalBalance,
            monthlyIncome = monthlyIncome,
            monthlyExpense = monthlyExpense,
            accounts = accounts,
            recentTransactions = transactions,
            categoryExpenses = categoryExpenses
        )
    }

    fun createEmptyOverview(): FinancialOverview {
        return FinancialOverview(
            totalBalance = 0.0,
            monthlyIncome = 0.0,
            monthlyExpense = 0.0,
            accounts = emptyList(),
            recentTransactions = emptyList(),
            categoryExpenses = emptyList()
        )
    }
}
