package com.gift.finder.data.repository

import android.content.Context
import com.gift.finder.data.local.dao.GiftHistoryDao
import com.gift.finder.data.local.dao.RejectedGiftDao
import com.gift.finder.data.local.entities.RejectedGiftEntity
import com.gift.finder.domain.model.Archetype
import com.gift.finder.domain.model.ArchetypeCategory
import com.gift.finder.domain.model.BudgetRange
import com.gift.finder.domain.model.GiftCategory
import com.gift.finder.domain.model.GiftStyle
import com.gift.finder.domain.model.GiftSuggestion
import com.gift.finder.domain.model.Person
import com.gift.finder.domain.model.RejectionReason
import com.gift.finder.domain.model.Season
import com.gift.finder.domain.model.StoreType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for gift suggestions and categories.
 */
@Singleton
class GiftRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val giftHistoryDao: GiftHistoryDao,
    private val rejectedGiftDao: RejectedGiftDao
) {
    private val gson = Gson()
    private var cachedCategories: List<GiftCategory>? = null
    private var cachedArchetypes: List<Archetype>? = null

    /**
     * Get all gift categories from JSON asset.
     */
    fun getGiftCategories(): List<GiftCategory> {
        if (cachedCategories != null) return cachedCategories!!
        
        return try {
            val json = context.assets.open("gift_data.json")
                .bufferedReader().use { it.readText() }
            
            val type = object : TypeToken<GiftDataJson>() {}.type
            val data: GiftDataJson = gson.fromJson(json, type)
            
            cachedCategories = data.categories.map { it.toDomain() }
            cachedCategories!!
        } catch (e: Exception) {
            android.util.Log.e("GiftRepository", "Error loading gift categories", e)
            // Return fallback categories if JSON parsing fails
            getFallbackCategories()
        }
    }

    /**
     * Get all archetypes from JSON asset.
     */
    fun getArchetypes(): List<Archetype> {
        if (cachedArchetypes != null) return cachedArchetypes!!
        
        return try {
            val json = context.assets.open("archetypes.json")
                .bufferedReader().use { it.readText() }
            
            val type = object : TypeToken<ArchetypesJson>() {}.type
            val data: ArchetypesJson = gson.fromJson(json, type)
            
            cachedArchetypes = data.archetypes.map { it.toDomain() }
            cachedArchetypes!!
        } catch (e: Exception) {
            android.util.Log.e("GiftRepository", "Error loading archetypes", e)
            getFallbackArchetypes()
        }
    }

    /**
     * Get gift suggestions for a person.
     */
    suspend fun getSuggestions(
        person: Person,
        style: GiftStyle?,
        budget: BudgetRange?,
        creativityLevel: Float = 0.5f,
        maxResults: Int = 20
    ): List<GiftSuggestion> {
        val oneYearAgo = System.currentTimeMillis() - (365L * 24 * 60 * 60 * 1000)
        
        // Get recently purchased categories (to exclude)
        val purchasedCategories = giftHistoryDao.getRecentlyPurchasedCategories(
            person.id, oneYearAgo
        ).toSet()
        
        // Get rejected categories
        val rejectedCategories = rejectedGiftDao.getRejectedCategoryIds(person.id).toSet()
        
        // Get disliked tags from person
        val dislikedTags = person.dislikes.map { it.lowercase() }.toSet()
        
        return getGiftCategories()
            .filter { category ->
                // Filter out purchased categories
                category.id !in purchasedCategories &&
                // Filter out rejected categories
                category.id !in rejectedCategories &&
                // Filter by season
                category.isSeasonAppropriate() &&
                // Filter out disliked tags
                category.interestTags.none { it.lowercase() in dislikedTags } &&
                // Filter by budget if specified
                (budget == null || category.budgetRange == budget)
            }
            .map { category ->
                val score = category.calculateMatchScore(
                    person.interests,
                    style,
                    budget
                )
                
                // Apply creativity bonus/penalty
                // High creativity = more weight on "Random Spark", lower on exact interests
                val interestMultiplier = (10 * (1.5f - creativityLevel)).coerceIn(5f, 15f)
                val randomSpark = if (creativityLevel > 0.7f) (0..20).random(java.util.Random(category.id.hashCode() + System.currentTimeMillis() / 100000)) else 0
                
                val finalScore = (score * (1.0f - creativityLevel) + randomSpark + (category.interestTags.count { tag -> person.interests.any { it.contains(tag, true) } } * interestMultiplier)).toInt()

                val reasons = buildMatchReasons(category, person, style)
                
                // Simulate Price Radar: Deterministic per day and category (20% chance)
                val daySeed = System.currentTimeMillis() / (24 * 60 * 60 * 1000)
                val random = java.util.Random(category.id.hashCode() + daySeed)
                val priceDrop = if (random.nextFloat() < 0.2f) (5..30).random(kotlin.random.Random(random.nextLong())) else null
                
                 GiftSuggestion(category, finalScore.toDouble() / 150.0, reasons, priceDropPercentage = priceDrop)
            }
            .sortedByDescending { it.matchScore }
            .take(maxResults)
    }

    /**
     * Reject a gift suggestion (Shadow Learning).
     */
    suspend fun rejectSuggestion(
        personId: Long,
        categoryId: String,
        reason: RejectionReason
    ) {
        rejectedGiftDao.insertRejectedGift(
            RejectedGiftEntity(
                id = UUID.randomUUID().toString(),
                personId = personId,
                categoryId = categoryId,
                reason = reason,
                rejectedAt = System.currentTimeMillis()
            )
        )
    }

    /**
     * Clear a rejection if user changes their mind.
     */
    suspend fun clearRejection(personId: Long, categoryId: String) {
        rejectedGiftDao.clearRejection(personId, categoryId)
    }

    /**
     * Get random gift for roulette.
     */
    suspend fun getRandomGift(
        person: Person,
        style: GiftStyle?,
        budget: BudgetRange?
    ): GiftSuggestion? {
        val suggestions = getSuggestions(person, style, budget, 50)
        return suggestions.randomOrNull()
    }

    private fun buildMatchReasons(
        category: GiftCategory,
        person: Person,
        style: GiftStyle?
    ): List<String> {
        val reasons = mutableListOf<String>()
        
        val matchingInterests = category.interestTags.filter { tag ->
            person.interests.any { it.equals(tag, ignoreCase = true) }
        }
        if (matchingInterests.isNotEmpty()) {
            reasons.add("Matches interests: ${matchingInterests.joinToString(", ")}")
        }
        if (style != null && style in category.styleTags) {
            reasons.add("Matches style: $style")
        }
        
        return reasons
    }

    private fun getFallbackCategories(): List<GiftCategory> {
        return listOf(
            GiftCategory(
                id = "tech_gadgets",
                title = "Tech Gadgets",
                description = "Latest technology accessories and gadgets",
                emoji = "📱",
                interestTags = listOf("technology", "gadgets", "electronics"),
                styleTags = listOf(GiftStyle.TECH, GiftStyle.PRACTICAL),
                budgetRange = BudgetRange.MEDIUM,
                seasons = listOf(Season.ALL),
                storeType = StoreType.AMAZON,
                searchQuery = "tech gadgets gifts"
            ),
            GiftCategory(
                id = "books",
                title = "Books",
                description = "Bestselling books and literature",
                emoji = "📚",
                interestTags = listOf("reading", "books", "literature"),
                styleTags = listOf(GiftStyle.CREATIVE, GiftStyle.SENTIMENTAL),
                budgetRange = BudgetRange.LOW,
                seasons = listOf(Season.ALL),
                storeType = StoreType.AMAZON,
                searchQuery = "bestseller books"
            )
        )
    }

    private fun getFallbackArchetypes(): List<Archetype> {
        return listOf(
            Archetype(
                id = "gamer",
                title = "Gamer",
                emoji = "🎮",
                description = "Loves video games and gaming culture",
                defaultInterests = listOf("gaming", "technology", "esports"),
                suggestedStyles = listOf(GiftStyle.FUN, GiftStyle.TECH),
                category = ArchetypeCategory.ENTERTAINMENT
            )
        )
    }
}

// JSON Parsing classes
private data class GiftDataJson(
    val categories: List<GiftCategoryJson>
)

private data class GiftCategoryJson(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val interestTags: List<String>,
    val styleTags: List<String>,
    val budgetRange: String,
    val seasons: List<String>?,
    val storeType: String?,
    val searchQuery: String,
    val imageUrl: String?
) {
    fun toDomain() = GiftCategory(
        id = id,
        title = title,
        description = description,
        emoji = emoji,
        interestTags = interestTags,
        styleTags = styleTags.mapNotNull { 
            try { GiftStyle.valueOf(it.uppercase()) } catch (e: Exception) { null }
        },
        budgetRange = try { BudgetRange.valueOf(budgetRange.uppercase()) } catch (e: Exception) { BudgetRange.MEDIUM },
        seasons = seasons?.mapNotNull { 
            try { Season.valueOf(it.uppercase()) } catch (e: Exception) { null }
        } ?: listOf(Season.ALL),
        storeType = try { StoreType.valueOf(storeType?.uppercase() ?: "AMAZON") } catch (e: Exception) { StoreType.AMAZON },
        searchQuery = searchQuery,
        imageUrl = imageUrl
    )
}

private data class ArchetypesJson(
    val archetypes: List<ArchetypeJson>
)

private data class ArchetypeJson(
    val id: String,
    val title: String,
    val emoji: String,
    val description: String,
    val defaultInterests: List<String>,
    val suggestedStyles: List<String>,
    val category: String
) {
    fun toDomain() = Archetype(
        id = id,
        title = title,
        emoji = emoji,
        description = description,
        defaultInterests = defaultInterests,
        suggestedStyles = suggestedStyles.mapNotNull { 
            try { GiftStyle.valueOf(it.uppercase()) } catch (e: Exception) { null }
        },
        category = try { ArchetypeCategory.valueOf(category.uppercase()) } catch (e: Exception) { ArchetypeCategory.ENTERTAINMENT }
    )
}
