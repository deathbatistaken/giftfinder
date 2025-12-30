package com.gift.finder.ui.screens.person

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gift.finder.R
import com.gift.finder.domain.model.Archetype
import com.gift.finder.domain.model.RelationshipType
import com.gift.finder.ui.viewmodels.PersonFormState
import com.gift.finder.ui.viewmodels.PersonUiState
import com.gift.finder.ui.viewmodels.PersonViewModel
import com.gift.finder.ui.theme.*
import com.gift.finder.ui.components.premium.AnimatedMeshBackground
import kotlinx.coroutines.launch

/**
 * Screen for adding a new person.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPersonScreen(
    viewModel: PersonViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onPersonCreated: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.add_person),
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                val id = viewModel.savePerson()
                                onPersonCreated(id)
                            }
                        },
                        enabled = formState.isValid
                    ) {
                        Text(stringResource(R.string.save), fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedMeshBackground()
            
            PersonForm(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                formState = formState,
                archetypes = viewModel.archetypes,
                onNameChange = viewModel::updateName,
                onRelationshipChange = viewModel::updateRelationshipType,
                onEmojiChange = viewModel::updateAvatarEmoji,
                onAddInterest = viewModel::addInterest,
                onRemoveInterest = viewModel::removeInterest,
                onAddDislike = viewModel::addDislike,
                onRemoveDislike = viewModel::removeDislike,
                onNotesChange = viewModel::updateNotes,
                onSelectArchetype = viewModel::selectArchetype
            )
        }
    }
}

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
    var interestInput by remember { mutableStateOf("") }
    var dislikeInput by remember { mutableStateOf("") }
    var showRelationshipDropdown by remember { mutableStateOf(false) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        // Name input
        OutlinedTextField(
            value = formState.name,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.name)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Emoji picker
        Text(text = stringResource(R.string.choose_emoji), style = MaterialTheme.typography.labelMedium)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf("🎁", "❤️", "👨", "👩", "👶", "👴", "👵", "🧑‍💼", "👨‍👩‍👧", "🐶")) { emoji ->
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable { onEmojiChange(emoji) },
                    color = if (emoji == formState.avatarEmoji)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(emoji, style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }
        }

        // Relationship type
        ExposedDropdownMenuBox(
            expanded = showRelationshipDropdown,
            onExpandedChange = { showRelationshipDropdown = it }
        ) {
            OutlinedTextField(
                value = formState.relationshipType.name.lowercase().replaceFirstChar { it.uppercase() },
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.relationship)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showRelationshipDropdown) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = showRelationshipDropdown,
                onDismissRequest = { showRelationshipDropdown = false }
            ) {
                RelationshipType.entries.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        onClick = {
                            onRelationshipChange(type)
                            showRelationshipDropdown = false
                        }
                    )
                }
            }
        }

        // Archetypes
        Text(text = stringResource(R.string.quick_profiles), style = MaterialTheme.typography.labelMedium)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(archetypes) { archetype ->
                FilterChip(
                    selected = formState.selectedArchetypeId == archetype.id,
                    onClick = { onSelectArchetype(archetype) },
                    label = { Text("${archetype.emoji} ${archetype.title}") }
                )
            }
        }

        // Interests
        Text(text = stringResource(R.string.interests), style = MaterialTheme.typography.labelMedium)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            formState.interests.forEach { interest ->
                InputChip(
                    selected = true,
                    onClick = { onRemoveInterest(interest) },
                    label = { Text(interest) },
                    trailingIcon = {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                )
            }
        }
        OutlinedTextField(
            value = interestInput,
            onValueChange = { interestInput = it },
            label = { Text(stringResource(R.string.add_interest)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = {
                    if (interestInput.isNotBlank()) {
                        onAddInterest(interestInput)
                        interestInput = ""
                    }
                }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                if (interestInput.isNotBlank()) {
                    onAddInterest(interestInput)
                    interestInput = ""
                }
            })
        )

        // Dislikes
        Text(text = stringResource(R.string.dislikes), style = MaterialTheme.typography.labelMedium)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            formState.dislikes.forEach { dislike ->
                InputChip(
                    selected = true,
                    onClick = { onRemoveDislike(dislike) },
                    label = { Text(dislike) },
                    trailingIcon = {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                )
            }
        }
        OutlinedTextField(
            value = dislikeInput,
            onValueChange = { dislikeInput = it },
            label = { Text(stringResource(R.string.add_dislike)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = {
                    if (dislikeInput.isNotBlank()) {
                        onAddDislike(dislikeInput)
                        dislikeInput = ""
                    }
                }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                if (dislikeInput.isNotBlank()) {
                    onAddDislike(dislikeInput)
                    dislikeInput = ""
                }
            })
        )

        // Notes
        OutlinedTextField(
            value = formState.notes,
            onValueChange = onNotesChange,
            label = { Text(stringResource(R.string.notes)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}
