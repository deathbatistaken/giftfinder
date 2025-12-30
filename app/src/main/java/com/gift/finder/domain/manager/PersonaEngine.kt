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
    private val archetypeManager: ArchetypeManager
) {

    /**
     * Generates a descriptive summary for a person.
     */
    fun generatePersonaSummary(person: Person): String {
        val archetype = archetypeManager.findDominantArchetype(person)
        val interests = person.interests
        
        return when {
            interests.isEmpty() -> "The Mysterious Soul"
            archetype != null -> {
                val adjective = getAdjective(interests)
                "The $adjective ${archetype.title}"
            }
            else -> {
                val adjective = getAdjective(interests)
                "The $adjective Enthusiast"
            }
        }
    }

    private fun getAdjective(interests: List<String>): String {
        val techKeywords = listOf("tech", "gadget", "software", "gaming", "pc", "coding", "ai", "hardware")
        val creativeKeywords = listOf("art", "music", "design", "painting", "creative", "craft", "photography", "cinema")
        val outdoorKeywords = listOf("nature", "hiking", "travel", "camping", "outdoor", "fitness", "sport", "yoga")
        val intellectualKeywords = listOf("reading", "science", "history", "learning", "books", "philosophy", "physics")
        val lifestyleKeywords = listOf("cooking", "fashion", "decor", "wellness", "coffee", "wine", "dining")

        return when {
            interests.any { i -> techKeywords.any { k -> i.contains(k, ignoreCase = true) } } -> "Visionary"
            interests.any { i -> creativeKeywords.any { k -> i.contains(k, ignoreCase = true) } } -> "Inspired"
            interests.any { i -> outdoorKeywords.any { k -> i.contains(k, ignoreCase = true) } } -> "Fearless"
            interests.any { i -> intellectualKeywords.any { k -> i.contains(k, ignoreCase = true) } } -> "Erudite"
            interests.any { i -> lifestyleKeywords.any { k -> i.contains(k, ignoreCase = true) } } -> "Sophisticated"
            interests.size > 5 -> "Eclectic"
            interests.size > 3 -> "Dynamic"
            else -> "Captivating"
        }
    }
}
