package com.gift.finder.domain.model

/**
 * Gift category with tags and metadata for matching.
 */
data class GiftCategory(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val interestTags: List<String>,
    val styleTags: List<GiftStyle>,
    val budgetRange: BudgetRange,
    val seasons: List<Season> = listOf(Season.ALL),
    val storeType: StoreType = StoreType.AMAZON,
    val searchQuery: String,
    val imageUrl: String? = null
) {
    /**
     * Calculate match score against person's interests and preferences.
     */
    fun calculateMatchScore(
        personInterests: List<String>,
        selectedStyle: GiftStyle?,
        selectedBudget: BudgetRange?
    ): Int {
        var score = 0
        
        // Interest matching (0-50 points)
        val matchingInterests = interestTags.count { tag ->
            personInterests.any { interest ->
                interest.equals(tag, ignoreCase = true) ||
                interest.contains(tag, ignoreCase = true) ||
                tag.contains(interest, ignoreCase = true)
            }
        }
        score += matchingInterests * 10
        
        // Style matching (0-30 points)
        if (selectedStyle != null && selectedStyle in styleTags) {
            score += 30
        } else if (selectedStyle != null && styleTags.isNotEmpty()) {
            score += 10 // Partial match
        }
        
        // Budget matching (0-20 points)
        if (selectedBudget != null && selectedBudget == budgetRange) {
            score += 20
        }
        
        return score
    }
    
    /**
     * Check if this category is appropriate for current season.
     */
    fun isSeasonAppropriate(): Boolean {
        if (Season.ALL in seasons) return true
        return Season.current() in seasons
    }
    
    /**
     * Generate store URL for this gift category.
     */
    fun getStoreUrl(): String {
        return "${storeType.baseUrl}${java.net.URLEncoder.encode(searchQuery, "UTF-8")}"
    }
}
