package com.gift.finder.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.Output
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.Color
import com.gift.finder.domain.manager.HapticEngine
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gift.finder.R
import com.gift.finder.ui.theme.*
import com.gift.finder.ui.viewmodels.SettingsViewModel
import com.gift.finder.ui.components.premium.AnimatedMeshBackground
import com.gift.finder.ui.components.premium.GlassCard
import com.gift.finder.ui.viewmodels.HapticViewModel
import kotlinx.coroutines.launch

/**
 * Settings screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: com.gift.finder.ui.viewmodels.SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToPaywall: () -> Unit,
    onNavigateToBudgetTracker: () -> Unit = {},
    hapticEngine: HapticEngine = hiltViewModel<com.gift.finder.ui.viewmodels.HapticViewModel>().hapticEngine
) {
    val subscriptionStatus by viewModel.subscriptionStatus.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val hapticsEnabled by viewModel.hapticsEnabled.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val cosmicAura by viewModel.cosmicAura.collectAsState()
    val reminderOffsets by viewModel.reminderOffsets.collectAsState()
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    val appLanguage by viewModel.appLanguage.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current
    val contentResolver = context.contentResolver
    val scope = rememberCoroutineScope()

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) {
            viewModel.exportData(
                uri = uri,
                contentResolver = contentResolver,
                onSuccess = { android.widget.Toast.makeText(context, context.getString(R.string.export_success), android.widget.Toast.LENGTH_SHORT).show() },
                onError = { android.widget.Toast.makeText(context, context.getString(R.string.export_error), android.widget.Toast.LENGTH_SHORT).show() }
            )
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            showDeleteDialog = false // Re-using delete dialog? No, import is destructive but let's just run it for now or add warning.
            // Ideally prompt heavily. Importing IS restoring.
             viewModel.importData(
                uri = uri,
                contentResolver = contentResolver,
                onSuccess = { 
                    android.widget.Toast.makeText(context, context.getString(R.string.import_success), android.widget.Toast.LENGTH_SHORT).show()
                    // Re-launch app or refresh data? StateFlows update automatically so UI should refresh.
                },
                onError = { android.widget.Toast.makeText(context, context.getString(R.string.import_error), android.widget.Toast.LENGTH_SHORT).show() }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.settings),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedMeshBackground()
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
            // Subscription section
            if (!subscriptionStatus.isPremium) {
                GlassCard(
                    onClick = onNavigateToPaywall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = GiftOrange)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.upgrade_to_pro),
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                stringResource(R.string.unlock_all_features),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = GiftPurple)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.subscription_active), fontWeight = FontWeight.Bold)
                            Text(stringResource(R.string.pro_member), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // Options inside GlassCards
            PaddingValues(16.dp).let { spacing ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.appearance),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        ListItem(
                            headlineContent = { Text(stringResource(R.string.theme)) },
                            supportingContent = { 
                                val themeText = when(themeMode) {
                                    "light" -> stringResource(R.string.theme_light)
                                    "dark" -> stringResource(R.string.theme_dark)
                                    else -> stringResource(R.string.theme_system)
                                }
                                Text(themeText) 
                            },
                            modifier = Modifier.clickable { showThemeDialog = true },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )

                        val languageMap = mapOf(
                            "en" to "English",
                            "tr" to "Türkçe",
                            "de" to "Deutsch",
                            "fr" to "Français",
                            "es" to "Español",
                            "ja" to "日本語"
                        )

                        ListItem(
                            headlineContent = { Text(stringResource(R.string.language)) },
                            supportingContent = { Text(languageMap[appLanguage] ?: "English") },
                            modifier = Modifier.clickable { showLanguageDialog = true },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )

                        ListItem(
                            headlineContent = { Text(stringResource(R.string.haptic_feedback)) },
                            trailingContent = {
                                Switch(
                                    checked = hapticsEnabled,
                                    onCheckedChange = { 
                                        scope.launch { hapticEngine.tap() }
                                        viewModel.setHapticsEnabled(it) 
                                    }
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )

                        Divider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

                        Text(
                            text = "Cosmic Aura",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(16.dp, 8.dp, 16.dp, 4.dp)
                        )

                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp)
                        ) {
                            items(com.gift.finder.domain.model.CosmicAura.entries) { aura ->
                                val isSelected = cosmicAura == aura
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable {
                                            scope.launch { hapticEngine.tap() }
                                            viewModel.setCosmicAura(aura)
                                        }
                                        .width(80.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier.size(64.dp),
                                        shape = CircleShape,
                                        color = if (isSelected) aura.primaryColor else aura.primaryColor.copy(alpha = 0.2f),
                                        border = androidx.compose.foundation.BorderStroke(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) Color.White else aura.primaryColor.copy(alpha = 0.5f)
                                        )
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(aura.emoji, style = MaterialTheme.typography.headlineSmall)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        aura.title.split(" ").first(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) aura.primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                GlassCard(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.notifications),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        ListItem(
                            headlineContent = { Text(stringResource(R.string.reminder_notifications)) },
                            supportingContent = { Text(stringResource(R.string.get_notified_events)) },
                            leadingContent = { Icon(Icons.Default.Notifications, contentDescription = null) },
                            trailingContent = {
                                Switch(
                                    checked = notificationsEnabled,
                                    onCheckedChange = { 
                                        scope.launch { hapticEngine.tap() }
                                        viewModel.setNotificationsEnabled(it) 
                                    }
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )

                        if (notificationsEnabled) {
                            Divider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                            
                            Text(
                                text = "Reminder Schedule",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(16.dp, 8.dp, 16.dp, 4.dp)
                            )

                            Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                                listOf(7 to "7 days before", 3 to "3 days before", 0 to "Same day").forEach { (days, label) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                scope.launch { hapticEngine.tap() }
                                                val newOffsets = reminderOffsets.toMutableList()
                                                if (days in newOffsets) newOffsets.remove(days) else newOffsets.add(days)
                                                viewModel.setReminderOffsets(newOffsets)
                                            }
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = days in reminderOffsets,
                                            onCheckedChange = null
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(label, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                GlassCard(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.data_management),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        ListItem(
                            headlineContent = { Text(stringResource(R.string.export_data)) },
                            leadingContent = { Icon(Icons.Default.Output, contentDescription = null) },
                            modifier = Modifier.clickable { 
                                val fileName = "GiftFinder_Backup_${System.currentTimeMillis()}.json"
                                exportLauncher.launch(fileName) 
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )

                         ListItem(
                            headlineContent = { Text(stringResource(R.string.import_data)) },
                            leadingContent = { Icon(Icons.Default.Input, contentDescription = null) },
                            modifier = Modifier.clickable { 
                                importLauncher.launch("application/json")
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                GlassCard(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.account),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        ListItem(
                            headlineContent = { Text(stringResource(R.string.budget_tracker)) },
                            supportingContent = { Text(stringResource(R.string.track_your_spending)) },
                            leadingContent = { Text("📊", style = MaterialTheme.typography.titleLarge) },
                            modifier = Modifier.clickable { onNavigateToBudgetTracker() },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )

                        ListItem(
                            headlineContent = { Text(stringResource(R.string.restore_purchases)) },
                            leadingContent = { Icon(Icons.Default.Refresh, contentDescription = null) },
                            modifier = Modifier.clickable { viewModel.restorePurchases() },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )

                        ListItem(
                            headlineContent = { 
                                Text(
                                    stringResource(R.string.delete_all_data),
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            leadingContent = { 
                                Icon(
                                    Icons.Default.Delete, 
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            modifier = Modifier.clickable { showDeleteDialog = true },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Version
            Text(
                text = stringResource(R.string.version) + " 1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        }
        }
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(stringResource(R.string.theme)) },
            text = {
                Column {
                    listOf("system", "light", "dark").forEach { mode ->
                        val modeText = when(mode) {
                            "light" -> stringResource(R.string.theme_light)
                            "dark" -> stringResource(R.string.theme_dark)
                            else -> stringResource(R.string.theme_system)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setThemeMode(mode)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = themeMode == mode,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(modeText)
                        }
                    }
                }
            },
            confirmButton = { }
        )
    }

    if (showLanguageDialog) {
        val languageMap = mapOf(
            "en" to "English",
            "tr" to "Türkçe",
            "de" to "Deutsch",
            "fr" to "Français",
            "es" to "Español",
            "ja" to "日本語"
        )
        
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.select_language)) },
            text = {
                Column {
                    languageMap.forEach { (code, name) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setAppLanguage(code)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = appLanguage == code,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(name)
                        }
                    }
                }
            },
            confirmButton = { }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_all_data)) },
            text = { Text(stringResource(R.string.delete_data_warning)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAllData()
                        showDeleteDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
