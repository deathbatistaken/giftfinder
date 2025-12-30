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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
    onNavigateToPaywall: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current

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
                                onNavigateToAddPerson()
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
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is HomeUiState.Success -> {
                    if (state.persons.isEmpty()) {
                        EmptyHomeState(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(padding),
                            onAddPerson = onNavigateToAddPerson
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

    Card(
        onClick = onClick,
        modifier = Modifier.width(160.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            urgencyColor.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Text(
                text = when (daysUntil) {
                    0 -> stringResource(R.string.today)
                    1 -> stringResource(R.string.tomorrow)
                    else -> stringResource(R.string.in_days, daysUntil)
                },
                style = MaterialTheme.typography.labelSmall,
                color = urgencyColor,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = specialDate.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
    GlassCard(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = Modifier
            .fillMaxWidth()
            .then(
                Modifier.background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            aura.primaryColor.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        center = androidx.compose.ui.geometry.Offset(0f, 0f),
                        radius = 200f
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
            ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Brush.linearGradient(
                colors = listOf(
                    aura.primaryColor.copy(alpha = 0.5f),
                    aura.primaryColor.copy(alpha = 0.1f)
                )
            )
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = person.avatarEmoji,
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = person.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = person.interests.take(3).joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Upcoming indicator
            val upcomingDate = person.specialDates.minByOrNull { it.getDaysUntil() }
            if (upcomingDate != null && upcomingDate.getDaysUntil() <= 30) {
                Surface(
                    color = when {
                        upcomingDate.getDaysUntil() <= 3 -> GiftRed
                        upcomingDate.getDaysUntil() <= 7 -> GiftOrange
                        else -> GiftGreen
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${upcomingDate.getDaysUntil()}d",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyHomeState(
    modifier: Modifier = Modifier,
    onAddPerson: () -> Unit
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🎁",
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.empty_home_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.empty_home_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onAddPerson) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.add_first_person))
        }
    }
}
