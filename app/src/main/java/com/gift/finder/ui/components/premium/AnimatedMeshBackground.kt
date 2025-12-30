package com.gift.finder.ui.components.premium

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import com.gift.finder.ui.theme.*

@Composable
fun AnimatedMeshBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "mesh")
    
    val color1 by infiniteTransition.animateColor(
        initialValue = CosmicPurple.copy(alpha = 0.2f),
        targetValue = GiftPurple.copy(alpha = 0.3f),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "c1"
    )

    val color2 by infiniteTransition.animateColor(
        initialValue = CosmicBlue.copy(alpha = 0.1f),
        targetValue = GiftBlue.copy(alpha = 0.2f),
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "c2"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(color1, Color.Transparent),
                center = center.copy(x = size.width * 0.2f, y = size.height * 0.3f),
                radius = size.width * 0.8f
            )
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(color2, Color.Transparent),
                center = center.copy(x = size.width * 0.8f, y = size.height * 0.7f),
                radius = size.width * 0.9f
            )
        )
    }
}
