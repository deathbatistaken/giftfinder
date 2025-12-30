package com.gift.finder.utils

import android.content.Context
import com.gift.finder.domain.model.Archetype
import com.gift.finder.domain.model.GiftCategory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class for parsing JSON files from assets.
 */
@Singleton
class JsonParser @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    /**
     * Parses archetypes from a JSON file in assets.
     */
    fun parseArchetypes(fileName: String): List<Archetype> {
        return try {
            val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<Archetype>>() {}.type
            gson.fromJson(jsonString, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Parses gift categories from a JSON file in assets.
     */
    fun parseGiftCategories(fileName: String): List<GiftCategory> {
        return try {
            val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<GiftCategory>>() {}.type
            gson.fromJson(jsonString, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
