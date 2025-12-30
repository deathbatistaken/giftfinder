package com.gift.finder.domain.model

import androidx.compose.ui.graphics.Color
import com.gift.finder.ui.theme.*

/**
 * Defines color themes for the animated cosmic background.
 */
enum class CosmicAura(
    val id: String,
    val title: String,
    val colors: List<Color>,
    val emoji: String
) {
    NEBULA(
        id = "nebula",
        title = "Nebula Purple",
        colors = listOf(CosmicPurple, GiftPurple, Color(0xFF9B59B6), Color(0xFFE056FD)),
        emoji = "🌌"
    ),
    SUPERNOVA(
        id = "supernova",
        title = "Supernova Red",
        colors = listOf(GiftRed, GiftOrange, Color(0xFFFF4757), Color(0xFFEA2027)),
        emoji = "💥"
    ),
    DEEP_SPACE(
        id = "deep_space",
        title = "Deep Space Blue",
        colors = listOf(CosmicBlue, GiftBlue, Color(0xFF1E90FF), Color(0xFF4834D4)),
        emoji = "☄️"
    ),
    AURORA(
        id = "aurora",
        title = "Aurora Green",
        colors = listOf(GiftGreen, Color(0xFF26DE81), Color(0xFF20BF6B), Color(0xFF05C46B)),
        emoji = "🌠"
    ),
    GOLDEN_GALAXY(
        id = "golden",
        title = "Golden Galaxy",
        colors = listOf(LuxuryGoldStart, LuxuryGoldMid, LuxuryGoldEnd, Color(0xFFF1C40F)),
        emoji = "✨"
    );

    companion object {
        fun fromId(id: String): CosmicAura = entries.find { it.id == id } ?: NEBULA
    }
}
