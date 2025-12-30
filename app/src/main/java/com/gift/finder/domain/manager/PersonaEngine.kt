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
        val techKeywords = listOf("tech", "gadget", "software", "gaming", "pc", "coding")
        val creativeKeywords = listOf("art", "music", "design", "painting", "creative", "craft")
        val outdoorKeywords = listOf("nature", "hiking", "travel", "camping", "outdoor", "fitness")
        val intellectualKeywords = listOf("reading", "science", "history", "learning", "books")

        return when {
            interests.any { i -> techKeywords.any { k -> i.contains(k, ignoreCase = true) } } -> "Innovative"
            interests.any { i -> creativeKeywords.any { k -> i.contains(k, ignoreCase = true) } } -> "Artistic"
            interests.any { i -> outdoorKeywords.any { k -> i.contains(k, ignoreCase = true) } } -> "Adventurous"
            interests.any { i -> intellectualKeywords.any { k -> i.contains(k, ignoreCase = true) } } -> "Thoughtful"
            interests.size > 3 -> "Versatile"
            else -> "Passionate"
        }
    }
}
