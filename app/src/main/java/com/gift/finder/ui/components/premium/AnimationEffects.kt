package com.gift.finder.ui.components.premium

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.gift.finder.ui.theme.LocalCosmicAura
import kotlinx.coroutines.delay

/**
 * Premium pull-to-refresh container with cosmic styling.
 * Wraps content and provides a beautiful refresh animation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumPullToRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val aura = LocalCosmicAura.current
    val state = rememberPullToRefreshState()
    
    if (state.isRefreshing) {
        LaunchedEffect(true) {
            onRefresh()
        }
    }
    
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            state.startRefresh()
        } else {
            state.endRefresh()
        }
    }

    Box(
        modifier = modifier.nestedScroll(state.nestedScrollConnection)
    ) {
        content()
        
        PullToRefreshContainer(
            state = state,
            modifier = Modifier.align(Alignment.TopCenter),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = aura.primaryColor
        )
    }
}

/**
 * Animated floating action button with pulse effect.
 */
@Composable
fun PulsatingFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit
) {
    val aura = LocalCosmicAura.current
    val infiniteTransition = rememberInfiniteTransition(label = "fab_pulse")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fab_scale"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fab_alpha"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Outer pulse ring
        Surface(
            modifier = Modifier
                .size((56 * scale).dp)
                .graphicsLayer { this.alpha = alpha },
            shape = CircleShape,
            color = aura.primaryColor.copy(alpha = 0.3f)
        ) {}
        
        // Main FAB
        FloatingActionButton(
            onClick = onClick,
            containerColor = aura.primaryColor,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            icon()
        }
    }
}

/**
 * Animated counter with smooth number transitions.
 */
@Composable
fun AnimatedNumberCounter(
    targetValue: Int,
    modifier: Modifier = Modifier,
    prefix: String = "",
    suffix: String = "",
    durationMillis: Int = 1000
) {
    var displayValue by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(targetValue) {
        val startValue = displayValue
        val difference = targetValue - startValue
        val steps = 60
        val stepDuration = durationMillis / steps
        
        for (i in 1..steps) {
            delay(stepDuration.toLong())
            displayValue = startValue + (difference * i / steps)
        }
        displayValue = targetValue
    }

    Text(
        text = "$prefix$displayValue$suffix",
        modifier = modifier,
        style = MaterialTheme.typography.headlineMedium
    )
}

/**
 * Bouncing dot loading indicator.
 */
@Composable
fun BouncingDotsIndicator(
    modifier: Modifier = Modifier,
    dotCount: Int = 3,
    dotSize: Float = 12f,
    spaceBetween: Float = 8f
) {
    val aura = LocalCosmicAura.current
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spaceBetween.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(dotCount) { index ->
            val delay = index * 100
            
            val offsetY by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -dotSize / 2,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 300,
                        delayMillis = delay,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot_$index"
            )
            
            Surface(
                modifier = Modifier
                    .size(dotSize.dp)
                    .offset(y = offsetY.dp),
                shape = CircleShape,
                color = aura.primaryColor
            ) {}
        }
    }
}

/**
 * Spinning loading icon with smooth rotation.
 */
@Composable
fun SpinningLoadingIcon(
    modifier: Modifier = Modifier
) {
    val aura = LocalCosmicAura.current
    val infiniteTransition = rememberInfiniteTransition(label = "spin")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Icon(
        imageVector = Icons.Default.Refresh,
        contentDescription = null,
        tint = aura.primaryColor,
        modifier = modifier
            .size(32.dp)
            .rotate(rotation)
    )
}

/**
 * Typewriter text effect for dramatic reveals.
 */
@Composable
fun TypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    typingSpeed: Long = 50L,
    onComplete: () -> Unit = {}
) {
    var displayText by remember { mutableStateOf("") }
    
    LaunchedEffect(text) {
        displayText = ""
        text.forEachIndexed { index, _ ->
            delay(typingSpeed)
            displayText = text.substring(0, index + 1)
        }
        onComplete()
    }

    Text(
        text = displayText,
        modifier = modifier,
        style = MaterialTheme.typography.bodyLarge
    )
}
