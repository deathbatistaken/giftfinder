package com.gift.finder.ui.components.premium

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gift.finder.ui.theme.*

/**
 * Gradient text with animated color flow.
 */
@Composable
fun GradientText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineMedium,
    colors: List<Color> = listOf(GiftPurple, GiftBlue, GiftPurple)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gradient_text")
    
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradient_offset"
    )

    Text(
        text = text,
        modifier = modifier,
        style = style.copy(
            brush = Brush.linearGradient(
                colors = colors,
                start = Offset(offset, 0f),
                end = Offset(offset + 500f, 0f)
            )
        ),
        fontWeight = FontWeight.Bold
    )
}

/**
 * Glowing text effect for important labels.
 */
@Composable
fun GlowingText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleLarge,
    glowColor: Color = GiftPurple
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Glow layer
        Text(
            text = text,
            style = style.copy(
                color = glowColor.copy(alpha = alpha),
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = glowColor.copy(alpha = alpha),
                    blurRadius = 20f
                )
            ),
            fontWeight = FontWeight.Bold
        )
        
        // Main text
        Text(
            text = text,
            style = style.copy(color = MaterialTheme.colorScheme.onBackground),
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Premium stat card with gradient background.
 */
@Composable
fun StatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    gradientColors: List<Color> = listOf(GiftPurple, GiftBlue)
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(colors = gradientColors)
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Animated progress ring with percentage.
 */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = GiftPurple,
    backgroundColor: Color = Color.LightGray.copy(alpha = 0.3f),
    strokeWidth: Float = 12f,
    showPercentage: Boolean = true
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val diameter = size.minDimension
            val radius = (diameter - strokeWidth) / 2
            val topLeft = Offset(
                (size.width - diameter) / 2 + strokeWidth / 2,
                (size.height - diameter) / 2 + strokeWidth / 2
            )
            
            // Background ring
            drawArc(
                color = backgroundColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = androidx.compose.ui.geometry.Size(diameter - strokeWidth, diameter - strokeWidth),
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = strokeWidth,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            )
            
            // Progress ring
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = topLeft,
                size = androidx.compose.ui.geometry.Size(diameter - strokeWidth, diameter - strokeWidth),
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = strokeWidth,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            )
        }
        
        if (showPercentage) {
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

/**
 * Animated badge with pop-in effect.
 */
@Composable
fun AnimatedBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = GiftRed,
    textColor: Color = Color.White
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "badge_scale"
    )

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Countdown timer display with urgency colors.
 */
@Composable
fun UrgencyCountdown(
    daysLeft: Int,
    modifier: Modifier = Modifier
) {
    val (color, label) = when {
        daysLeft <= 0 -> GiftRed to "Today!"
        daysLeft == 1 -> GiftOrange to "Tomorrow"
        daysLeft <= 3 -> GiftOrange to "$daysLeft days"
        daysLeft <= 7 -> GiftBlue to "$daysLeft days"
        else -> GiftGreen to "$daysLeft days"
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "urgency")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "urgency_alpha"
    )
    
    val shouldPulse = daysLeft <= 3

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = if (shouldPulse) alpha * 0.2f else 0.2f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
