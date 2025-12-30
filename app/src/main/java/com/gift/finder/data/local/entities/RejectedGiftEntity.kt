package com.gift.finder.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gift.finder.domain.model.RejectionReason

/**
 * Room entity for storing rejected gift suggestions (Shadow Learning).
 */
@Entity(
    tableName = "rejected_gifts",
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
data class RejectedGiftEntity(
    @PrimaryKey
    val id: String,
    val personId: Long,
    val categoryId: String,
    val reason: RejectionReason,
    val rejectedAt: Long
)
