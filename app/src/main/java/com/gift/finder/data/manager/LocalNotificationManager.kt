package com.gift.finder.data.manager

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.gift.finder.GiftFinderApplication
import com.gift.finder.R
import com.gift.finder.data.receiver.NotificationReceiver
import com.gift.finder.domain.model.SpecialDate
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for local notifications and alarm scheduling.
 */
@Singleton
class LocalNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    /**
     * Schedule notifications for a special date with custom offsets.
     */
    fun scheduleNotificationsForDate(
        specialDate: SpecialDate,
        personName: String,
        offsets: List<Int> = specialDate.notificationOffsets
    ) {
        offsets.forEach { daysBefore ->
            scheduleNotification(
                specialDate = specialDate,
                personName = personName,
                daysBefore = daysBefore
            )
        }
    }

    /**
     * Cancel all notifications for a special date.
     */
    fun cancelNotificationsForDate(specialDateId: String) {
        // Cancel all possible standard offsets to be safe
        listOf(0, 1, 3, 7, 14).forEach { daysBefore ->
            val requestCode = "${specialDateId}_$daysBefore".hashCode()
            val intent = Intent(context, NotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent?.let {
                alarmManager.cancel(it)
                it.cancel()
            }
        }
    }

    /**
     * Show immediate notification (for testing or instant alerts).
     */
    fun showNotification(title: String, message: String, notificationId: Int = System.currentTimeMillis().toInt()) {
        val notification = NotificationCompat.Builder(context, GiftFinderApplication.CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun scheduleNotification(
        specialDate: SpecialDate,
        personName: String,
        daysBefore: Int
    ) {
        val notificationTime = calculateNotificationTime(specialDate, daysBefore)
        
        // Don't schedule if time is in the past
        if (notificationTime.timeInMillis <= System.currentTimeMillis()) return

        val requestCode = "${specialDate.id}_$daysBefore".hashCode()
        
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(NotificationReceiver.EXTRA_TITLE, buildNotificationTitle(specialDate, daysBefore))
            putExtra(NotificationReceiver.EXTRA_MESSAGE, buildNotificationMessage(specialDate, personName, daysBefore))
            putExtra(NotificationReceiver.EXTRA_NOTIFICATION_ID, requestCode)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    notificationTime.timeInMillis,
                    pendingIntent
                )
            } else {
                // Fallback to inexact alarm
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    notificationTime.timeInMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                notificationTime.timeInMillis,
                pendingIntent
            )
        }
    }

    private fun calculateNotificationTime(specialDate: SpecialDate, daysBefore: Int): Calendar {
        val nextOccurrence = specialDate.getNextOccurrence()
        return nextOccurrence.apply {
            add(Calendar.DAY_OF_YEAR, -daysBefore)
            set(Calendar.HOUR_OF_DAY, 9) // Send at 9 AM
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
    }

    private fun buildNotificationTitle(specialDate: SpecialDate, daysBefore: Int): String {
        return when (daysBefore) {
            0 -> context.getString(R.string.notification_title_today)
            1 -> context.getString(R.string.notification_title_tomorrow)
            else -> context.getString(R.string.notification_title_days, daysBefore)
        }
    }

    private fun buildNotificationMessage(
        specialDate: SpecialDate,
        personName: String,
        daysBefore: Int
    ): String {
        return when (daysBefore) {
            0 -> context.getString(R.string.notification_message_today, personName, specialDate.title)
            else -> context.getString(R.string.notification_message_upcoming, personName, specialDate.title)
        }
    }
}
