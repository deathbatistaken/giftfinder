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
        sb.append("✨ Gift Portal for $personName ✨\n")
        sb.append("Check out these curated gift ideas from GiftFinder 🎁\n\n")

        suggestions.forEachIndexed { index, suggestion ->
            val emoji = suggestion.category.emoji
            val title = suggestion.category.title
            val desc = suggestion.category.description
            
            sb.append("${index + 1}. $emoji $title\n")
            sb.append("   $desc\n")
            sb.append("   🔗 Shop: ${suggestion.category.getStoreUrl()}\n\n")
        }

        sb.append("Shared via GiftFinder 🌠")
        return sb.toString()
    }
}
