package com.gift.finder.data.repository

import com.gift.finder.data.local.dao.SavedGiftDao
import com.gift.finder.data.local.entities.SavedGiftEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing wishlist items.
 */
@Singleton
class SavedGiftRepository @Inject constructor(
    private val savedGiftDao: SavedGiftDao
) {
    fun getWishlistForPerson(personId: Long): Flow<List<SavedGiftEntity>> {
        return savedGiftDao.getSavedGiftsForPerson(personId)
    }

    suspend fun saveGift(personId: Long, categoryId: String) {
        if (savedGiftDao.isGiftSaved(personId, categoryId) == 0) {
            savedGiftDao.insertSavedGift(
                SavedGiftEntity(
                    personId = personId,
                    categoryId = categoryId
                )
            )
        }
    }

    suspend fun removeGift(personId: Long, categoryId: String) {
        savedGiftDao.deleteSavedGift(personId, categoryId)
    }

    suspend fun isGiftSaved(personId: Long, categoryId: String): Boolean {
        return savedGiftDao.isGiftSaved(personId, categoryId) > 0
    }
}
