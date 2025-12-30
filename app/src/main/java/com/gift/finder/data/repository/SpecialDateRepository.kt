package com.gift.finder.data.repository

import com.gift.finder.data.local.dao.SpecialDateDao
import com.gift.finder.data.mapper.toDomain
import com.gift.finder.domain.model.SpecialDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for special date operations.
 */
@Singleton
class SpecialDateRepository @Inject constructor(
    private val specialDateDao: SpecialDateDao
) {
    /**
     * Get all special dates with notifications enabled.
     */
    fun getSpecialDatesWithNotifications(): Flow<List<SpecialDate>> {
        return specialDateDao.getSpecialDatesWithNotifications().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Get all special dates.
     */
    fun getAllSpecialDates(): Flow<List<SpecialDate>> {
        return specialDateDao.getAllSpecialDates().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Get special dates on a specific date (for notification scheduling).
     */
    suspend fun getSpecialDatesOnDate(month: Int, day: Int): List<SpecialDate> {
        return specialDateDao.getSpecialDatesOnDate(month, day).map { it.toDomain() }
    }

    /**
     * Get upcoming special dates (within next N days).
     */
    fun getUpcomingDates(withinDays: Int): Flow<List<SpecialDate>> {
        return getAllSpecialDates().map { dates ->
            dates.filter { it.getDaysUntil() in 0..withinDays }
                .sortedBy { it.getDaysUntil() }
        }
    }
}
