package com.gift.finder.ui.components.premium

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.gift.finder.ui.theme.*
import kotlin.random.Random

/**
 * A simple confetti particle effect.
 */
@Composable
fun ConfettiEffect(
    modifier: Modifier = Modifier,
    durationMillis: Int = 3000
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.toFloat()
    val screenHeight = configuration.screenHeightDp.toFloat()
    
    val particles = remember {
        List(50) {
            ConfettiParticle(
                x = Random.nextFloat() * screenWidth,
                y = -Random.nextFloat() * 100f,
                color = listOf(GiftPink, GiftPurple, GiftBlue, GiftGreen, GiftOrange, GiftRed).random(),
                size = Random.nextFloat() * 10f + 5f,
                speed = Random.nextFloat() * 200f + 100f,
                rotationSpeed = Random.nextFloat() * 5f - 2.5f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { particle ->
            val currentY = (particle.y + progress * particle.speed * 10f) % (screenHeight * 1.5f)
            val currentRotation = progress * 360f * particle.rotationSpeed
            
            withTransform({
                rotate(currentRotation, pivot = Offset(particle.x, currentY))
            }) {
                drawRect(
                    color = particle.color,
                    topLeft = Offset(particle.x, currentY),
                    size = androidx.compose.ui.geometry.Size(particle.size, particle.size)
                )
            }
        }
    }
}

private data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val color: Color,
    val size: Float,
    val speed: Float,
    val rotationSpeed: Float
)
