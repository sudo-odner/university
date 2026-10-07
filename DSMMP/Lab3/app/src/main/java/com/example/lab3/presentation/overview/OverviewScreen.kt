package com.example.lab3.presentation.overview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lab3.domain.model.FinancialOverview
import com.example.lab3.presentation.common.EmptyStateView
import com.example.lab3.presentation.common.OverviewSkeleton
import com.example.lab3.presentation.overview.components.AccountsSection
import com.example.lab3.presentation.overview.components.BalanceSummaryCard
import com.example.lab3.presentation.overview.components.CategoryDistributionSection
import com.example.lab3.presentation.overview.components.TransactionItem
import com.example.lab3.presentation.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(
    uiState: OverviewUiState,
    themeMode: ThemeMode,
    windowWidthSizeClass: WindowWidthSizeClass,
    onTransactionClick: (String) -> Unit,
    onPopulateMockData: () -> Unit,
    onClearData: () -> Unit,
    onToggleThemeMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Личные финансы",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Октябрь 2026",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Quick state trigger: Clear data to test Empty state
                    IconButton(
                        onClick = onClearData,
                        enabled = uiState is OverviewUiState.Content
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Очистить данные (проверка пустого состояния)",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Reload/Populate data
                    IconButton(onClick = onPopulateMockData) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Обновить данные",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Theme Mode toggle button (System -> Light -> Dark)
                    IconButton(onClick = onToggleThemeMode) {
                        val icon = when (themeMode) {
                            ThemeMode.SYSTEM -> Icons.Outlined.BrightnessAuto
                            ThemeMode.LIGHT -> Icons.Outlined.LightMode
                            ThemeMode.DARK -> Icons.Outlined.DarkMode
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = "Сменить режим темы",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Smooth animated state transitions
            AnimatedVisibility(
                visible = uiState is OverviewUiState.Loading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                OverviewSkeleton()
            }

            AnimatedVisibility(
                visible = uiState is OverviewUiState.Empty,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                EmptyStateView(onPopulateMockData = onPopulateMockData)
            }

            AnimatedVisibility(
                visible = uiState is OverviewUiState.Content,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                val overview = (uiState as? OverviewUiState.Content)?.overview
                if (overview != null) {
                    if (windowWidthSizeClass == WindowWidthSizeClass.Expanded) {
                        ExpandedOverviewContent(
                            overview = overview,
                            onTransactionClick = onTransactionClick
                        )
                    } else {
                        CompactOverviewContent(
                            overview = overview,
                            onTransactionClick = onTransactionClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CompactOverviewContent(
    overview: FinancialOverview,
    onTransactionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                BalanceSummaryCard(
                    totalBalance = overview.totalBalance,
                    monthlyIncome = overview.monthlyIncome,
                    monthlyExpense = overview.monthlyExpense
                )
            }
        }

        item {
            AccountsSection(accounts = overview.accounts)
        }

        item {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                CategoryDistributionSection(categoryExpenses = overview.categoryExpenses)
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Последние операции",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${overview.recentTransactions.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        items(overview.recentTransactions, key = { it.id }) { tx ->
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                TransactionItem(
                    transaction = tx,
                    onClick = { onTransactionClick(tx.id) }
                )
            }
        }
    }
}

@Composable
fun ExpandedOverviewContent(
    overview: FinancialOverview,
    onTransactionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Left Column (45%): Balance, Accounts carousel, Category analysis
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BalanceSummaryCard(
                totalBalance = overview.totalBalance,
                monthlyIncome = overview.monthlyIncome,
                monthlyExpense = overview.monthlyExpense
            )
            AccountsSection(accounts = overview.accounts)
            CategoryDistributionSection(categoryExpenses = overview.categoryExpenses)
        }

        // Right Column (55%): Full Transaction List with higher density
        Column(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "История операций",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${overview.recentTransactions.size} операций",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(overview.recentTransactions, key = { it.id }) { tx ->
                    TransactionItem(
                        transaction = tx,
                        onClick = { onTransactionClick(tx.id) }
                    )
                }
            }
        }
    }
}
