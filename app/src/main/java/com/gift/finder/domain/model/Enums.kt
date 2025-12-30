package com.gift.finder.domain.model

/**
 * Relationship type between user and gift recipient.
 */
enum class RelationshipType(val displayKey: String) {
    PARTNER("relationship_partner"),
    SPOUSE("relationship_spouse"),
    FAMILY("relationship_family"),
    FRIEND("relationship_friend"),
    COLLEAGUE("relationship_colleague"),
    BOSS("relationship_boss"),
    CHILD("relationship_child"),
    PARENT("relationship_parent"),
    SIBLING("relationship_sibling"),
    OTHER("relationship_other")
}

/**
 * Budget range for gift suggestions.
 */
/**
 * Budget range for gift suggestions.
 */
enum class BudgetRange(val minUsd: Int, val maxUsd: Int, val displayKey: String) {
    BUDGET(0, 25, "budget_low_tier"),
    LOW(25, 50, "budget_low"),
    MEDIUM(50, 100, "budget_medium"),
    HIGH(100, 250, "budget_high"),
    LUXURY(250, Int.MAX_VALUE, "budget_luxury");

    constructor(displayKey: String, min: Int, max: Int) : this(min, max, displayKey)
}

/**
 * Gift style preferences.
 */
enum class GiftStyle(val displayKey: String) {
    PRACTICAL("style_practical"),
    ROMANTIC("style_romantic"),
    FUN("style_fun"),
    CREATIVE("style_creative"),
    LUXURIOUS("style_luxurious"),
    SENTIMENTAL("style_sentimental"),
    EXPERIENTIAL("style_experiential"),
    TECH("style_tech")
}

/**
 * Seasons for gift filtering.
 */
enum class Season(val months: List<Int>) {
    SPRING(listOf(3, 4, 5)),
    SUMMER(listOf(6, 7, 8)),
    FALL(listOf(9, 10, 11)),
    WINTER(listOf(12, 1, 2)),
    ALL(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));

    companion object {
        fun current(): Season {
            val month = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1
            return entries.find { it != ALL && month in it.months } ?: ALL
        }
    }
}

/**
 * Special date types.
 */
enum class SpecialDateType(val displayKey: String) {
    BIRTHDAY("birthday"),
    ANNIVERSARY("anniversary"),
    WEDDING("event_wedding"),
    VALENTINE("event_valentine"),
    MOTHERS_DAY("event_mothers_day"),
    FATHERS_DAY("event_fathers_day"),
    CHRISTMAS("event_christmas"),
    NEW_YEAR("event_new_year"),
    GRADUATION("event_graduation"),
    OTHER("other")
}

/**
 * Rejection reason for shadow learning.
 */
enum class RejectionReason(val displayKey: String) {
    TOO_EXPENSIVE("rejection_expensive"),
    NOT_THEIR_STYLE("rejection_not_style"),
    ALREADY_HAS("rejection_already_have"),
    BOUGHT_BEFORE("rejection_bought_before"),
    NOT_INTERESTED("rejection_not_interested"),
    OTHER("rejection_other")
}

/**
 * Store types for smart deep links.
 */
enum class StoreType(val baseUrl: String) {
    AMAZON("https://www.amazon.com/s?k="),
    ETSY("https://www.etsy.com/search?q="),
    STEAM("https://store.steampowered.com/search/?term="),
    GOODREADS("https://www.goodreads.com/search?q="),
    GOOGLE("https://www.google.com/search?q="),
    CUSTOM("")
}
