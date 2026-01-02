package com.gift.finder.ui.widgets

import android.content.Context
import android.content.Intent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.gift.finder.MainActivity
import com.gift.finder.ui.theme.DarkSlate
import com.gift.finder.ui.theme.NeonPurple

class QuickActionWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                val context = androidx.glance.LocalContext.current
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(DarkSlate)
                        .padding(12.dp)
                ) {
                    Column(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = context.getString(com.gift.finder.R.string.widget_actions_title),
                            style = TextStyle(
                                color = ColorProvider(NeonPurple),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(8.dp))
                        Row(modifier = GlanceModifier.fillMaxWidth()) {
                            // Add Person Button
                            Box(
                                modifier = GlanceModifier
                                    .defaultWeight()
                                    .height(48.dp)
                                    .background(androidx.compose.ui.graphics.Color(0xFF2D2D2D))
                                    .clickable(
                                        actionStartActivity(
                                            Intent(context, MainActivity::class.java).apply {
                                                action = "com.gift.finder.ACTION_ADD_PERSON"
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            }
                                        )
                                    )
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("➕", style = TextStyle(fontSize = 16.sp))
                                    Spacer(modifier = GlanceModifier.width(8.dp))
                                    Text(
                                        context.getString(com.gift.finder.R.string.widget_action_add),
                                        style = TextStyle(
                                            color = ColorProvider(androidx.compose.ui.graphics.Color.White),
                                            fontSize = 14.sp
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = GlanceModifier.width(8.dp))
                            // Roulette Button
                            Box(
                                modifier = GlanceModifier
                                    .defaultWeight()
                                    .height(48.dp)
                                    .background(androidx.compose.ui.graphics.Color(0xFF2D2D2D))
                                    .clickable(actionStartActivity<MainActivity>())
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🎲", style = TextStyle(fontSize = 16.sp))
                                    Spacer(modifier = GlanceModifier.width(8.dp))
                                    Text(
                                        context.getString(com.gift.finder.R.string.widget_action_play),
                                        style = TextStyle(
                                            color = ColorProvider(androidx.compose.ui.graphics.Color.White),
                                            fontSize = 14.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
