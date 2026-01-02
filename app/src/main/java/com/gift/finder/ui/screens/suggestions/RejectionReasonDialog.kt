package com.gift.finder.ui.screens.suggestions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.NotInterested
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gift.finder.R
import com.gift.finder.domain.model.RejectionReason
import com.gift.finder.ui.theme.LocalCosmicAura

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
    val aura = LocalCosmicAura.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                stringResource(R.string.why_not),
                fontWeight = FontWeight.Bold
            ) 
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = giftTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = aura.primaryColor
                )
                Spacer(modifier = Modifier.height(4.dp))

                RejectionReason.entries.forEach { reason ->
                    Surface(
                        onClick = { onReasonSelected(reason) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = CircleShape,
                                color = aura.primaryColor.copy(alpha = 0.1f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = getRejectionIcon(reason),
                                        contentDescription = null,
                                        tint = aura.primaryColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(getRejectionTitleRes(reason)),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = stringResource(getRejectionDescriptionRes(reason)),
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

private fun getRejectionIcon(reason: RejectionReason): ImageVector {
    return when (reason) {
        RejectionReason.TOO_EXPENSIVE -> Icons.Default.AttachMoney
        RejectionReason.NOT_THEIR_STYLE -> Icons.Default.Style
        RejectionReason.ALREADY_HAS -> Icons.Default.Inventory
        RejectionReason.NOT_INTERESTED -> Icons.Default.NotInterested
        RejectionReason.BOUGHT_BEFORE -> Icons.Default.Replay
        RejectionReason.OTHER -> Icons.Default.HelpOutline
    }
}

private fun getRejectionTitleRes(reason: RejectionReason): Int {
    return when (reason) {
        RejectionReason.TOO_EXPENSIVE -> R.string.rejection_expensive
        RejectionReason.NOT_THEIR_STYLE -> R.string.rejection_not_style
        RejectionReason.ALREADY_HAS -> R.string.rejection_already_have
        RejectionReason.NOT_INTERESTED -> R.string.rejection_not_interested
        RejectionReason.BOUGHT_BEFORE -> R.string.rejection_bought_before
        RejectionReason.OTHER -> R.string.rejection_other
    }
}

private fun getRejectionDescriptionRes(reason: RejectionReason): Int {
    return when (reason) {
        RejectionReason.TOO_EXPENSIVE -> R.string.rejection_expensive_desc
        RejectionReason.NOT_THEIR_STYLE -> R.string.rejection_not_style_desc
        RejectionReason.ALREADY_HAS -> R.string.rejection_already_have_desc
        RejectionReason.NOT_INTERESTED -> R.string.rejection_not_interested_desc
        RejectionReason.BOUGHT_BEFORE -> R.string.rejection_bought_before_desc
        RejectionReason.OTHER -> R.string.rejection_other_desc
    }
}
