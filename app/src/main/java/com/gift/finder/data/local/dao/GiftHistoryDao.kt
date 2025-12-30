package com.gift.finder.data.local.dao

import androidx.room.*
import com.gift.finder.data.local.entities.GiftHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for GiftHistory entity operations.
 */
@Dao
interface GiftHistoryDao {

    @Query("SELECT * FROM gift_history WHERE personId = :personId ORDER BY purchasedAt DESC")
    fun getGiftHistoryForPerson(personId: Long): Flow<List<GiftHistoryEntity>>

    @Query("""
        SELECT * FROM gift_history 
        WHERE personId = :personId 
        AND purchasedAt > :sinceTimestamp
        ORDER BY purchasedAt DESC
    """)
    suspend fun getRecentGiftHistory(personId: Long, sinceTimestamp: Long): List<GiftHistoryEntity>

    @Query("""
        SELECT categoryId FROM gift_history 
        WHERE personId = :personId 
        AND purchasedAt > :sinceTimestamp
    """)
    suspend fun getRecentlyPurchasedCategories(personId: Long, sinceTimestamp: Long): List<String>

    @Query("SELECT SUM(price) FROM gift_history WHERE personId = :personId")
    fun getTotalSpentOnPerson(personId: Long): Flow<Double?>

    @Query("""
        SELECT SUM(price) FROM gift_history 
        WHERE purchasedAt >= :startTimestamp AND purchasedAt <= :endTimestamp
    """)
    fun getTotalSpentInPeriod(startTimestamp: Long, endTimestamp: Long): Flow<Double?>

    @Query("SELECT * FROM gift_history ORDER BY purchasedAt DESC LIMIT :limit")
    fun getRecentGifts(limit: Int): Flow<List<GiftHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGiftHistory(giftHistory: GiftHistoryEntity)

    @Delete
    suspend fun deleteGiftHistory(giftHistory: GiftHistoryEntity)

    @Query("DELETE FROM gift_history WHERE id = :id")
    suspend fun deleteGiftHistoryById(id: String)

    @Query("DELETE FROM gift_history WHERE personId = :personId")
    suspend fun deleteAllForPerson(personId: Long)
}
