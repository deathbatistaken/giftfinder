package com.gift.finder.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gift.finder.domain.model.RelationshipType

/**
 * Room entity for storing person data.
 */
@Entity(tableName = "persons")
data class PersonEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uuid: String,
    val name: String,
    val relationshipType: RelationshipType,
    val avatarEmoji: String,
    val interests: List<String>,
    val dislikes: List<String>,
    val notes: String,
    val archetypeId: String?,
    val createdAt: Long,
    val updatedAt: Long
)
