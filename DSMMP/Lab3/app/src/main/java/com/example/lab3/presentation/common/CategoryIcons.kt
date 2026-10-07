package com.example.lab3.presentation.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.lab3.domain.model.Category

fun getCategoryIcon(category: Category): ImageVector {
    return when (category) {
        Category.SUPERMARKETS -> Icons.Outlined.ShoppingCart
        Category.RESTAURANTS -> Icons.Outlined.Restaurant
        Category.TRANSPORT -> Icons.Outlined.DirectionsCar
        Category.HEALTH -> Icons.Outlined.MedicalServices
        Category.ENTERTAINMENT -> Icons.Outlined.SportsEsports
        Category.HOUSING -> Icons.Outlined.Home
        Category.SALARY -> Icons.Outlined.Payments
        Category.INVESTMENTS -> Icons.Outlined.TrendingUp
        Category.OTHER -> Icons.Outlined.MoreHoriz
    }
}
