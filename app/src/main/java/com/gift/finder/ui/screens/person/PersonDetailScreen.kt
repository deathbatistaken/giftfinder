package com.gift.finder.ui.screens.person

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gift.finder.R
import com.gift.finder.domain.manager.HapticEngine
import com.gift.finder.domain.model.Person
import com.gift.finder.domain.model.SpecialDate
import com.gift.finder.domain.model.SpecialDateType
import com.gift.finder.ui.theme.*
import com.gift.finder.ui.viewmodels.HapticViewModel
import com.gift.finder.ui.viewmodels.PersonUiState
import com.gift.finder.ui.viewmodels.PersonViewModel
import com.gift.finder.ui.components.premium.AnimatedMeshBackground
import com.gift.finder.ui.components.premium.GlassCard
import kotlinx.coroutines.launch

/**
 * Person detail screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonDetailScreen(
    personId: Long,
    viewModel: PersonViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToSuggestions: () -> Unit,
    onNavigateToRoulette: (Long) -> Unit,
    onNavigateToWishlist: (Long) -> Unit,
    hapticEngine: HapticEngine = hiltViewModel<HapticViewModel>().hapticEngine
) {
    val uiState by viewModel.uiState.collectAsState()
    val dominantArchetype by viewModel.dominantArchetype.collectAsState()
    val personaSummary by viewModel.personaSummary.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when (val state = uiState) {
                        is PersonUiState.Loaded -> Text(state.person.name, fontWeight = FontWeight.ExtraBold)
                        else -> Text("")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedMeshBackground()

            when (val state = uiState) {
                is PersonUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is PersonUiState.Loaded -> {
                    PersonDetailContent(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        person = state.person,
                        dominantArchetype = dominantArchetype,
                        personaSummary = personaSummary,
                        onAddSpecialDate = { title, type, month, day ->
                            scope.launch {
                                viewModel.addSpecialDate(title, type, month, day)
                                hapticEngine.success()
                            }
                        },
                        onDeleteSpecialDate = { date ->
                            scope.launch {
                                viewModel.deleteSpecialDate(date)
                                hapticEngine.tap()
                            }
                        },
                        onNavigateToSuggestions = onNavigateToSuggestions,
                        onNavigateToRoulette = { onNavigateToRoulette(personId) },
                        onNavigateToWishlist = { onNavigateToWishlist(personId) }
                    )
                }

                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.person_not_found))
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_person_title)) },
            text = { Text(stringResource(R.string.delete_person_message)) },
            confirmButton = {
                TextButton(onClick = {
                    // Delete would be handled by HomeViewModel
                    showDeleteDialog = false
                    onNavigateBack()
                }) {
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PersonDetailContent(
    modifier: Modifier = Modifier,
    person: Person,
    dominantArchetype: com.gift.finder.domain.model.Archetype?,
    personaSummary: String?,
    onAddSpecialDate: (String, SpecialDateType, Int, Int) -> Unit,
    onDeleteSpecialDate: (SpecialDate) -> Unit,
    onNavigateToSuggestions: () -> Unit,
    onNavigateToRoulette: () -> Unit,
    onNavigateToWishlist: () -> Unit
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Persona Card
        item {
            val aura = LocalCosmicAura.current
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        Modifier.background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    aura.primaryColor.copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                    ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Brush.linearGradient(
                        colors = listOf(
                            aura.primaryColor.copy(alpha = 0.4f),
                            Color.Transparent,
                            aura.primaryColor.copy(alpha = 0.2f)
                        )
                    )
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar with Halo
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(108.dp),
                            shape = CircleShape,
                            color = aura.primaryColor.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(2.dp, aura.primaryColor.copy(alpha = 0.3f))
                        ) {}
                        Text(person.avatarEmoji, style = MaterialTheme.typography.displayMedium)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        person.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    val relationshipStringId = remember(person.relationshipType) {
                        val field = R.string::class.java.getField(person.relationshipType.displayKey)
                        field.getInt(null)
                    }
                    Text(
                        stringResource(relationshipStringId).uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )

                    if (personaSummary != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            personaSummary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontStyle = FontStyle.Italic,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier
                                .padding(horizontal = 24.dp)
                                .graphicsLayer { alpha = 0.9f }
                        )
                    }

                    if (dominantArchetype != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            color = aura.primaryColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, aura.primaryColor.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(dominantArchetype.emoji, style = MaterialTheme.typography.titleSmall)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    dominantArchetype.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = aura.primaryColor
                                )
                            }
                        }
                    }
                }
            }
        }

        // Action buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onNavigateToSuggestions,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🎁 " + stringResource(R.string.find_gift))
                }
                OutlinedButton(
                    onClick = onNavigateToRoulette,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🎰 " + stringResource(R.string.roulette))
                }
            }
            
            OutlinedButton(
                onClick = onNavigateToWishlist,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🔖 Wishlist")
            }
        }

        // Interests
        if (person.interests.isNotEmpty()) {
            item {
                Text(stringResource(R.string.interests), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    person.interests.forEach { interest ->
                        AssistChip(onClick = { }, label = { Text(interest) })
                    }
                }
            }
        }

        // Special dates
        if (person.specialDates.isNotEmpty()) {
            item {
                Text(stringResource(R.string.special_dates), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            items(person.specialDates) { date ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    SpecialDateCard(
                        specialDate = date,
                        onDelete = { onDeleteSpecialDate(date) }
                    )
                }
            }
        }

        // Gift history
        if (person.giftHistory.isNotEmpty()) {
            item {
                Text(stringResource(R.string.gift_history), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            items(person.giftHistory) { gift ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(gift.categoryTitle, fontWeight = FontWeight.Medium)
                            if (gift.occasion.isNotBlank()) {
                                Text(gift.occasion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        if (gift.price != null) {
                            Text("$${gift.price}", fontWeight = FontWeight.Bold, color = GiftGreen)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecialDateCard(
    specialDate: SpecialDate,
    onDelete: () -> Unit
) {
    val daysUntil = specialDate.getDaysUntil()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(specialDate.title, fontWeight = FontWeight.Medium)
                Text(
                    "${specialDate.month}/${specialDate.dayOfMonth}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                color = when {
                    daysUntil <= 3 -> GiftRed
                    daysUntil <= 7 -> GiftOrange
                    else -> GiftGreen
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = when (daysUntil) {
                        0 -> stringResource(R.string.today)
                        1 -> stringResource(R.string.tomorrow)
                        else -> stringResource(R.string.in_days, daysUntil)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.error)
            }
    }
}
