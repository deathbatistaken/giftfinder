package com.gift.finder.ui.screens.suggestions

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gift.finder.R
import com.gift.finder.domain.model.BudgetRange
import com.gift.finder.domain.model.GiftStyle
import com.gift.finder.domain.model.GiftSuggestion
import com.gift.finder.domain.model.RejectionReason
import com.gift.finder.ui.theme.*
import com.gift.finder.ui.viewmodels.GiftSuggestionsViewModel
import com.gift.finder.ui.viewmodels.SuggestionsUiState
import com.gift.finder.ui.components.premium.AnimatedMeshBackground
import com.gift.finder.ui.components.premium.GlassCard
import com.gift.finder.ui.components.premium.SwipeableGiftCard
import com.gift.finder.ui.components.premium.ConfettiEffect

/**
 * Gift suggestions screen.
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GiftSuggestionsScreen(
    personId: Long,
    windowSizeClass: WindowSizeClass,
    viewModel: GiftSuggestionsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToRoulette: () -> Unit,
    onNavigateToPaywall: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val uiState by viewModel.uiState.collectAsState()
    val selectedStyle by viewModel.selectedStyle.collectAsState()
    val selectedBudget by viewModel.selectedBudget.collectAsState()
    var isDiscoveryMode by remember { mutableStateOf(true) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.gift_ideas),
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
                actions = {
                    IconButton(onClick = { isDiscoveryMode = !isDiscoveryMode }) {
                        Icon(
                            imageVector = if (isDiscoveryMode) Icons.Default.GridView else Icons.Default.ViewCarousel,
                            contentDescription = stringResource(if (isDiscoveryMode) R.string.grid_view else R.string.discovery_mode),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToRoulette,
                containerColor = GiftPurple,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
            ) {
                Text("🎰", style = MaterialTheme.typography.headlineSmall)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedMeshBackground()
            
            when (val state = uiState) {
                is SuggestionsUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is SuggestionsUiState.Success -> {
                    val haptic = LocalHapticFeedback.current
                    val columns = when (windowSizeClass.widthSizeClass) {
                        WindowWidthSizeClass.Compact -> 1
                        WindowWidthSizeClass.Medium -> 2
                        WindowWidthSizeClass.Expanded -> 2
                        else -> 1
                    }

                    Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                        FiltersRow(
                            selectedStyle = selectedStyle,
                            selectedBudget = selectedBudget,
                            onStyleSelected = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.setStyle(it)
                            },
                            onBudgetSelected = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.setBudget(it)
                            }
                        )

                        if (isDiscoveryMode) {
                            if (state.suggestions.isNotEmpty()) {
                                // Discovery Mode Swipe Stack
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val topSuggestion = state.suggestions.first()
                                    SwipeableGiftCard(
                                        suggestion = topSuggestion,
                                        onSwipedLeft = { reason ->
                                            viewModel.rejectSuggestion(topSuggestion.category.id, reason)
                                        },
                                        onSwipedRight = {
                                            // Handle Save/Interest
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    
                                    if (state.suggestions.size > 1) {
                                        Text(
                                            text = "+${state.suggestions.size - 1} " + stringResource(R.string.more_ideas),
                                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else {
                                // Empty state for Discovery
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("✨", style = MaterialTheme.typography.displayLarge)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            stringResource(R.string.no_results),
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "Try changing your style or budget filters!",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            // Classic Grid View
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(columns),
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.suggestions) { suggestion ->
                                    GiftSuggestionCard(
                                        suggestion = suggestion,
                                        onReject = { reason -> 
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            viewModel.rejectSuggestion(suggestion.category.id, reason) 
                                        },
                                        onOpenStore = {
                                            if (!suggestion.isPremiumLocked) {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(suggestion.category.getStoreUrl()))
                                                context.startActivity(intent)
                                            }
                                        },
                                        onUnlock = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onNavigateToPaywall()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                is SuggestionsUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(padding),
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
private fun FiltersRow(
    selectedStyle: GiftStyle?,
    selectedBudget: BudgetRange?,
    onStyleSelected: (GiftStyle?) -> Unit,
    onBudgetSelected: (BudgetRange?) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        // Style filters
        Text(stringResource(R.string.style), style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(4.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = selectedStyle == null,
                    onClick = { onStyleSelected(null) },
                    label = { Text(stringResource(R.string.all)) }
                )
            }
            items(GiftStyle.entries.toList()) { style ->
                val stringId = remember(style) {
                    val field = R.string::class.java.getField(style.displayKey)
                    field.getInt(null)
                }
                FilterChip(
                    selected = selectedStyle == style,
                    onClick = { onStyleSelected(style) },
                    label = { Text(stringResource(stringId)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Budget filters
        Text(stringResource(R.string.budget), style = MaterialTheme.typography.labelMedium)
        Spacer(modifier = Modifier.height(4.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = selectedBudget == null,
                    onClick = { onBudgetSelected(null) },
                    label = { Text(stringResource(R.string.all)) }
                )
            }
            items(BudgetRange.entries.toList()) { budget ->
                val stringId = remember(budget) {
                    val field = R.string::class.java.getField(budget.displayKey)
                    field.getInt(null)
                }
                FilterChip(
                    selected = selectedBudget == budget,
                    onClick = { onBudgetSelected(budget) },
                    label = { Text(stringResource(stringId)) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GiftSuggestionCard(
    suggestion: GiftSuggestion,
    onReject: (RejectionReason) -> Unit,
    onOpenStore: () -> Unit,
    onUnlock: () -> Unit
) {
    var showRejectDialog by remember { mutableStateOf(false) }

    GlassCard(
        onClick = { if (suggestion.isPremiumLocked) onUnlock() else onOpenStore() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Box {
            Column(
                modifier = Modifier
                    .then(if (suggestion.isPremiumLocked) Modifier.blur(8.dp) else Modifier)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = suggestion.category.emoji,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = suggestion.category.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = suggestion.category.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (!suggestion.isPremiumLocked) {
                        IconButton(onClick = { showRejectDialog = true }) {
                            Icon(Icons.Default.ThumbDown, contentDescription = stringResource(R.string.not_interested))
                        }
                    }
                }

                if (suggestion.matchReasons.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        suggestion.matchReasons.take(2).forEach { reason ->
                            Surface(
                                color = GiftGreen.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = reason,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GiftGreen,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (suggestion.isPremiumLocked) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(stringResource(R.string.unlock_pro), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }

    if (showRejectDialog) {
        RejectionReasonDialog(
            giftTitle = suggestion.category.title,
            onDismiss = { showRejectDialog = false },
            onReasonSelected = { reason ->
                onReject(reason)
                showRejectDialog = false
            }
        )
    }
}
