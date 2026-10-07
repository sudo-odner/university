package com.example.lab3.presentation.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val translateAnimation = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnimation.value, y = translateAnimation.value)
    )

    background(brush)
}

@Composable
fun ShimmerPlaceholder(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp)
) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = shape
            )
            .shimmerEffect()
    )
}

@Composable
fun OverviewSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Balance skeleton card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                ShimmerPlaceholder(modifier = Modifier.fillMaxWidth(0.4f).height(16.dp))
                Spacer(modifier = Modifier.height(14.dp))
                ShimmerPlaceholder(modifier = Modifier.fillMaxWidth(0.7f).height(38.dp))
                Spacer(modifier = Modifier.height(24.dp))
                ShimmerPlaceholder(modifier = Modifier.fillMaxWidth().height(48.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        ShimmerPlaceholder(modifier = Modifier.fillMaxWidth(0.35f).height(22.dp))
        Spacer(modifier = Modifier.height(12.dp))

        // Accounts horizontal scroll skeleton
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Box(modifier = Modifier.shimmerEffect())
        }

        Spacer(modifier = Modifier.height(24.dp))
        ShimmerPlaceholder(modifier = Modifier.fillMaxWidth(0.5f).height(22.dp))
        Spacer(modifier = Modifier.height(12.dp))

        // Transaction list skeletons
        repeat(4) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .height(68.dp),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                Box(modifier = Modifier.shimmerEffect())
            }
        }
    }
}
