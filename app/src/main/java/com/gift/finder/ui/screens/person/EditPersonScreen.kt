package com.gift.finder.ui.screens.person

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gift.finder.R
import com.gift.finder.domain.model.SpecialDateType
import com.gift.finder.ui.viewmodels.PersonUiState
import com.gift.finder.ui.viewmodels.PersonViewModel
import com.gift.finder.ui.theme.*
import com.gift.finder.ui.components.premium.AnimatedMeshBackground
import com.gift.finder.ui.components.premium.GlassCard
import kotlinx.coroutines.launch

/**
 * Screen for editing an existing person.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPersonScreen(
    personId: Long,
    viewModel: PersonViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onSaved: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val formState by viewModel.formState.collectAsState()
    val scope = rememberCoroutineScope()
    var showAddDateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.edit_person),
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
                                viewModel.savePerson()
                                onSaved()
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
        floatingActionButton = {
            if (uiState is PersonUiState.Loaded) {
                FloatingActionButton(onClick = { showAddDateDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_special_date))
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedMeshBackground()
            
            when (val state = uiState) {
                is PersonUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is PersonUiState.Loaded -> {
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

                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Text(stringResource(R.string.person_not_found))
                    }
                }
            }
        }
    }

    if (showAddDateDialog) {
        AddSpecialDateDialog(
            onDismiss = { showAddDateDialog = false },
            onConfirm = { title, dateType, month, day ->
                scope.launch {
                    viewModel.addSpecialDate(title, dateType, month, day)
                    showAddDateDialog = false
                }
            }
        )
    }
}
