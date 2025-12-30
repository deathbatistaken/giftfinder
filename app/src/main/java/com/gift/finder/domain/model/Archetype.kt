package com.gift.finder.domain.model

/**
 * Archetype for quick person profiling.
 * Pre-defined templates with interest tags.
 */
data class Archetype(
    val id: String,
    val title: String,
    val emoji: String,
    val description: String,
    val defaultInterests: List<String>,
    val suggestedStyles: List<GiftStyle>,
    val category: ArchetypeCategory
)

/**
 * Archetype categories for organization.
 */
enum class ArchetypeCategory(val displayKey: String) {
    TECH("archetype_category_tech"),
    CREATIVE("archetype_category_creative"),
    ACTIVE("archetype_category_active"),
    HOME("archetype_category_home"),
    PROFESSIONAL("archetype_category_professional"),
    ENTERTAINMENT("archetype_category_entertainment")
}
