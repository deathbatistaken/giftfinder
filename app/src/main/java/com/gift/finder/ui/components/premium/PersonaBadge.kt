package com.gift.finder.ui.components.premium

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gift.finder.domain.model.Archetype
import com.gift.finder.ui.theme.LocalCosmicAura
import androidx.compose.ui.res.stringResource

/**
 * A premium animated badge for user archetypes.
 */
@Composable
fun PersonaBadge(
    archetype: Archetype,
    modifier: Modifier = Modifier
) {
    val aura = LocalCosmicAura.current
    val infiniteTransition = rememberInfiniteTransition(label = "badge_aura")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        contentAlignment = Alignment.Center
    ) {
        // Rotating Aura Background
        Box(
            modifier = Modifier
                .size(width = 160.dp, height = 48.dp)
                .rotate(rotation)
                .background(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            aura.primaryColor.copy(alpha = 0.1f),
                            aura.primaryColor.copy(alpha = 0.6f),
                            aura.primaryColor.copy(alpha = 0.1f)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .blur(8.dp)
        )

        // Main Badge Surface
        val isDark = androidx.compose.foundation.isSystemInDarkTheme()
        Surface(
            color = if (isDark) aura.primaryColor.copy(alpha = 0.15f) else aura.primaryColor.copy(alpha = 0.2f),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                Brush.linearGradient(
                    colors = listOf(
                        aura.primaryColor.copy(alpha = 0.8f),
                        aura.primaryColor.copy(alpha = 0.2f),
                        aura.primaryColor.copy(alpha = 0.6f)
                    )
                )
            ),
            modifier = Modifier.wrapContentSize()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = archetype.emoji,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = archetype.title.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = if (isDark) Color.White else aura.primaryColor,
                        letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified
                    )
                    Text(
                        text = stringResource(com.gift.finder.R.string.rank_cosmic),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White.copy(alpha = 0.7f) else aura.primaryColor.copy(alpha = 0.6f),
                        letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified
                    )
                }
            }
        }
    }
}

// Add blur extension if not available or just use Box with background
// Since blur might need API 31+, I'll use a safer approach if needed, 
// but for Perfection Phase, we usually aim for high-end.
// I'll check if Modifier.blur exists (it should in Compose 1.1+)
import androidx.compose.ui.draw.blur
