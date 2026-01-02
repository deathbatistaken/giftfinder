package com.gift.finder.ui.components.premium

import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.gift.finder.ui.theme.*
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Swipe action for swipeable cards.
 */
enum class SwipeAction {
    NONE,
    LEFT,
    RIGHT
}

/**
 * Swipeable card with reveal actions.
 * Swipe left to reject, swipe right to accept.
 */
@Composable
fun SwipeableActionCard(
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    modifier: Modifier = Modifier,
    leftActionColor: Color = GiftRed,
    rightActionColor: Color = GiftGreen,
    swipeThreshold: Float = 100f,
    content: @Composable () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val swipeThresholdPx = with(LocalDensity.current) { swipeThreshold.dp.toPx() }
    
    val animatedOffset by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "swipe_offset"
    )
    
    val rotation by animateFloatAsState(
        targetValue = (offsetX / swipeThresholdPx) * 5f,
        animationSpec = spring(),
        label = "rotation"
    )

    Box(modifier = modifier) {
        // Background reveal layer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .matchParentSize(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left action (reject)
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(80.dp),
                color = leftActionColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = leftActionColor,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Right action (accept)
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(80.dp),
                color = rightActionColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = rightActionColor,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
        
        // Main content
        Box(
            modifier = Modifier
                .offset { IntOffset(animatedOffset.roundToInt(), 0) }
                .graphicsLayer { rotationZ = rotation }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            when {
                                offsetX < -swipeThresholdPx -> {
                                    onSwipeLeft()
                                    offsetX = 0f
                                }
                                offsetX > swipeThresholdPx -> {
                                    onSwipeRight()
                                    offsetX = 0f
                                }
                                else -> {
                                    offsetX = 0f
                                }
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            offsetX += dragAmount
                        }
                    )
                }
        ) {
            content()
        }
    }
}

/**
 * Card with 3D tilt effect on long press.
 */
@Composable
fun TiltCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val rotationX by animateFloatAsState(
        targetValue = if (isPressed) 5f else 0f,
        animationSpec = spring(),
        label = "tilt_x"
    )
    
    val rotationY by animateFloatAsState(
        targetValue = if (isPressed) -5f else 0f,
        animationSpec = spring(),
        label = "tilt_y"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(),
        label = "scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                this.rotationX = rotationX
                this.rotationY = rotationY
                this.scaleX = scale
                this.scaleY = scale
                cameraDistance = 12f * density
            }
    ) {
        content()
    }
}

/**
 * Expandable card with smooth height animation.
 */
@Composable
fun ExpandableCard(
    isExpanded: Boolean,
    header: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    expandedContent: @Composable () -> Unit
) {
    val aura = LocalCosmicAura.current
    
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isExpanded) aura.primaryColor.copy(alpha = 0.4f) 
            else aura.primaryColor.copy(alpha = 0.2f)
        )
    ) {
        Column {
            header()
            
            androidx.compose.animation.AnimatedVisibility(visible = isExpanded) {
                Column {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = aura.primaryColor.copy(alpha = 0.2f)
                    )
                    expandedContent()
                }
            }
        }
    }
}

/**
 * Flip card with front and back content.
 */
@Composable
fun FlipCard(
    isFlipped: Boolean,
    modifier: Modifier = Modifier,
    front: @Composable () -> Unit,
    back: @Composable () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(500),
        label = "flip"
    )

    Box(
        modifier = modifier.graphicsLayer {
            rotationY = rotation
            cameraDistance = 12f * density
        }
    ) {
        if (rotation <= 90f) {
            front()
        } else {
            Box(modifier = Modifier.graphicsLayer { rotationY = 180f }) {
                back()
            }
        }
    }
}

/**
 * Shake animation wrapper for error states.
 */
@Composable
fun ShakeWrapper(
    isShaking: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val shakeOffset by animateFloatAsState(
        targetValue = if (isShaking) 1f else 0f,
        animationSpec = if (isShaking) {
            keyframes {
                durationMillis = 500
                0f at 0
                -10f at 50
                10f at 100
                -10f at 150
                10f at 200
                -5f at 250
                5f at 300
                0f at 350
            }
        } else {
            tween(0)
        },
        label = "shake"
    )

    Box(
        modifier = modifier.offset(x = shakeOffset.dp)
    ) {
        content()
    }
}
