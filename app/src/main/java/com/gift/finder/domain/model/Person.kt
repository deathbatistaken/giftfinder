package com.gift.finder.domain.model

import java.util.UUID

/**
 * Domain model representing a person the user wants to buy gifts for.
 */
data class Person(
    val id: Long = 0,
    val uuid: String = UUID.randomUUID().toString(),
    val name: String,
    val relationshipType: RelationshipType,
    val avatarEmoji: String = "🎁",
    val interests: List<String> = emptyList(),
    val dislikes: List<String> = emptyList(),
    val notes: String = "",
    val specialDates: List<SpecialDate> = emptyList(),
    val giftHistory: List<GiftHistoryItem> = emptyList(),
    val archetypeId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Special date associated with a person.
 */
data class SpecialDate(
    val id: String = UUID.randomUUID().toString(),
    val personId: Long,
    val title: String,
    val dateType: SpecialDateType,
    val month: Int,
    val dayOfMonth: Int,
    val year: Int? = null, // null means recurring yearly
    val notificationOffsets: List<Int> = listOf(7, 3, 0), // days before
    val isNotificationEnabled: Boolean = true
) {
    /**
     * Get the next occurrence of this date.
     */
    fun getNextOccurrence(): java.util.Calendar {
        val now = java.util.Calendar.getInstance()
        val thisYear = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.MONTH, month - 1)
            set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth)
            set(java.util.Calendar.HOUR_OF_DAY, 9)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
        }
        
        return if (thisYear.after(now)) {
            thisYear
        } else {
            thisYear.apply { add(java.util.Calendar.YEAR, 1) }
        }
    }
    
    /**
     * Get days until this date.
     */
    fun getDaysUntil(): Int {
        val nextOccurrence = getNextOccurrence()
        val now = java.util.Calendar.getInstance()
        val diff = nextOccurrence.timeInMillis - now.timeInMillis
        return (diff / (1000 * 60 * 60 * 24)).toInt()
    }
}

/**
 * History item for gifts that were marked as purchased.
 */
data class GiftHistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val personId: Long,
    val categoryId: String,
    val categoryTitle: String,
    val purchaseDate: Long = System.currentTimeMillis(),
    val price: Double? = null,
    val notes: String = "",
    val occasion: String = ""
)
