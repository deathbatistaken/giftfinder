package com.gift.finder.domain.manager

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class CalendarManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Launches the system calendar app to insert an event.
     */
    fun addEventToCalendar(title: String, description: String, dateMillis: Long) {
        try {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, title)
                putExtra(CalendarContract.Events.DESCRIPTION, description)
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, dateMillis)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, dateMillis + 3600000) // 1 hour duration
                putExtra(CalendarContract.Events.ALL_DAY, true)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
