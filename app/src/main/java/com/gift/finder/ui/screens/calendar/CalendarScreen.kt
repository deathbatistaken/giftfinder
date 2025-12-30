package com.gift.finder.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.gift.finder.R
import com.gift.finder.domain.model.SpecialDate
import com.gift.finder.ui.theme.*
import com.gift.finder.ui.viewmodels.HomeViewModel
import com.gift.finder.ui.components.premium.AnimatedMeshBackground
import com.gift.finder.ui.components.premium.GlassCard
import java.text.SimpleDateFormat
import java.util.*

/**
 * Calendar screen showing all upcoming special dates.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToPersonDetail: (Long) -> Unit
) {
    val upcomingDates by viewModel.upcomingDates.collectAsState()
    val persons by viewModel.persons.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.calendar),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedMeshBackground()
            
            if (upcomingDates.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📅", style = MaterialTheme.typography.displayMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            stringResource(R.string.no_upcoming_dates),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            stringResource(R.string.add_special_dates_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Group by month
                    val groupedDates = upcomingDates.groupBy { date ->
                        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(
                            Calendar.getInstance().apply {
                                set(Calendar.MONTH, date.month - 1)
                                set(Calendar.DAY_OF_MONTH, date.dayOfMonth)
                            }.time
                        )
                    }

                    groupedDates.forEach { (monthYear, dates) ->
                        item {
                            Text(
                                text = monthYear,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        items(dates) { specialDate ->
                            val person = persons.find { it.id == specialDate.personId }
                            val daysUntil = specialDate.getDaysUntil()

                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { person?.let { onNavigateToPersonDetail(it.id) } }
                            ) {
                                CalendarDateCard(
                                    specialDate = specialDate,
                                    personName = person?.name ?: "",
                                    personEmoji = person?.avatarEmoji ?: "👤",
                                    daysUntil = daysUntil
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDateCard(
    specialDate: SpecialDate,
    personName: String,
    personEmoji: String,
    daysUntil: Int
) {
    val aura = LocalCosmicAura.current
    val urgencyColor = when {
        daysUntil <= 0 -> GiftRed
        daysUntil <= 3 -> GiftOrange
        else -> aura.primaryColor
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
            // Date circle with Halo if urgent
            Box(contentAlignment = Alignment.Center) {
                if (daysUntil <= 3) {
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = CircleShape,
                        color = urgencyColor.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, urgencyColor.copy(alpha = 0.3f))
                    ) {}
                }
                
                Column(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(urgencyColor.copy(alpha = 0.1f)),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = specialDate.dayOfMonth.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = urgencyColor
                    )
                    Text(
                        text = SimpleDateFormat("MMM", Locale.getDefault()).format(
                            Calendar.getInstance().apply { set(Calendar.MONTH, specialDate.month - 1) }.time
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = urgencyColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(personEmoji, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = personName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = specialDate.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Countdown chip
            Surface(
                color = urgencyColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, urgencyColor.copy(alpha = 0.4f))
            ) {
                Text(
                    text = when (daysUntil) {
                        0 -> stringResource(R.string.today)
                        1 -> stringResource(R.string.tomorrow)
                        else -> stringResource(R.string.in_days, daysUntil)
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = urgencyColor,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
    }
}
