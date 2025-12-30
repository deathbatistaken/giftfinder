package com.gift.finder.data.local.dao

import androidx.room.*
import com.gift.finder.data.local.entities.SpecialDateEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for SpecialDate entity operations.
 */
@Dao
interface SpecialDateDao {

    @Query("SELECT * FROM special_dates WHERE personId = :personId ORDER BY month, dayOfMonth")
    fun getSpecialDatesForPerson(personId: Long): Flow<List<SpecialDateEntity>>

    @Query("SELECT * FROM special_dates WHERE id = :id")
    suspend fun getSpecialDateById(id: String): SpecialDateEntity?

    @Query("SELECT * FROM special_dates WHERE isNotificationEnabled = 1")
    fun getSpecialDatesWithNotifications(): Flow<List<SpecialDateEntity>>

    @Query("""
        SELECT sd.* FROM special_dates sd
        INNER JOIN persons p ON sd.personId = p.id
        ORDER BY sd.month, sd.dayOfMonth
    """)
    fun getAllSpecialDates(): Flow<List<SpecialDateEntity>>

    @Query("""
        SELECT sd.* FROM special_dates sd
        INNER JOIN persons p ON sd.personId = p.id
        WHERE sd.month = :month AND sd.dayOfMonth = :day
    """)
    suspend fun getSpecialDatesOnDate(month: Int, day: Int): List<SpecialDateEntity>

    @Query("SELECT COUNT(*) FROM special_dates WHERE personId = :personId")
    fun getSpecialDateCountForPerson(personId: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpecialDate(specialDate: SpecialDateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpecialDates(specialDates: List<SpecialDateEntity>)

    @Update
    suspend fun updateSpecialDate(specialDate: SpecialDateEntity)

    @Delete
    suspend fun deleteSpecialDate(specialDate: SpecialDateEntity)

    @Query("DELETE FROM special_dates WHERE id = :id")
    suspend fun deleteSpecialDateById(id: String)

    @Query("DELETE FROM special_dates WHERE personId = :personId")
    suspend fun deleteAllForPerson(personId: Long)

    @Query("SELECT * FROM special_dates WHERE isNotificationEnabled = 1")
    fun getWithNotificationsEnabled(): Flow<List<SpecialDateEntity>>
}
