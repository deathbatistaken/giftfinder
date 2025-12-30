package com.gift.finder.data.local.dao

import androidx.room.*
import com.gift.finder.data.local.entities.PersonEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Person entity operations.
 */
@Dao
interface PersonDao {

    @Query("SELECT * FROM persons ORDER BY updatedAt DESC")
    fun getAllPersons(): Flow<List<PersonEntity>>

    @Query("SELECT * FROM persons WHERE id = :id")
    fun getPersonById(id: Long): Flow<PersonEntity?>

    @Query("SELECT * FROM persons WHERE uuid = :uuid")
    suspend fun getPersonByUuid(uuid: String): PersonEntity?

    @Query("SELECT COUNT(*) FROM persons")
    fun getPersonCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerson(person: PersonEntity): Long

    @Update
    suspend fun updatePerson(person: PersonEntity)

    @Delete
    suspend fun deletePerson(person: PersonEntity)

    @Query("DELETE FROM persons WHERE id = :id")
    suspend fun deletePersonById(id: Long)

    @Query("SELECT * FROM persons WHERE name LIKE '%' || :query || '%'")
    fun searchPersons(query: String): Flow<List<PersonEntity>>

    @Query("UPDATE persons SET updatedAt = :timestamp WHERE id = :personId")
    suspend fun updateTimestamp(personId: Long, timestamp: Long = System.currentTimeMillis())
}
