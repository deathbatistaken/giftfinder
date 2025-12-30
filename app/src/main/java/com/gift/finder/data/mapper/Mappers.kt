package com.gift.finder.data.mapper

import com.gift.finder.data.local.entities.GiftHistoryEntity
import com.gift.finder.data.local.entities.PersonEntity
import com.gift.finder.data.local.entities.SpecialDateEntity
import com.gift.finder.domain.model.GiftHistoryItem
import com.gift.finder.domain.model.Person
import com.gift.finder.domain.model.SpecialDate

/**
 * Mappers between Room entities and domain models.
 */

// Person Mappers
fun PersonEntity.toDomain(
    specialDates: List<SpecialDate> = emptyList(),
    giftHistory: List<GiftHistoryItem> = emptyList()
): Person = Person(
    id = id,
    uuid = uuid,
    name = name,
    relationshipType = relationshipType,
    avatarEmoji = avatarEmoji,
    interests = interests,
    dislikes = dislikes,
    notes = notes,
    specialDates = specialDates,
    giftHistory = giftHistory,
    archetypeId = archetypeId,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Person.toEntity(): PersonEntity = PersonEntity(
    id = id,
    uuid = uuid,
    name = name,
    relationshipType = relationshipType,
    avatarEmoji = avatarEmoji,
    interests = interests,
    dislikes = dislikes,
    notes = notes,
    archetypeId = archetypeId,
    createdAt = createdAt,
    updatedAt = updatedAt
)

// SpecialDate Mappers
fun SpecialDateEntity.toDomain(): SpecialDate = SpecialDate(
    id = id,
    personId = personId,
    title = title,
    dateType = dateType,
    month = month,
    dayOfMonth = dayOfMonth,
    year = year,
    notificationOffsets = notificationOffsets,
    isNotificationEnabled = isNotificationEnabled
)

fun SpecialDate.toEntity(): SpecialDateEntity = SpecialDateEntity(
    id = id,
    personId = personId,
    title = title,
    dateType = dateType,
    month = month,
    dayOfMonth = dayOfMonth,
    year = year,
    notificationOffsets = notificationOffsets,
    isNotificationEnabled = isNotificationEnabled
)

// GiftHistory Mappers
fun GiftHistoryEntity.toDomain(): GiftHistoryItem = GiftHistoryItem(
    id = id,
    personId = personId,
    categoryId = categoryId,
    categoryTitle = categoryTitle,
    purchaseDate = purchasedAt,
    price = price,
    notes = notes,
    occasion = occasion
)

fun GiftHistoryItem.toEntity(): GiftHistoryEntity = GiftHistoryEntity(
    id = id,
    personId = personId,
    categoryId = categoryId,
    categoryTitle = categoryTitle,
    purchasedAt = purchaseDate,
    price = price,
    notes = notes,
    occasion = occasion
)
