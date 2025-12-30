package com.gift.finder.domain.model

/**
 * Represents a gift suggestion with matching score and reasons.
 */
data class GiftSuggestion(
    val category: GiftCategory,
    val matchScore: Double,
    val matchReasons: List<String> = emptyList(),
    val isPremiumLocked: Boolean = false
) {
    /**
     * Check if this suggestion is a good match (score > 0.5).
     */
    fun isGoodMatch(): Boolean = matchScore > 0.5

    /**
     * Check if this is an excellent match (score > 0.8).
     */
    fun isExcellentMatch(): Boolean = matchScore > 0.8
}
