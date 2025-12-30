package com.gift.finder.ui.components.premium

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gift.finder.ui.theme.GiftBlue
import com.gift.finder.ui.theme.GiftGreen
import com.gift.finder.ui.theme.GiftPurple

/**
 * A beautiful, animated radial chart for spending distribution.
 */
@Composable
fun CinematicRadialChart(
    portions: List<RadialPortion>,
    modifier: Modifier = Modifier,
    centerText: String = "",
    strokeWidth: Dp = 16.dp
) {
    val animationProgress = remember { Animatable(0f) }
    
    LaunchedEffect(portions) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
        )
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = size
            val radius = (minOf(canvasSize.width, canvasSize.height) / 2) - strokeWidth.toPx()
            val center = Offset(canvasSize.width / 2, canvasSize.height / 2)
            
            var startAngle = -90f
            
            portions.forEach { portion ->
                val sweepAngle = portion.percentage * 360f * animationProgress.value
                
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(portion.color.copy(alpha = 0.7f), portion.color),
                        center = center
                    ),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round),
                    size = Size(radius * 2, radius * 2),
                    topLeft = Offset(center.x - radius, center.y - radius)
                )
                
                startAngle += sweepAngle
            }
        }
        
        if (centerText.isNotEmpty()) {
            Text(
                text = centerText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 24.sp
            )
        }
    }
}

/**
 * Portions for the Radial Chart.
 */
data class RadialPortion(
    val percentage: Float,
    val color: Color,
    val label: String
)

/**
 * Animated bar for spending comparison.
 */
@Composable
fun AnimatedSpendingBar(
    label: String,
    value: Float, // 0f to 1f
    amountText: String,
    color: Color = GiftPurple,
    modifier: Modifier = Modifier
) {
    val barProgress = remember { Animatable(0f) }
    
    LaunchedEffect(value) {
        barProgress.animateTo(
            targetValue = value,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                amountText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Canvas(modifier = Modifier.fillMaxWidth().height(12.dp)) {
            val width = size.width
            val height = size.height
            
            // Track
            drawRoundRect(
                color = Color.Gray.copy(alpha = 0.1f),
                size = size,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(height / 2)
            )
            
            // Progress
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(color.copy(alpha = 0.5f), color)
                ),
                size = Size(width * barProgress.value, height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(height / 2)
            )
        }
    }
}
