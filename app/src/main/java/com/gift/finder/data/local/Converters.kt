package com.gift.finder.data.local

import androidx.room.TypeConverter
import com.gift.finder.domain.model.RejectionReason
import com.gift.finder.domain.model.RelationshipType
import com.gift.finder.domain.model.SpecialDateType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Room TypeConverters for complex types.
 */
class Converters {
    private val gson = Gson()

    // List<String> Converters
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    // List<Int> Converters
    @TypeConverter
    fun fromIntList(value: List<Int>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toIntList(value: String): List<Int> {
        val type = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    // RelationshipType Converters
    @TypeConverter
    fun fromRelationshipType(value: RelationshipType): String {
        return value.name
    }

    @TypeConverter
    fun toRelationshipType(value: String): RelationshipType {
        return RelationshipType.valueOf(value)
    }

    // SpecialDateType Converters
    @TypeConverter
    fun fromSpecialDateType(value: SpecialDateType): String {
        return value.name
    }

    @TypeConverter
    fun toSpecialDateType(value: String): SpecialDateType {
        return SpecialDateType.valueOf(value)
    }

    // RejectionReason Converters
    @TypeConverter
    fun fromRejectionReason(value: RejectionReason): String {
        return value.name
    }

    @TypeConverter
    fun toRejectionReason(value: String): RejectionReason {
        return RejectionReason.valueOf(value)
    }
}
