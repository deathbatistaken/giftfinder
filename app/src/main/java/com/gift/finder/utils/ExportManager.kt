package com.gift.finder.utils

import com.gift.finder.domain.model.GiftSuggestion

/**
 * Utility to format gift wishlists for sharing.
 */
object ExportManager {

    /**
     * Formats a wishlist into a beautiful string for sharing.
     */
    fun formatWishlist(personName: String, suggestions: List<GiftSuggestion>): String {
        if (suggestions.isEmpty()) return "My wishlist for $personName is empty... for now! 🎁"

        val sb = StringBuilder()
        sb.append("✨ GIFT PORTAL: $personName ✨\n")
        sb.append("Curated ideas found via GiftFinder 🌠\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━\n\n")

        suggestions.forEachIndexed { index, suggestion ->
            val emoji = suggestion.category.emoji
            val title = suggestion.category.title
            
            sb.append("$emoji $title\n")
            sb.append("🛒 Get it here: ${suggestion.category.getStoreUrl()}\n\n")
        }

        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("Generated with ✨ GiftFinder ✨\n")
        sb.append("Find the perfect gift for everyone.")
        return sb.toString()
    }
}
