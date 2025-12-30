package com.gift.finder.data.local.dao

import androidx.room.*
import com.gift.finder.data.local.entities.RejectedGiftEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for RejectedGift entity operations (Shadow Learning).
 */
@Dao
interface RejectedGiftDao {

    @Query("SELECT * FROM rejected_gifts WHERE personId = :personId")
    fun getRejectedGiftsForPerson(personId: Long): Flow<List<RejectedGiftEntity>>

    @Query("SELECT categoryId FROM rejected_gifts WHERE personId = :personId")
    suspend fun getRejectedCategoryIds(personId: Long): List<String>

    @Query("""
        SELECT categoryId FROM rejected_gifts 
        WHERE personId = :personId 
        AND rejectedAt > :sinceTimestamp
    """)
    suspend fun getRecentlyRejectedCategories(personId: Long, sinceTimestamp: Long): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRejectedGift(rejectedGift: RejectedGiftEntity)

    @Delete
    suspend fun deleteRejectedGift(rejectedGift: RejectedGiftEntity)

    @Query("DELETE FROM rejected_gifts WHERE personId = :personId AND categoryId = :categoryId")
    suspend fun clearRejection(personId: Long, categoryId: String)

    @Query("DELETE FROM rejected_gifts WHERE personId = :personId")
    suspend fun deleteAllForPerson(personId: Long)

    @Query("DELETE FROM rejected_gifts WHERE rejectedAt < :cutoffTimestamp")
    suspend fun deleteOlderThan(cutoffTimestamp: Long)
}
