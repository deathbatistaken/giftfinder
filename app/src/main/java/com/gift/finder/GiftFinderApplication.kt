package com.gift.finder

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.gift.finder.data.worker.CleanupWorker
import com.gift.finder.data.worker.ScheduleNotificationsWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * GiftFinder Application class.
 * Initializes Hilt dependency injection, notification channels, and WorkManager.
 */
@HiltAndroidApp
class GiftFinderApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        scheduleWorkers()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                getString(R.string.notification_channel_reminders),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notification_channel_reminders_desc)
                enableVibration(true)
            }

            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL,
                getString(R.string.notification_channel_general),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = getString(R.string.notification_channel_general_desc)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(generalChannel)
        }
    }

    private fun scheduleWorkers() {
        // Schedule notification worker to run daily
        val notificationWork = PeriodicWorkRequestBuilder<ScheduleNotificationsWorker>(
            1, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            ScheduleNotificationsWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            notificationWork
        )

        // Schedule cleanup worker to run weekly
        val cleanupWork = PeriodicWorkRequestBuilder<CleanupWorker>(
            7, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            CleanupWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            cleanupWork
        )
    }

    companion object {
        const val CHANNEL_REMINDERS = "reminder_channel"
        const val CHANNEL_GENERAL = "general_channel"
    }
}

