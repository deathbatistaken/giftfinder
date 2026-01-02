package com.gift.finder.ui.screens.person

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.gift.finder.ui.components.premium.GlassCard
import com.gift.finder.ui.components.premium.LocalCosmicAura
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gift.finder.R
import com.gift.finder.domain.model.Archetype
import com.gift.finder.domain.model.RelationshipType
import com.gift.finder.ui.viewmodels.PersonFormState
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi

/**
 * Reusable form composable for Add/Edit Person screens.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PersonForm(
    modifier: Modifier = Modifier,
    formState: PersonFormState,
    archetypes: List<Archetype>,
    onNameChange: (String) -> Unit,
    onRelationshipChange: (RelationshipType) -> Unit,
    onEmojiChange: (String) -> Unit,
    onAddInterest: (String) -> Unit,
    onRemoveInterest: (String) -> Unit,
    onAddDislike: (String) -> Unit,
    onRemoveDislike: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSelectArchetype: (Archetype) -> Unit
) {
    var newInterest by remember { mutableStateOf("") }
    var newDislike by remember { mutableStateOf("") }
    var showEmojiPicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        val aura = LocalCosmicAura.current

        // Basic Info Section
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    stringResource(R.string.basic_info).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = aura.primaryColor
                )
                
                OutlinedTextField(
                    value = formState.name,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(R.string.name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    isError = formState.name.isBlank()
                )

                // Emoji picker
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(R.string.choose_emoji),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    EmojiPickerRow(
                        selectedEmoji = formState.avatarEmoji,
                        onEmojiSelected = onEmojiChange
                    )
                }
            }
        }

        // Relationship Section
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    stringResource(R.string.relationship).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = aura.primaryColor
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RelationshipType.entries.forEach { type ->
                        val stringId = remember(type) {
                            val field = R.string::class.java.getField(type.displayKey)
                            field.getInt(null)
                        }
                        FilterChip(
                            selected = formState.relationshipType == type,
                            onClick = { onRelationshipChange(type) },
                            label = { Text(stringResource(stringId)) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }

        // Quick profiles
        if (archetypes.isNotEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        stringResource(R.string.quick_profiles).uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = aura.primaryColor
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(archetypes) { archetype ->
                            ArchetypeChip(
                                archetype = archetype,
                                isSelected = formState.selectedArchetypeId == archetype.id,
                                onClick = { onSelectArchetype(archetype) }
                            )
                        }
                    }
                }
            }
        }

        // Interests Section
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    stringResource(R.string.interests).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = aura.primaryColor
                )
                
                if (formState.interests.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        formState.interests.forEach { interest ->
                            InputChip(
                                selected = false,
                                onClick = { },
                                label = { Text(interest) },
                                shape = RoundedCornerShape(12.dp),
                                trailingIcon = {
                                    IconButton(
                                        onClick = { onRemoveInterest(interest) },
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.delete))
                                    }
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newInterest,
                        onValueChange = { newInterest = it },
                        label = { Text(stringResource(R.string.add_interest)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    IconButton(
                        onClick = {
                            if (newInterest.isNotBlank()) {
                                onAddInterest(newInterest.trim())
                                newInterest = ""
                            }
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_interest), tint = aura.primaryColor)
                    }
                }
            }
        }

        // Dislikes Section
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    stringResource(R.string.dislikes).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = aura.primaryColor
                )
                
                if (formState.dislikes.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        formState.dislikes.forEach { dislike ->
                            InputChip(
                                selected = false,
                                onClick = { },
                                label = { Text(dislike) },
                                shape = RoundedCornerShape(12.dp),
                                trailingIcon = {
                                    IconButton(
                                        onClick = { onRemoveDislike(dislike) },
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.delete))
                                    }
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newDislike,
                        onValueChange = { newDislike = it },
                        label = { Text(stringResource(R.string.add_dislike)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    IconButton(
                        onClick = {
                            if (newDislike.isNotBlank()) {
                                onAddDislike(newDislike.trim())
                                newDislike = ""
                            }
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_dislike), tint = aura.primaryColor)
                    }
                }
            }
        }

        // Notes Section
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    stringResource(R.string.notes).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = aura.primaryColor
                )
                OutlinedTextField(
                    value = formState.notes,
                    onValueChange = onNotesChange,
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }
}

@Composable
private fun EmojiPickerRow(
    selectedEmoji: String,
    onEmojiSelected: (String) -> Unit
) {
    val emojis = listOf("👤", "👩", "👨", "👧", "👦", "👶", "🧑", "👵", "👴", "🐱", "🐶", "🎁", "❤️", "⭐")

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(emojis) { emoji ->
            FilterChip(
                selected = selectedEmoji == emoji,
                onClick = { onEmojiSelected(emoji) },
                label = { Text(emoji, style = MaterialTheme.typography.titleLarge) }
            )
        }
    }
}

@Composable
private fun ArchetypeChip(
    archetype: Archetype,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(archetype.emoji, style = MaterialTheme.typography.titleMedium)
                Text(archetype.title)
            }
        }
    )
}
