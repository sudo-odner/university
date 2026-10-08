package com.example.lab3.presentation.preview

import android.content.res.Configuration
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.lab3.data.datasource.FinanceMockDataSource
import com.example.lab3.presentation.details.TransactionDetailScreen
import com.example.lab3.presentation.overview.CompactOverviewContent
import com.example.lab3.presentation.overview.OverviewScreen
import com.example.lab3.presentation.overview.OverviewUiState
import com.example.lab3.presentation.overview.components.BalanceSummaryCard
import com.example.lab3.presentation.overview.components.CategoryDistributionSection
import com.example.lab3.presentation.overview.components.TransactionItem
import com.example.lab3.presentation.theme.FinanceAppTheme
import com.example.lab3.presentation.theme.ThemeMode

private val sampleOverview = FinanceMockDataSource.createInitialOverview()
private val sampleTransaction = sampleOverview.recentTransactions.first()

@Preview(name = "Light Mode Overview", showBackground = true)
@Composable
fun PreviewOverviewLight() {
    FinanceAppTheme(darkTheme = false) {
        OverviewScreen(
            uiState = OverviewUiState.Content(sampleOverview),
            themeMode = ThemeMode.LIGHT,
            windowWidthSizeClass = WindowWidthSizeClass.Compact,
            onTransactionClick = {},
            onPopulateMockData = {},
            onClearData = {},
            onToggleThemeMode = {}
        )
    }
}

@Preview(name = "Dark Mode Overview", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewOverviewDark() {
    FinanceAppTheme(darkTheme = true) {
        OverviewScreen(
            uiState = OverviewUiState.Content(sampleOverview),
            themeMode = ThemeMode.DARK,
            windowWidthSizeClass = WindowWidthSizeClass.Compact,
            onTransactionClick = {},
            onPopulateMockData = {},
            onClearData = {},
            onToggleThemeMode = {}
        )
    }
}

@Preview(name = "Empty State Light", showBackground = true)
@Composable
fun PreviewOverviewEmptyLight() {
    FinanceAppTheme(darkTheme = false) {
        OverviewScreen(
            uiState = OverviewUiState.Empty,
            themeMode = ThemeMode.LIGHT,
            windowWidthSizeClass = WindowWidthSizeClass.Compact,
            onTransactionClick = {},
            onPopulateMockData = {},
            onClearData = {},
            onToggleThemeMode = {}
        )
    }
}

@Preview(name = "Loading Skeleton Light", showBackground = true)
@Composable
fun PreviewOverviewLoadingLight() {
    FinanceAppTheme(darkTheme = false) {
        OverviewScreen(
            uiState = OverviewUiState.Loading,
            themeMode = ThemeMode.LIGHT,
            windowWidthSizeClass = WindowWidthSizeClass.Compact,
            onTransactionClick = {},
            onPopulateMockData = {},
            onClearData = {},
            onToggleThemeMode = {}
        )
    }
}

@Preview(name = "Transaction Details Light", showBackground = true)
@Composable
fun PreviewTransactionDetailsLight() {
    FinanceAppTheme(darkTheme = false) {
        TransactionDetailScreen(
            transaction = sampleTransaction,
            onBackClick = {}
        )
    }
}

@Preview(name = "Transaction Details Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewTransactionDetailsDark() {
    FinanceAppTheme(darkTheme = true) {
        TransactionDetailScreen(
            transaction = sampleTransaction,
            onBackClick = {}
        )
    }
}

@Preview(name = "Balance Card Component", showBackground = true)
@Composable
fun PreviewBalanceCard() {
    FinanceAppTheme(darkTheme = false) {
        BalanceSummaryCard(
            totalBalance = sampleOverview.totalBalance,
            monthlyIncome = sampleOverview.monthlyIncome,
            monthlyExpense = sampleOverview.monthlyExpense
        )
    }
}

@Preview(name = "Category Distribution Component", showBackground = true)
@Composable
fun PreviewCategoryDistribution() {
    FinanceAppTheme(darkTheme = false) {
        CategoryDistributionSection(categoryExpenses = sampleOverview.categoryExpenses)
    }
}
