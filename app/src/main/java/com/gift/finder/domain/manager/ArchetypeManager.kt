package com.gift.finder.domain.manager

import android.content.Context
import com.gift.finder.domain.model.Archetype
import com.gift.finder.domain.model.Person
import com.gift.finder.utils.JsonParser
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager to handle archetype matching logic.
 */
@Singleton
class ArchetypeManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val jsonParser: JsonParser
) {
    private var cachedArchetypes: List<Archetype>? = null

    fun getArchetypes(): List<Archetype> {
        if (cachedArchetypes == null) {
            cachedArchetypes = jsonParser.parseArchetypes("archetypes.json")
        }
        return cachedArchetypes ?: emptyList()
    }

    /**
     * Finds the dominant archetype for a person based on their interests.
     */
    fun findDominantArchetype(person: Person): Archetype? {
        val archetypes = getArchetypes()
        if (person.interests.isEmpty()) return null
        
        return archetypes.maxByOrNull { archetype ->
            archetype.defaultInterests.count { tag ->
                person.interests.any { interest ->
                    interest.equals(tag, ignoreCase = true) ||
                    interest.contains(tag, ignoreCase = true) ||
                    tag.contains(interest, ignoreCase = true)
                }
            }
        }?.takeIf { archetype ->
            // Must match at least one interest or have a high overlap
            val matchCount = archetype.defaultInterests.count { tag ->
                person.interests.any { it.contains(tag, ignoreCase = true) }
            }
            matchCount > 0
        }
    }
}
