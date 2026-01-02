package com.gift.finder.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items

import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.gift.finder.R
import com.gift.finder.domain.model.Person
import com.gift.finder.domain.model.SpecialDate
import com.gift.finder.ui.theme.*
import com.gift.finder.ui.viewmodels.HomeUiState
import com.gift.finder.ui.viewmodels.HomeViewModel
import com.gift.finder.ui.components.premium.AnimatedMeshBackground
import com.gift.finder.ui.components.premium.GlassCard
import com.gift.finder.ui.components.premium.PremiumButton
import com.gift.finder.ui.components.premium.SkeletonHomeScreen
import com.gift.finder.ui.components.premium.PremiumPullToRefresh
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Home/Dashboard screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    windowSizeClass: WindowSizeClass,
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToAddPerson: () -> Unit,
    onNavigateToPersonDetail: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToPaywall: () -> Unit,
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToImportContacts: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    var showAddOptions by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search))
                    }
                    IconButton(onClick = onNavigateToCalendar) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = stringResource(R.string.calendar))
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            when (val state = uiState) {
                is HomeUiState.Success -> {
                    FloatingActionButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (state.canAddPerson) {
                                showAddOptions = true
                            } else {
                                onNavigateToPaywall()
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_person))
                    }
                }
                else -> {}
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedMeshBackground()
            
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    SkeletonHomeScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    )
                }

                is HomeUiState.Success -> {
                    if (state.persons.isEmpty()) {
                        EmptyHomeState(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding),
                            onAddPerson = { showAddOptions = true }
                        )
                    } else {
                        HomeContent(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding),
                            persons = state.persons,
                            upcomingDates = state.upcomingDates,
                            onPersonClick = onNavigateToPersonDetail,
                            windowSizeClass = windowSizeClass
                        )
                    }
                }

                is HomeUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(state.message)
                    }
                }
            }

            if (showAddOptions) {
                AlertDialog(
                    onDismissRequest = { showAddOptions = false },
                    title = { Text(stringResource(R.string.add_person)) },
                    text = {
                        Column {
                            TextButton(
                                onClick = {
                                    showAddOptions = false
                                    onNavigateToAddPerson()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.create_manually), style = MaterialTheme.typography.bodyLarge)
                            }
                            TextButton(
                                onClick = {
                                    showAddOptions = false
                                    onNavigateToImportContacts()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.import_contacts), style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { showAddOptions = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun HomeContent(
    modifier: Modifier = Modifier,
    persons: List<Person>,
    upcomingDates: List<SpecialDate>,
    onPersonClick: (Long) -> Unit,
    windowSizeClass: WindowSizeClass
) {
    val columns = when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> 1
        WindowWidthSizeClass.Medium -> 2
        WindowWidthSizeClass.Expanded -> 3
        else -> 1
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cosmic Summary
        if (upcomingDates.isNotEmpty()) {
            item(span = { GridItemSpan(columns) }) {
                CosmicSummaryCard(
                    upcomingDates = upcomingDates,
                    persons = persons,
                    onPersonClick = onPersonClick
                )
            }
        }

        // Upcoming events section
        if (upcomingDates.isNotEmpty()) {
            item(span = { GridItemSpan(columns) }) {
                Text(
                    text = stringResource(R.string.upcoming_events),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item(span = { GridItemSpan(columns) }) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(upcomingDates.take(5)) { date ->
                        UpcomingDateCard(
                            specialDate = date,
                            onClick = { onPersonClick(date.personId) }
                        )
                    }
                }
            }
        }

        // People section
        item(span = { GridItemSpan(columns) }) {
            Text(
                text = stringResource(R.string.your_people),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        items(persons) { person ->
            PersonCard(
                person = person,
                onClick = { onPersonClick(person.id) }
            )
        }
    }
}

@Composable
private fun UpcomingDateCard(
    specialDate: SpecialDate,
    onClick: () -> Unit
) {
    val daysUntil = specialDate.getDaysUntil()
    val urgencyColor = when {
        daysUntil <= 3 -> GiftRed
        daysUntil <= 7 -> GiftOrange
        else -> GiftGreen
    }
    val aura = LocalCosmicAura.current

    GlassCard(
        onClick = onClick,
        modifier = Modifier.width(180.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            urgencyColor.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            urgencyColor.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = urgencyColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = when (daysUntil) {
                            0 -> stringResource(R.string.today)
                            1 -> stringResource(R.string.tomorrow)
                            else -> stringResource(R.string.in_days, daysUntil)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = urgencyColor,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = specialDate.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun PersonCard(
    person: Person,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val aura = LocalCosmicAura.current
    val infiniteTransition = rememberInfiniteTransition(label = "person_card")
    val auraGlow by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_glow"
    )

    GlassCard(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Brush.linearGradient(
                colors = listOf(
                    aura.primaryColor.copy(alpha = auraGlow),
                    aura.primaryColor.copy(alpha = 0.1f)
                )
            )
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Glowing Avatar
            Box(contentAlignment = Alignment.Center) {
                Surface(
                    modifier = Modifier
                        .size(64.dp)
                        .graphicsLayer { alpha = auraGlow * 0.5f },
                    shape = CircleShape,
                    color = aura.primaryColor.copy(alpha = 0.15f)
                ) {}
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CircleShape,
                    color = aura.primaryColor.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, aura.primaryColor.copy(alpha = 0.3f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = person.avatarEmoji,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = person.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (person.interests.isNotEmpty()) {
                    Text(
                        text = person.interests.take(3).joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Upcoming indicator
            val upcomingDate = person.specialDates.minByOrNull { it.getDaysUntil() }
            if (upcomingDate != null && upcomingDate.getDaysUntil() <= 30) {
                val daysUntil = upcomingDate.getDaysUntil()
                Surface(
                    color = when {
                        daysUntil <= 3 -> GiftRed
                        daysUntil <= 7 -> GiftOrange
                        else -> GiftGreen
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${daysUntil}d",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CosmicSummaryCard(
    upcomingDates: List<SpecialDate>,
    persons: List<Person>,
    onPersonClick: (Long) -> Unit
) {
    val nextDate = upcomingDates.firstOrNull() ?: return
    val person = persons.find { it.id == nextDate.personId } ?: return
    val daysUntil = nextDate.getDaysUntil()
    val aura = LocalCosmicAura.current
    val isToday = daysUntil == 0

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onPersonClick(person.id) },
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isToday) GiftGold.copy(alpha = 0.5f) else aura.primaryColor.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = if (isToday) GiftGold.copy(alpha = 0.15f) else aura.primaryColor.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isToday) Icons.Default.Celebration else Icons.Default.Event,
                        contentDescription = stringResource(if (isToday) R.string.cd_celebration_icon else R.string.cd_calendar_icon),
                        tint = if (isToday) GiftGold else aura.primaryColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.cosmic_summary).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = if (isToday) GiftGold else aura.primaryColor,
                    letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isToday) {
                        stringResource(R.string.cosmic_summary_today, "${person.name} - ${nextDate.title}")
                    } else {
                        stringResource(R.string.cosmic_summary_upcoming, "${person.name} - ${nextDate.title}", daysUntil)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Surface(
                color = if (isToday) GiftGold else aura.primaryColor,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isToday) stringResource(R.string.today) else "${daysUntil}d",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyHomeState(
    modifier: Modifier = Modifier,
    onAddPerson: () -> Unit
) {
    val aura = LocalCosmicAura.current
    val infiniteTransition = rememberInfiniteTransition(label = "empty_state")
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.graphicsLayer { translationY = floatAnim },
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                color = aura.primaryColor.copy(alpha = 0.1f),
                border = androidx.compose.foundation.BorderStroke(2.dp, aura.primaryColor.copy(alpha = 0.2f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = stringResource(R.string.cd_gift_icon),
                        tint = aura.primaryColor,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
        Text(
            text = stringResource(R.string.empty_home_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.empty_home_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(40.dp))
        PremiumButton(
            text = stringResource(R.string.add_first_person),
            icon = Icons.Default.PersonAdd,
            onClick = onAddPerson
        )
    }
}
