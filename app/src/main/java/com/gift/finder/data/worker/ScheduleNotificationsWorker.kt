package com.gift.finder.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gift.finder.data.local.dao.SpecialDateDao
import com.gift.finder.data.local.dao.PersonDao
import com.gift.finder.data.manager.LocalNotificationManager
import com.gift.finder.data.mapper.toDomain
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * WorkManager worker for scheduling notifications for all special dates.
 */
@HiltWorker
class ScheduleNotificationsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val specialDateDao: SpecialDateDao,
    private val personDao: PersonDao,
    private val notificationManager: LocalNotificationManager
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Get all special dates with notifications enabled
            val specialDates = specialDateDao.getWithNotificationsEnabled().first()
            
            for (dateEntity in specialDates) {
                // Get person name for notification
                val person = personDao.getById(dateEntity.personId).first()
                if (person != null) {
                    val specialDate = dateEntity.toDomain()
                    notificationManager.scheduleNotificationsForDate(specialDate, person.name)
                }
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "schedule_notifications_work"
    }
}
