package com.gift.finder.utils

import com.gift.finder.domain.model.GiftSuggestion

/**
 * Utility to format gift wishlists for sharing.
 */
object ExportManager {

    /**
     * Formats a wishlist into a beautiful string for sharing.
     */
    fun formatWishlist(context: android.content.Context, personName: String, suggestions: List<GiftSuggestion>): String {
        if (suggestions.isEmpty()) return context.getString(com.gift.finder.R.string.share_empty_wishlist, personName)

        val sb = StringBuilder()
        sb.append(context.getString(com.gift.finder.R.string.share_header, personName))

        suggestions.forEachIndexed { index, suggestion ->
            val emoji = suggestion.category.emoji
            val title = suggestion.category.title
            
            sb.append("$emoji $title\n")
            sb.append(context.getString(com.gift.finder.R.string.share_store_link, suggestion.category.getStoreUrl()))
        }

        sb.append(context.getString(com.gift.finder.R.string.share_footer))
        return sb.toString()
    }
}
