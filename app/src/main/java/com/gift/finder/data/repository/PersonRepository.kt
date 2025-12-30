package com.gift.finder.data.repository

import com.gift.finder.data.local.dao.GiftHistoryDao
import com.gift.finder.data.local.dao.PersonDao
import com.gift.finder.data.local.dao.RejectedGiftDao
import com.gift.finder.data.local.dao.SpecialDateDao
import com.gift.finder.data.mapper.toDomain
import com.gift.finder.data.mapper.toEntity
import com.gift.finder.domain.model.GiftHistoryItem
import com.gift.finder.domain.model.Person
import com.gift.finder.domain.model.SpecialDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Person CRUD operations.
 */
@Singleton
class PersonRepository @Inject constructor(
    private val personDao: PersonDao,
    private val specialDateDao: SpecialDateDao,
    private val giftHistoryDao: GiftHistoryDao,
    private val rejectedGiftDao: RejectedGiftDao
) {
    /**
     * Get all persons with their related data.
     */
    fun getAllPersons(): Flow<List<Person>> {
        return personDao.getAllPersons().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Get a person by ID with all related data.
     */
    fun getPersonById(id: Long): Flow<Person?> {
        return personDao.getPersonById(id).flatMapLatest { entity ->
            if (entity == null) {
                flowOf(null)
            } else {
                combine(
                    specialDateDao.getSpecialDatesForPerson(id),
                    giftHistoryDao.getGiftHistoryForPerson(id)
                ) { dates, history ->
                    entity.toDomain(
                        specialDates = dates.map { it.toDomain() },
                        giftHistory = history.map { it.toDomain() }
                    )
                }
            }
        }
    }

    /**
     * Get person count for limiting free tier.
     */
    fun getPersonCount(): Flow<Int> = personDao.getPersonCount()

    /**
     * Insert a new person.
     */
    suspend fun insertPerson(person: Person): Long {
        val id = personDao.insertPerson(person.toEntity())
        
        // Insert special dates if present
        if (person.specialDates.isNotEmpty()) {
            val datesWithPersonId = person.specialDates.map { 
                it.copy(personId = id).toEntity() 
            }
            specialDateDao.insertSpecialDates(datesWithPersonId)
        }
        
        return id
    }

    /**
     * Update an existing person.
     */
    suspend fun updatePerson(person: Person) {
        personDao.updatePerson(person.copy(updatedAt = System.currentTimeMillis()).toEntity())
    }

    /**
     * Delete a person and all related data (cascades).
     */
    suspend fun deletePerson(personId: Long) {
        personDao.deletePersonById(personId)
    }

    /**
     * Search persons by name.
     */
    fun searchPersons(query: String): Flow<List<Person>> {
        return personDao.searchPersons(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Add a special date for a person.
     */
    suspend fun addSpecialDate(specialDate: SpecialDate) {
        specialDateDao.insertSpecialDate(specialDate.toEntity())
        personDao.updateTimestamp(specialDate.personId)
    }

    /**
     * Update a special date.
     */
    suspend fun updateSpecialDate(specialDate: SpecialDate) {
        specialDateDao.updateSpecialDate(specialDate.toEntity())
        personDao.updateTimestamp(specialDate.personId)
    }

    /**
     * Delete a special date.
     */
    suspend fun deleteSpecialDate(id: String, personId: Long) {
        specialDateDao.deleteSpecialDateById(id)
        personDao.updateTimestamp(personId)
    }

    /**
     * Add a gift to history.
     */
    suspend fun addGiftToHistory(giftHistory: GiftHistoryItem) {
        giftHistoryDao.insertGiftHistory(giftHistory.toEntity())
        personDao.updateTimestamp(giftHistory.personId)
    }

    /**
     * Get count of special dates for free tier limiting.
     */
    fun getSpecialDateCount(personId: Long): Flow<Int> {
        return specialDateDao.getSpecialDateCountForPerson(personId)
    }
}
