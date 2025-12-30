package com.gift.finder.ui.screens.person

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gift.finder.R
import com.gift.finder.domain.model.SpecialDate
import com.gift.finder.ui.theme.GiftBlue
import com.gift.finder.ui.theme.GiftGreen
import com.gift.finder.ui.theme.GiftOrange
import com.gift.finder.ui.theme.GiftRed

/**
 * Screen for managing special dates for a person.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecialDatesScreen(
    personName: String,
    specialDates: List<SpecialDate>,
    onNavigateBack: () -> Unit,
    onAddDate: () -> Unit,
    onDeleteDate: (SpecialDate) -> Unit,
    onToggleNotification: (SpecialDate, Boolean) -> Unit
) {
    var dateToDelete by remember { mutableStateOf<SpecialDate?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$personName - ${stringResource(R.string.special_dates)}") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddDate) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_special_date))
            }
        }
    ) { padding ->
        if (specialDates.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📅", style = MaterialTheme.typography.displayMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.no_special_dates),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onAddDate) {
                        Text(stringResource(R.string.add_special_date))
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(specialDates) { date ->
                    SpecialDateCard(
                        specialDate = date,
                        onDelete = { dateToDelete = date },
                        onToggleNotification = { enabled ->
                            onToggleNotification(date, enabled)
                        }
                    )
                }
            }
        }
    }

    dateToDelete?.let { date ->
        AlertDialog(
            onDismissRequest = { dateToDelete = null },
            title = { Text(stringResource(R.string.delete_date_title)) },
            text = { Text(stringResource(R.string.delete_date_message, date.title)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteDate(date)
                        dateToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { dateToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun SpecialDateCard(
    specialDate: SpecialDate,
    onDelete: () -> Unit,
    onToggleNotification: (Boolean) -> Unit
) {
    val daysUntil = specialDate.getDaysUntil()
    val urgencyColor = when {
        daysUntil <= 0 -> GiftRed
        daysUntil <= 3 -> GiftOrange
        daysUntil <= 7 -> GiftBlue
        else -> GiftGreen
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = specialDate.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${specialDate.month}/${specialDate.dayOfMonth}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = urgencyColor.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = when (daysUntil) {
                            0 -> "Today!"
                            1 -> "Tomorrow"
                            else -> "$daysUntil days"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = urgencyColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.notifications),
                    style = MaterialTheme.typography.bodyMedium
                )
                Switch(
                    checked = specialDate.isNotificationEnabled,
                    onCheckedChange = onToggleNotification
                )
            }
        }
    }
}
