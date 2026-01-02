package com.gift.finder.ui.screens.suggestions

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SearchOff
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
import kotlinx.coroutines.delay
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
import com.gift.finder.ui.components.premium.SkeletonSuggestionsScreen
import com.gift.finder.util.CurrencyUtils
import kotlinx.coroutines.launch

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
    onNavigateToPaywall: () -> Unit,
    hapticEngine: com.gift.finder.domain.manager.HapticEngine = hiltViewModel<com.gift.finder.ui.viewmodels.HapticViewModel>().hapticEngine
) {
    val haptic = LocalHapticFeedback.current
    val uiState by viewModel.uiState.collectAsState()
    val selectedStyle by viewModel.selectedStyle.collectAsState()
    val selectedBudget by viewModel.selectedBudget.collectAsState()
    val appCurrency by viewModel.appCurrency.collectAsState()
    var isDiscoveryMode by remember { mutableStateOf(true) }
    var purchasingGift by remember { mutableStateOf<GiftSuggestion?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showConfetti by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                Icon(
                    imageVector = Icons.Default.Casino,
                    contentDescription = stringResource(R.string.cd_casino_icon),
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedMeshBackground()
            if (showConfetti) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                    ConfettiEffect()
                }
            }
            
            when (val state = uiState) {
                is SuggestionsUiState.Loading -> {
                    val aura = LocalCosmicAura.current
                    val infiniteTransition = rememberInfiniteTransition(label = "crystal_ball")
                    
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 0.95f,
                        targetValue = 1.05f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1500, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulse"
                    )
                    
                    val rotation by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(3000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "rotation"
                    )

                    Box(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Professional Loading Animation
                            Box(contentAlignment = Alignment.Center) {
                                // Outer ring
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(120.dp)
                                        .graphicsLayer { rotationZ = rotation },
                                    color = aura.primaryColor.copy(alpha = 0.3f),
                                    strokeWidth = 4.dp,
                                    trackColor = Color.Transparent
                                )
                                
                                // Inner pulsing circle
                                Surface(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .graphicsLayer {
                                            scaleX = pulseScale
                                            scaleY = pulseScale
                                        },
                                    shape = CircleShape,
                                    color = aura.primaryColor.copy(alpha = 0.1f),
                                    border = androidx.compose.foundation.BorderStroke(2.dp, aura.primaryColor.copy(alpha = 0.3f))
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = aura.primaryColor,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            Text(
                                stringResource(R.string.consulting_stars),
                                style = MaterialTheme.typography.titleLarge,
                                color = aura.primaryColor,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                stringResource(R.string.analyzing_preferences),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
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
                            appCurrency = appCurrency,
                            onStyleSelected = {
                                scope.launch { hapticEngine.tap() }
                                viewModel.setStyle(it)
                            },
                            onBudgetSelected = {
                                scope.launch { hapticEngine.tap() }
                                viewModel.setBudget(it)
                            }
                        )

                        SuggestionsGrid(
                            suggestions = state.suggestions,
                            isDiscoveryMode = isDiscoveryMode,
                            columns = columns,
                            appCurrency = appCurrency,
                            onSuggestionRejected = { suggestion, reason ->
                                viewModel.rejectSuggestion(suggestion.category.id, reason)
                                scope.launch { hapticEngine.tap() }
                            },
                            onSaveToWishlist = { suggestion ->
                                viewModel.saveToWishlist(suggestion.category.id)
                                showConfetti = true
                                scope.launch {
                                    hapticEngine.celebration()
                                    snackbarHostState.showSnackbar(
                                        message = context.getString(R.string.saved_to_wishlist, suggestion.category.title),
                                        duration = SnackbarDuration.Short
                                    )
                                    delay(3000)
                                    showConfetti = false
                                }
                            },
                            onOpenStore = { suggestion ->
                                if (!suggestion.isPremiumLocked) {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(suggestion.category.getStoreUrl()))
                                    context.startActivity(intent)
                                }
                            },
                            onMarkAsPurchased = { suggestion ->
                                purchasingGift = suggestion
                            },
                            onUnlock = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onNavigateToPaywall()
                            }
                        )
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

            if (purchasingGift != null) {
                MarkAsPurchasedDialog(
                    suggestion = purchasingGift!!,
                    appCurrency = appCurrency,
                    onDismiss = { purchasingGift = null },
                    onConfirm = { price, occasion ->
                        val usdPrice = price?.let { 
                            com.gift.finder.util.CurrencyUtils.convertToUsd(it, appCurrency) 
                        }
                        viewModel.purchaseGift(
                            purchasingGift!!.category.id,
                            purchasingGift!!.category.title,
                            usdPrice,
                            occasion
                        )
                        purchasingGift = null
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = context.getString(R.string.gift_marked_purchased),
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SuggestionsGrid(
    suggestions: List<GiftSuggestion>,
    isDiscoveryMode: Boolean,
    columns: Int,
    appCurrency: String,
    onSuggestionRejected: (GiftSuggestion, RejectionReason) -> Unit,
    onSaveToWishlist: (GiftSuggestion) -> Unit,
    onOpenStore: (GiftSuggestion) -> Unit,
    onMarkAsPurchased: (GiftSuggestion) -> Unit,
    onUnlock: () -> Unit
) {
    if (isDiscoveryMode) {
        if (suggestions.isNotEmpty()) {
            // Discovery Mode Swipe Stack
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                val topSuggestion = suggestions.first()
                SwipeableGiftCard(
                    suggestion = topSuggestion,
                    onSwipedLeft = { reason ->
                        onSuggestionRejected(topSuggestion, reason)
                    },
                    onSwipedRight = {
                        onSaveToWishlist(topSuggestion)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (suggestions.size > 1) {
                    Text(
                        text = "+${suggestions.size - 1} " + stringResource(R.string.more_ideas),
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Empty state for Discovery
            val aura = LocalCosmicAura.current
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        color = aura.primaryColor.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = aura.primaryColor,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        stringResource(R.string.no_results),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.try_different_filters),
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
            items(suggestions) { suggestion ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + expandVertically()
                ) {
                    GiftSuggestionCard(
                        suggestion = suggestion,
                        appCurrency = appCurrency,
                        onSuggestionRejected = { s, reason -> onSuggestionRejected(s, reason) },
                        onSaveToWishlist = { s -> onSaveToWishlist(s) },
                        onOpenStore = { s -> onOpenStore(s) },
                        onMarkAsPurchased = { s -> onMarkAsPurchased(s) },
                        onUnlock = onUnlock
                    )
                }
            }
        }
    }
}

@Composable
private fun FiltersRow(
    selectedStyle: GiftStyle?,
    selectedBudget: BudgetRange?,
    appCurrency: String,
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
                FilterChip(
                    selected = selectedBudget == budget,
                    onClick = { onBudgetSelected(budget) },
                    label = { 
                        Text(
                            CurrencyUtils.formatRange(budget.minUsd, budget.maxUsd, appCurrency)
                        ) 
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GiftSuggestionCard(
    suggestion: GiftSuggestion,
    appCurrency: String,
    onSuggestionRejected: (GiftSuggestion, RejectionReason) -> Unit,
    onSaveToWishlist: (GiftSuggestion) -> Unit,
    onOpenStore: (GiftSuggestion) -> Unit,
    onMarkAsPurchased: (GiftSuggestion) -> Unit,
    onUnlock: () -> Unit
) {
    var showRejectDialog by remember { mutableStateOf(false) }

    val aura = LocalCosmicAura.current
    val infiniteTransition = rememberInfiniteTransition(label = "card_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    GlassCard(
        onClick = { if (suggestion.isPremiumLocked) onUnlock() else onOpenStore(suggestion) },
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (suggestion.priceDropPercentage != null) 
                GiftRed.copy(alpha = glowAlpha) 
            else 
                aura.primaryColor.copy(alpha = 0.3f)
        )
    ) {
        Box {
            Column(
                modifier = Modifier
                    .then(if (suggestion.isPremiumLocked) Modifier.blur(8.dp) else Modifier)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(aura.primaryColor.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = suggestion.category.emoji,
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = suggestion.category.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
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
                        Column(horizontalAlignment = Alignment.End) {
                            suggestion.priceDropPercentage?.let { drop ->
                                Surface(
                                    color = GiftRed,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                ) {
                                    Text(
                                        "-$drop%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Row {
                                IconButton(onClick = { showRejectDialog = true }) {
                                    Icon(Icons.Default.ThumbDown, contentDescription = stringResource(R.string.not_interested), modifier = Modifier.size(20.dp))
                                }
                                IconButton(onClick = { onMarkAsPurchased(suggestion) }) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = stringResource(R.string.mark_as_purchased), tint = GiftGreen, modifier = Modifier.size(20.dp))
                                }
                            }
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
                onSuggestionRejected(suggestion, reason)
                showRejectDialog = false
            }
        )
    }
}
