package com.gift.finder.ui.components.premium

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gift.finder.ui.theme.GlassBorder
import com.gift.finder.ui.theme.GlassWhite

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    borderWidth: Dp = 1.dp,
    border: BorderStroke? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            )
    ) {
        // Blur Background Effect
        Box(
            modifier = Modifier
                .matchParentSize()
                .blur(20.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            GlassWhite.copy(alpha = 0.4f),
                            GlassWhite.copy(alpha = 0.1f)
                        )
                    )
                )
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (border != null) {
                        Modifier.border(border, RoundedCornerShape(cornerRadius))
                    } else {
                        Modifier.border(
                            width = borderWidth,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    GlassBorder.copy(alpha = 0.6f),
                                    GlassBorder.copy(alpha = 0.1f)
                                )
                            ),
                            shape = RoundedCornerShape(cornerRadius)
                        )
                    }
                )
                .padding(16.dp),
            content = content
        )
    }
}
