package com.gift.finder.ui.screens.suggestions

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gift.finder.R
import com.gift.finder.domain.model.RejectionReason

/**
 * Dialog for selecting a reason when rejecting a gift suggestion.
 * Used for Shadow Learning feature.
 */
@Composable
fun RejectionReasonDialog(
    giftTitle: String,
    onDismiss: () -> Unit,
    onReasonSelected: (RejectionReason) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.why_not)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = giftTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                RejectionReason.entries.forEach { reason ->
                    Surface(
                        onClick = { onReasonSelected(reason) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = getRejectionEmoji(reason),
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = getRejectionTitle(reason),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = getRejectionDescription(reason),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

private fun getRejectionEmoji(reason: RejectionReason): String {
    return when (reason) {
        RejectionReason.TOO_EXPENSIVE -> "💸"
        RejectionReason.NOT_THEIR_STYLE -> "👔"
        RejectionReason.ALREADY_HAS -> "📦"
        RejectionReason.NOT_INTERESTED -> "🙅"
        RejectionReason.BOUGHT_BEFORE -> "🔄"
        RejectionReason.OTHER -> "❓"
    }
}

private fun getRejectionTitle(reason: RejectionReason): String {
    return when (reason) {
        RejectionReason.TOO_EXPENSIVE -> "Too Expensive"
        RejectionReason.NOT_THEIR_STYLE -> "Not Their Style"
        RejectionReason.ALREADY_HAS -> "Already Has It"
        RejectionReason.NOT_INTERESTED -> "Not Interested"
        RejectionReason.BOUGHT_BEFORE -> "Bought Before"
        RejectionReason.OTHER -> "Other Reason"
    }
}

private fun getRejectionDescription(reason: RejectionReason): String {
    return when (reason) {
        RejectionReason.TOO_EXPENSIVE -> "Out of my budget range"
        RejectionReason.NOT_THEIR_STYLE -> "Doesn't fit their taste"
        RejectionReason.ALREADY_HAS -> "They already own this"
        RejectionReason.NOT_INTERESTED -> "Not interested in this"
        RejectionReason.BOUGHT_BEFORE -> "I gave this previously"
        RejectionReason.OTHER -> "Something else"
    }
}
