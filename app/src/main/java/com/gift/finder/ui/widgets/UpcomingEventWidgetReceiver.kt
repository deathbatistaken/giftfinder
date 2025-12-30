package com.gift.finder.ui.widgets

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.gift.finder.data.repository.SpecialDateRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

class UpcomingEventWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = UpcomingEventWidget()

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun specialDateRepository(): SpecialDateRepository
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: android.appwidget.AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        updateWidget(context)
    }

    private fun updateWidget(context: Context) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        )
        val repository = entryPoint.specialDateRepository()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Fetch strictly needed data
                val upcomingDates = repository.getUpcomingDates(30).first()
                val nextEvent = upcomingDates.firstOrNull()

                val manager = GlanceAppWidgetManager(context)
                val glanceIds = manager.getGlanceIds(UpcomingEventWidget::class.java)

                    glanceIds.forEach { glanceId ->
                    updateAppWidgetState(context, glanceId) { prefs ->
                        if (nextEvent != null) {
                            prefs[stringPreferencesKey("eventName")] = nextEvent.type.name
                            prefs[stringPreferencesKey("personName")] = nextEvent.personName
                            val days = nextEvent.getDaysUntil()
                            prefs[stringPreferencesKey("daysInfo")] = if (days == 0) context.getString(com.gift.finder.R.string.widget_today) else context.getString(com.gift.finder.R.string.widget_days_left, days)
                            prefs[stringPreferencesKey("emoji")] = nextEvent.personEmoji
                        } else {
                            prefs[stringPreferencesKey("eventName")] = context.getString(com.gift.finder.R.string.widget_no_events)
                            prefs[stringPreferencesKey("personName")] = ""
                            prefs[stringPreferencesKey("daysInfo")] = ""
                            prefs[stringPreferencesKey("emoji")] = "📅"
                        }
                    }
                    glanceAppWidget.update(context, glanceId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
