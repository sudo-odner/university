package com.example.lab3.presentation.overview.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.lab3.presentation.common.FinanceFormatters
import com.example.lab3.presentation.theme.financeColors

@Composable
fun BalanceSummaryCard(
    totalBalance: Double,
    monthlyIncome: Double,
    monthlyExpense: Double,
    modifier: Modifier = Modifier
) {
    var isBalanceHidden by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Общий баланс",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                )

                IconButton(
                    onClick = { isBalanceHidden = !isBalanceHidden },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isBalanceHidden) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = if (isBalanceHidden) "Показать баланс" else "Скрыть баланс",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isBalanceHidden) "••••••• ₽" else FinanceFormatters.formatMoney(totalBalance),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FlowIndicator(
                    label = "Доходы",
                    amount = if (isBalanceHidden) "•••••• ₽" else "+${FinanceFormatters.formatShortMoney(monthlyIncome)}",
                    icon = Icons.Outlined.ArrowDownward,
                    isIncome = true,
                    modifier = Modifier.weight(1f)
                )

                FlowIndicator(
                    label = "Расходы",
                    amount = if (isBalanceHidden) "•••••• ₽" else "-${FinanceFormatters.formatShortMoney(monthlyExpense)}",
                    icon = Icons.Outlined.ArrowUpward,
                    isIncome = false,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun FlowIndicator(
    label: String,
    amount: String,
    icon: ImageVector,
    isIncome: Boolean,
    modifier: Modifier = Modifier
) {
    val indicatorColor = if (isIncome) MaterialTheme.financeColors.income else MaterialTheme.financeColors.expense

    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(indicatorColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = indicatorColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = amount,
                    style = MaterialTheme.typography.labelLarge,
                    color = indicatorColor
                )
            }
        }
    }
}
