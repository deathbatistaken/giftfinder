package com.gift.finder.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gift.finder.ui.theme.*

/**
 * Animated circular progress indicator.
 */
@Composable
fun AnimatedCircularProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    strokeWidth: Dp = 12.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    progressColor: Color = GiftPurple,
    content: @Composable () -> Unit = {}
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidthPx = strokeWidth.toPx()
            val arcSize = Size(this.size.width - strokeWidthPx, this.size.height - strokeWidthPx)
            val topLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2)

            // Background arc
            drawArc(
                color = backgroundColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )

            // Progress arc
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
        }

        content()
    }
}

/**
 * Animated counter for numbers.
 */
@Composable
fun AnimatedCounter(
    targetValue: Int,
    modifier: Modifier = Modifier
) {
    var previousValue by remember { mutableIntStateOf(0) }
    val animatedValue by animateIntAsState(
        targetValue = targetValue,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "counter"
    )

    LaunchedEffect(targetValue) {
        previousValue = targetValue
    }

    Text(
        text = animatedValue.toString(),
        style = MaterialTheme.typography.displaySmall,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

/**
 * Gradient card background.
 */
@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(GradientStart, GradientEnd),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.linearGradient(colors = colors)
            )
            .padding(16.dp),
        content = content
    )
}

/**
 * Pulsating animation wrapper.
 */
@Composable
fun PulsatingAnimation(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier.then(Modifier.size((40 * scale).dp)),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/**
 * Countdown timer display.
 */
@Composable
fun CountdownDisplay(
    days: Int,
    modifier: Modifier = Modifier
) {
    val (urgencyColor, emoji) = when {
        days == 0 -> GiftRed to "🎉"
        days <= 3 -> GiftOrange to "⏰"
        days <= 7 -> GiftBlue to "📅"
        else -> GiftGreen to "🎁"
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(urgencyColor.copy(alpha = 0.1f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = emoji, style = MaterialTheme.typography.titleMedium)
        Text(
            text = when (days) {
                0 -> "Today!"
                1 -> "Tomorrow"
                else -> "$days days"
            },
            style = MaterialTheme.typography.labelLarge,
            color = urgencyColor,
            fontWeight = FontWeight.Bold
        )
    }
}
