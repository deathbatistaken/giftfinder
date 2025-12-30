package com.gift.finder.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for storing gift purchase history.
 */
@Entity(
    tableName = "gift_history",
    foreignKeys = [
        ForeignKey(
            entity = PersonEntity::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("personId"), Index("categoryId")]
)
data class GiftHistoryEntity(
    @PrimaryKey
    val id: String,
    val personId: Long,
    val categoryId: String,
    val categoryTitle: String,
    val purchasedAt: Long,
    val price: Double?,
    val notes: String,
    val occasion: String
)
