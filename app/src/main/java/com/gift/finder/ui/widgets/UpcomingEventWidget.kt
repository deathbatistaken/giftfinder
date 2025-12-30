package com.gift.finder.ui.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.gift.finder.MainActivity
import com.gift.finder.ui.theme.DarkSlate
import com.gift.finder.ui.theme.NeonPurple
import androidx.glance.unit.ColorProvider

class UpcomingEventWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                WidgetContent()
            }
        }
    }

    @Composable
    private fun WidgetContent() {
        val eventName = currentState(stringPreferencesKey("eventName")) ?: "No Upcoming Events"
        val personName = currentState(stringPreferencesKey("personName")) ?: ""
        val daysInfo = currentState(stringPreferencesKey("daysInfo")) ?: ""
        val emoji = currentState(stringPreferencesKey("emoji")) ?: "📅"

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(DarkSlate)
                .clickable(actionStartActivity<MainActivity>())
                .padding(12.dp)
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Header
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✨ UPCOMING",
                        style = TextStyle(
                            color = ColorProvider(NeonPurple),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height(8.dp))

                // Event Content
                if (personName.isNotEmpty()) {
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = emoji,
                            style = TextStyle(fontSize = 32.sp)
                        )
                        Spacer(modifier = GlanceModifier.width(12.dp))
                        Column {
                            Text(
                                text = personName,
                                style = TextStyle(
                                    color = ColorProvider(androidx.compose.ui.graphics.Color.White),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = eventName,
                                style = TextStyle(
                                    color = ColorProvider(androidx.compose.ui.graphics.Color.LightGray),
                                    fontSize = 14.sp
                                )
                            )
                        }
                    }
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    Text(
                        text = daysInfo,
                        style = TextStyle(
                            color = ColorProvider(NeonPurple),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                } else {
                    // Empty State
                    Column(
                        modifier = GlanceModifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No upcoming events found.",
                            style = TextStyle(
                                color = ColorProvider(androidx.compose.ui.graphics.Color.Gray),
                                fontSize = 14.sp
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(4.dp))
                        Text(
                            text = "Tap to add dates",
                            style = TextStyle(
                                color = ColorProvider(androidx.compose.ui.graphics.Color.LightGray),
                                fontSize = 12.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
