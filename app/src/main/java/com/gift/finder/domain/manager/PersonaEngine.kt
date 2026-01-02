package com.gift.finder.domain.manager

import com.gift.finder.domain.model.Archetype
import com.gift.finder.domain.model.Person
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Engine to generate human-readable persona summaries for people.
 */
@Singleton
class PersonaEngine @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context,
    private val archetypeManager: ArchetypeManager
) {

    /**
     * Generates a descriptive summary for a person.
     */
    fun generatePersonaSummary(person: Person): String {
        val archetype = archetypeManager.findDominantArchetype(person)
        val interests = person.interests
        
        return when {
            interests.isEmpty() -> context.getString(com.gift.finder.R.string.persona_mysterious_soul)
            archetype != null -> {
                val adjective = getAdjective(interests)
                context.getString(com.gift.finder.R.string.persona_template_archetype, adjective, archetype.title)
            }
            else -> {
                val adjective = getAdjective(interests)
                context.getString(com.gift.finder.R.string.persona_template_enthusiast, adjective)
            }
        }
    }

    private fun getAdjective(interests: List<String>): String {
        val techKeywords = listOf("tech", "gadget", "software", "gaming", "pc", "coding", "ai", "hardware")
        val creativeKeywords = listOf("art", "music", "design", "painting", "creative", "craft", "photography", "cinema")
        val outdoorKeywords = listOf("nature", "hiking", "travel", "camping", "outdoor", "fitness", "sport", "yoga")
        val intellectualKeywords = listOf("reading", "science", "history", "learning", "books", "philosophy", "physics")
        val lifestyleKeywords = listOf("cooking", "fashion", "decor", "wellness", "coffee", "wine", "dining")
        val celestialKeywords = listOf("astro", "star", "space", "cosmic", "universe", "destiny", "mystery")
        val socialKeywords = listOf("party", "social", "friend", "event", "community", "vibrant")

        val resId = when {
            interests.any { i -> celestialKeywords.any { k -> i.contains(k, ignoreCase = true) } } -> com.gift.finder.R.string.persona_keyword_celestial
            interests.any { i -> techKeywords.any { k -> i.contains(k, ignoreCase = true) } } -> com.gift.finder.R.string.persona_keyword_tech
            interests.any { i -> creativeKeywords.any { k -> i.contains(k, ignoreCase = true) } } -> com.gift.finder.R.string.persona_keyword_creative
            interests.any { i -> outdoorKeywords.any { k -> i.contains(k, ignoreCase = true) } } -> com.gift.finder.R.string.persona_keyword_outdoor
            interests.any { i -> intellectualKeywords.any { k -> i.contains(k, ignoreCase = true) } } -> com.gift.finder.R.string.persona_keyword_intellectual
            interests.any { i -> lifestyleKeywords.any { k -> i.contains(k, ignoreCase = true) } } -> com.gift.finder.R.string.persona_keyword_lifestyle
            interests.any { i -> socialKeywords.any { k -> i.contains(k, ignoreCase = true) } } -> com.gift.finder.R.string.persona_keyword_radiant
            interests.size > 5 -> com.gift.finder.R.string.persona_keyword_eclectic
            interests.size > 3 -> com.gift.finder.R.string.persona_keyword_dynamic
            else -> com.gift.finder.R.string.persona_keyword_mystic
        }
        
        return context.getString(resId)
    }
}
