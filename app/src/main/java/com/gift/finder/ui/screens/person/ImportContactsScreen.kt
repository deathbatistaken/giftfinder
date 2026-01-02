package com.gift.finder.ui.screens.person

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gift.finder.R
import com.gift.finder.domain.model.Contact
import com.gift.finder.ui.theme.GiftGreen
import com.gift.finder.ui.viewmodels.ImportContactsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportContactsScreen(
    onNavigateBack: () -> Unit,
    onImportSuccess: () -> Unit,
    viewModel: ImportContactsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Permission handling
    var hasPermission by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasPermission = isGranted
        if (isGranted) {
            viewModel.loadContacts()
        }
    }

    LaunchedEffect(Unit) {
        val permissionCheck = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        )
        if (permissionCheck == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            hasPermission = true
            viewModel.loadContacts()
        } else {
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }
    
    LaunchedEffect(uiState.importComplete) {
        if (uiState.importComplete) {
            onImportSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.import_contacts)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    if (uiState.contacts.isNotEmpty()) {
                        TextButton(onClick = viewModel::selectAll) {
                            Text(stringResource(R.string.all))
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (uiState.selectedContactIds.isNotEmpty()) {
                Button(
                    onClick = viewModel::importSelectedContacts,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    enabled = !uiState.isImporting
                ) {
                    if (uiState.isImporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(stringResource(R.string.import_action, uiState.selectedContactIds.size))
                    }
                }
            }
        }
    ) { padding ->
        if (!hasPermission) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        stringResource(R.string.permission_required_contacts),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.READ_CONTACTS) }) {
                        Text(stringResource(R.string.grant_permission))
                    }
                }
            }
        } else if (uiState.isLoading) {
             Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                 CircularProgressIndicator()
             }
        } else if (uiState.contacts.isEmpty()) {
             Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                 Text(stringResource(R.string.no_contacts_found))
             }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(uiState.contacts, key = { it.id }) { contact ->
                    ContactItem(
                        contact = contact,
                        isSelected = uiState.selectedContactIds.contains(contact.id),
                        onToggle = { viewModel.toggleSelection(contact.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ContactItem(
    contact: Contact,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    ListItem(
        headlineContent = { Text(contact.name, maxLines = 1) },
        supportingContent = {
             if (contact.phoneNumber != null) {
                 Text(contact.phoneNumber)
             } else if (contact.birthdayDate != null) {
                 Text("${stringResource(R.string.emoji_cake)} ${contact.birthdayDate}")
             }
        },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) GiftGreen else MaterialTheme.colorScheme.primaryContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                 if (isSelected) {
                     Icon(
                         Icons.Default.Check,
                         contentDescription = null,
                         tint = Color.White
                     )
                 } else {
                     Text(
                         contact.getInitials(),
                         style = MaterialTheme.typography.titleMedium,
                         color = MaterialTheme.colorScheme.onPrimaryContainer
                     )
                 }
            }
        },
        modifier = Modifier.clickable(onClick = onToggle)
    )
}
