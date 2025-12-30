package com.gift.finder.data.local.dao

import androidx.room.*
import com.gift.finder.data.local.entities.SavedGiftEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for managing saved gifts (wishlist).
 */
@Dao
interface SavedGiftDao {
    @Query("SELECT * FROM saved_gifts WHERE personId = :personId ORDER BY savedAt DESC")
    fun getSavedGiftsForPerson(personId: Long): Flow<List<SavedGiftEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedGift(savedGift: SavedGiftEntity)

    @Delete
    suspend fun deleteSavedGift(savedGift: SavedGiftEntity)

    @Query("DELETE FROM saved_gifts WHERE personId = :personId AND categoryId = :categoryId")
    suspend fun deleteSavedGift(personId: Long, categoryId: String)
    
    @Query("SELECT COUNT(*) FROM saved_gifts WHERE personId = :personId AND categoryId = :categoryId")
    suspend fun isGiftSaved(personId: Long, categoryId: String): Int
}
