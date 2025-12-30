package com.gift.finder.ui.components.premium

import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.gift.finder.R
import com.gift.finder.domain.model.GiftSuggestion
import com.gift.finder.domain.model.RejectionReason
import kotlin.math.roundToInt

/**
 * A swipeable card component for gift suggestions.
 * Swiping Left: Reject
 * Swiping Right: Interested/Save
 */
@Composable
fun SwipeableGiftCard(
    suggestion: GiftSuggestion,
    onSwipedLeft: (RejectionReason) -> Unit,
    onSwipedRight: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    
    val scope = rememberCoroutineScope()
    val animatedOffsetX = animateFloatAsState(
        targetValue = offsetX,
        label = "offsetX"
    )
    val animatedOffsetY = animateFloatAsState(
        targetValue = offsetY,
        label = "offsetY"
    )

    // Calculate rotation based on horizontal offset
    val rotation = (offsetX / 60).coerceIn(-15f, 15f)
    
    // Threshold for swipe action
    val swipeThreshold = 500f

    Box(
        modifier = modifier
            .offset { IntOffset(animatedOffsetX.value.roundToInt(), animatedOffsetY.value.roundToInt()) }
            .graphicsLayer {
                rotationZ = rotation
                alpha = 1f - (kotlin.math.abs(offsetX) / 1000f).coerceIn(0f, 0.5f)
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { },
                    onDragEnd = {
                        if (offsetX > swipeThreshold) {
                            // Swipe Right
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSwipedRight()
                        } else if (offsetX < -swipeThreshold) {
                            // Swipe Left
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSwipedLeft(RejectionReason.NOT_INTERESTED) // Default reason
                        } else {
                            // Reset
                            offsetX = 0f
                            offsetY = 0f
                        }
                    },
                    onDragCancel = {
                        offsetX = 0f
                        offsetY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                )
            }
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth().height(400.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = suggestion.category.emoji,
                    style = MaterialTheme.typography.displayLarge
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = suggestion.category.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = suggestion.category.description,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        stringResource(R.string.not_interested_swipe),
                        color = Color.Red.copy(alpha = ((-offsetX) / swipeThreshold).coerceIn(0f, 1f)),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        stringResource(R.string.interested),
                        color = Color.Green.copy(alpha = ((offsetX) / swipeThreshold).coerceIn(0f, 1f)),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
