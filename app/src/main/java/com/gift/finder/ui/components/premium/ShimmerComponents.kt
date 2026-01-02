package com.gift.finder.ui.components.premium

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Premium shimmer effect for loading states.
 * Creates a flowing gradient animation that gives a premium loading feel.
 */
@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    widthOfShadowBrush: Int = 500,
    angleOfAxisY: Float = 270f,
    durationMillis: Int = 1000
) {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.3f),
        Color.LightGray.copy(alpha = 0.5f),
        Color.LightGray.copy(alpha = 1.0f),
        Color.LightGray.copy(alpha = 0.5f),
        Color.LightGray.copy(alpha = 0.3f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = (durationMillis + widthOfShadowBrush).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnimation - widthOfShadowBrush, 0f),
        end = Offset(translateAnimation, angleOfAxisY)
    )

    Box(
        modifier = modifier
            .background(brush = brush)
    )
}

/**
 * Skeleton loader for a single line of text.
 */
@Composable
fun SkeletonText(
    modifier: Modifier = Modifier,
    width: Dp = 120.dp,
    height: Dp = 16.dp
) {
    ShimmerEffect(
        modifier = modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(4.dp))
    )
}

/**
 * Skeleton loader for circular avatars.
 */
@Composable
fun SkeletonCircle(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    ShimmerEffect(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
    )
}

/**
 * Skeleton loader for rectangular cards.
 */
@Composable
fun SkeletonCard(
    modifier: Modifier = Modifier,
    height: Dp = 100.dp
) {
    ShimmerEffect(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(16.dp))
    )
}

/**
 * Skeleton loader for a person card layout.
 */
@Composable
fun SkeletonPersonCard(
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SkeletonCircle(size = 56.dp)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                SkeletonText(width = 140.dp, height = 18.dp)
                Spacer(modifier = Modifier.height(8.dp))
                SkeletonText(width = 100.dp, height = 14.dp)
            }
            SkeletonCircle(size = 32.dp)
        }
    }
}

/**
 * Skeleton loader for a gift suggestion card.
 */
@Composable
fun SkeletonSuggestionCard(
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SkeletonCircle(size = 48.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    SkeletonText(width = 160.dp, height = 18.dp)
                    Spacer(modifier = Modifier.height(6.dp))
                    SkeletonText(width = 200.dp, height = 14.dp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SkeletonText(width = 60.dp, height = 24.dp)
                SkeletonText(width = 80.dp, height = 24.dp)
                SkeletonText(width = 70.dp, height = 24.dp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            ShimmerEffect(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(22.dp))
            )
        }
    }
}

/**
 * Skeleton loader for the home screen.
 */
@Composable
fun SkeletonHomeScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Summary card skeleton
        SkeletonCard(height = 80.dp)
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Person cards skeleton
        repeat(4) {
            SkeletonPersonCard()
        }
    }
}

/**
 * Skeleton loader for suggestions screen.
 */
@Composable
fun SkeletonSuggestionsScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Filter chips skeleton
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(4) {
                SkeletonText(width = 70.dp, height = 32.dp)
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Suggestion cards skeleton
        repeat(3) {
            SkeletonSuggestionCard()
        }
    }
}
