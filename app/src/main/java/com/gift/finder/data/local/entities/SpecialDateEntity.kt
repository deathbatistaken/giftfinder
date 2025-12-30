package com.gift.finder.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gift.finder.domain.model.SpecialDateType

/**
 * Room entity for storing special dates.
 */
@Entity(
    tableName = "special_dates",
    foreignKeys = [
        ForeignKey(
            entity = PersonEntity::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("personId")]
)
data class SpecialDateEntity(
    @PrimaryKey
    val id: String,
    val personId: Long,
    val title: String,
    val dateType: SpecialDateType,
    val month: Int,
    val dayOfMonth: Int,
    val year: Int?,
    val notificationOffsets: List<Int>,
    val isNotificationEnabled: Boolean
)
