package com.gift.finder.ui.screens.person

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gift.finder.R
import com.gift.finder.domain.model.SpecialDateType
import java.util.Calendar
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow

/**
 * Dialog for adding a new special date.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddSpecialDateDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, dateType: SpecialDateType, month: Int, dayOfMonth: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedDateType by remember { mutableStateOf(SpecialDateType.BIRTHDAY) }
    var selectedMonth by remember { mutableStateOf(1) }
    var selectedDay by remember { mutableStateOf(1) }
    var showDatePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_special_date)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.event_title)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Date type selection
                Text(
                    text = stringResource(R.string.event_type),
                    style = MaterialTheme.typography.labelMedium
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SpecialDateType.entries.forEach { type ->
                        val stringId = remember(type) {
                            val field = R.string::class.java.getField(type.displayKey)
                            field.getInt(null)
                        }
                        FilterChip(
                            selected = selectedDateType == type,
                            onClick = { selectedDateType = type },
                            label = { Text(stringResource(stringId)) }
                        )
                    }
                }

                // Date picker button
                OutlinedButton(
                    onClick = {
                        val calendar = Calendar.getInstance()
                        DatePickerDialog(
                            context,
                            { _, _, month, day ->
                                selectedMonth = month + 1
                                selectedDay = day
                            },
                            calendar.get(Calendar.YEAR),
                            selectedMonth - 1,
                            selectedDay
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${stringResource(R.string.date)}: $selectedMonth/$selectedDay"
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title, selectedDateType, selectedMonth, selectedDay)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
