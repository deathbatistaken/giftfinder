package com.gift.finder.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.gift.finder.data.manager.LocalNotificationManager
import com.gift.finder.data.repository.PersonRepository
import com.gift.finder.data.repository.SpecialDateRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Broadcast receiver for device boot to reschedule notifications.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var specialDateRepository: SpecialDateRepository

    @Inject
    lateinit var personRepository: PersonRepository

    @Inject
    lateinit var notificationManager: LocalNotificationManager

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        scope.launch {
            rescheduleAllNotifications()
        }
    }

    private suspend fun rescheduleAllNotifications() {
        val persons = personRepository.getAllPersons().first()
        val personsMap = persons.associateBy { it.id }

        val specialDates = specialDateRepository.getSpecialDatesWithNotifications().first()
        
        specialDates.forEach { specialDate ->
            val personName = personsMap[specialDate.personId]?.name ?: return@forEach
            notificationManager.scheduleNotificationsForDate(specialDate, personName)
        }
    }
}
