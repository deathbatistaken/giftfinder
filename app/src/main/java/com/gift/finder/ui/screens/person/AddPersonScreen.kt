package com.gift.finder.ui.screens.person

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.gift.finder.R
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
