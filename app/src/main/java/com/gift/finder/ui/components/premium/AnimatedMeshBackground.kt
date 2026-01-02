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
import com.gift.finder.domain.model.CosmicAura
import com.gift.finder.ui.theme.*

@Composable
fun AnimatedMeshBackground(
    aura: CosmicAura = LocalCosmicAura.current
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mesh")
    
    val color1 by infiniteTransition.animateColor(
        initialValue = aura.colors[0].copy(alpha = 0.2f),
        targetValue = aura.colors[1].copy(alpha = 0.3f),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "c1"
    )

    val color2 by infiniteTransition.animateColor(
        initialValue = aura.colors[2].copy(alpha = 0.1f),
        targetValue = aura.colors[3].copy(alpha = 0.2f),
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "c2"
    )

    // Cosmic Stars Logic
    val starCount = 60
    val starPositions = remember {
        List(starCount) {
            Offset(
                x = Math.random().toFloat(),
                y = Math.random().toFloat()
            )
        }
    }
    
    val starAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "stars"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Base Mesh Gradients
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(color1, Color.Transparent),
                center = center.copy(x = size.width * 0.2f, y = size.height * 0.3f),
                radius = size.width * 1.2f
            )
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(color2, Color.Transparent),
                center = center.copy(x = size.width * 0.8f, y = size.height * 0.7f),
                radius = size.width * 1.5f
            )
        )
        
        // Render Cosmic Particles (Stars)
        starPositions.forEachIndexed { index, pos ->
            val x = pos.x * size.width
            val y = pos.y * size.height
            val radius = if (index % 5 == 0) 2.dp.toPx() else 1.dp.toPx()
            
            // Twinkle effect variation
            val individualAlpha = (starAlpha * (0.5f + (index % 10) / 20f)).coerceIn(0f, 1f)
            
            drawCircle(
                color = Color.White.copy(alpha = individualAlpha),
                radius = radius,
                center = Offset(x, y)
            )
            
            // Subtle Glow Path
            if (index % 10 == 0) {
                drawCircle(
                    color = aura.primaryColor.copy(alpha = individualAlpha * 0.5f),
                    radius = radius * 3f,
                    center = Offset(x, y)
                )
            }
        }
    }
}
