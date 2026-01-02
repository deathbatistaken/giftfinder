package com.gift.finder.ui.screens.suggestions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.gift.finder.R
import com.gift.finder.domain.model.GiftSuggestion

/**
 * Dialog for marking a gift as purchased with optional price.
 */
@Composable
fun MarkAsPurchasedDialog(
    suggestion: GiftSuggestion,
    occasion: String = "",
    appCurrency: String = "USD",
    onDismiss: () -> Unit,
    onConfirm: (price: Double?, occasion: String) -> Unit
) {
    var priceText by remember { mutableStateOf("") }
    var occasionText by remember { mutableStateOf(occasion) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.mark_as_purchased)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Gift info
                Text(
                    text = "${suggestion.category.emoji} ${suggestion.category.title}",
                    style = MaterialTheme.typography.titleMedium
                )

                // Price input
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { newValue ->
                        // Only allow numbers and decimal point
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                            priceText = newValue
                        }
                    },
                    label = { Text(stringResource(R.string.price_optional)) },
                    prefix = { Text(com.gift.finder.util.CurrencyUtils.getSymbol(appCurrency)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                // Occasion input
                OutlinedTextField(
                    value = occasionText,
                    onValueChange = { occasionText = it },
                    label = { Text(stringResource(R.string.occasion_optional)) },
                    placeholder = { Text(stringResource(R.string.occasion_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val price = priceText.toDoubleOrNull()
                    onConfirm(price, occasionText)
                }
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
