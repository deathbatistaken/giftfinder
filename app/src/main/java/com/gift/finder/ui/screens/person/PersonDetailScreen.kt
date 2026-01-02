package com.gift.finder.ui.screens.person

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Casino
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
import com.gift.finder.ui.components.premium.PersonaBadge
import com.gift.finder.ui.components.premium.PremiumButton
import com.gift.finder.ui.components.premium.PremiumOutlinedButton
import com.gift.finder.ui.components.premium.SkeletonPersonCard
import com.gift.finder.ui.components.premium.SkeletonCard
import com.gift.finder.ui.components.premium.SkeletonText
import kotlinx.coroutines.launch
import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState

/**
 * Person detail screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonDetailScreen(
    personId: Long,
    viewModel: PersonViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit = {},
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
                    IconButton(onClick = { onNavigateToEdit(personId) }) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_person))
                    }
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
                    Column(
                        modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Person card skeleton
                        SkeletonPersonCard()
                        // Action buttons skeleton
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SkeletonText(modifier = Modifier.weight(1f), width = 100.dp, height = 44.dp)
                            SkeletonText(modifier = Modifier.weight(1f), width = 100.dp, height = 44.dp)
                        }
                        // Details skeleton
                        SkeletonCard(height = 120.dp)
                        SkeletonCard(height = 80.dp)
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
                        onNavigateToWishlist = { onNavigateToWishlist(personId) },
                        onAddToCalendar = viewModel::addToCalendar
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
                    scope.launch {
                        viewModel.deletePerson(personId)
                        showDeleteDialog = false
                        onNavigateBack()
                    }
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
    onNavigateToWishlist: () -> Unit,
    onAddToCalendar: (SpecialDate) -> Unit
) {
    var showAddDateDialog by remember { mutableStateOf(false) }

    // Add Special Date Dialog
    if (showAddDateDialog) {
        AddSpecialDateDialog(
            onDismiss = { showAddDateDialog = false },
            onConfirm = { title, dateType, month, day ->
                onAddSpecialDate(title, dateType, month, day)
                showAddDateDialog = false
            }
        )
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Persona Card
        item {
            val aura = LocalCosmicAura.current
            val infiniteTransition = rememberInfiniteTransition(label = "avatar_aura")
            
            val auraScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.25f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            val auraAlpha by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 0.5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "alpha"
            )

            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        Modifier.background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    aura.primaryColor.copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(32.dp)
                        )
                    )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar with Pulsating Halo
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        // Multi-layered Aura
                        Surface(
                            modifier = Modifier
                                .size(120.dp)
                                .graphicsLayer {
                                    scaleX = auraScale
                                    scaleY = auraScale
                                    alpha = auraAlpha
                                },
                            shape = CircleShape,
                            color = aura.primaryColor.copy(alpha = 0.1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, aura.primaryColor.copy(alpha = 0.3f))
                        ) {}
                        
                        Surface(
                            modifier = Modifier.size(110.dp),
                            shape = CircleShape,
                            color = aura.primaryColor.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(2.dp, aura.primaryColor.copy(alpha = 0.4f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(person.avatarEmoji, style = MaterialTheme.typography.displayMedium)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text(
                        person.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    val relationshipStringId = remember(person.relationshipType) {
                        val field = R.string::class.java.getField(person.relationshipType.displayKey)
                        field.getInt(null)
                    }
                    Surface(
                        color = aura.primaryColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            stringResource(relationshipStringId).uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = aura.primaryColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    if (personaSummary != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        GlassCard(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            cornerRadius = 16.dp,
                            border = androidx.compose.foundation.BorderStroke(1.dp, aura.primaryColor.copy(alpha = 0.1f))
                        ) {
                            Text(
                                personaSummary,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontStyle = FontStyle.Italic,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    if (dominantArchetype != null) {
                        Spacer(modifier = Modifier.height(24.dp))
                        PersonaBadge(archetype = dominantArchetype)
                        Spacer(modifier = Modifier.height(16.dp))
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
                PremiumButton(
                    text = stringResource(R.string.find_gift),
                    icon = Icons.Default.CardGiftcard,
                    onClick = onNavigateToSuggestions,
                    modifier = Modifier.weight(1f)
                )
                PremiumButton(
                    text = stringResource(R.string.roulette),
                    icon = Icons.Default.Casino,
                    containerColor = GiftPurple,
                    onClick = onNavigateToRoulette,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            PremiumOutlinedButton(
                text = stringResource(R.string.wishlist),
                icon = Icons.Default.Bookmark,
                onClick = onNavigateToWishlist,
                modifier = Modifier.fillMaxWidth()
            )
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
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.special_dates), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                TextButton(onClick = { showAddDateDialog = true }) {
                    Text(stringResource(R.string.add_special_date_action))
                }
            }
        }
        if (person.specialDates.isNotEmpty()) {
            items(person.specialDates) { date ->
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    SpecialDateCard(
                        specialDate = date,
                        onDelete = { onDeleteSpecialDate(date) },
                        onAddToCalendar = { onAddToCalendar(date) }
                    )
                }
            }
        } else {
            item {
                Text(
                    stringResource(R.string.no_special_dates),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
    onDelete: () -> Unit,
    onAddToCalendar: () -> Unit
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
            IconButton(onClick = onAddToCalendar) {
                Icon(Icons.Default.CalendarMonth, contentDescription = stringResource(R.string.add_to_calendar), tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.error)
            }
    }
}
