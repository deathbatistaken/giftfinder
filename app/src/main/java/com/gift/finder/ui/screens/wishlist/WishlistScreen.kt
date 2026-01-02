package com.gift.finder.ui.screens.wishlist

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gift.finder.R
import com.gift.finder.domain.model.GiftSuggestion
import com.gift.finder.ui.components.premium.AnimatedMeshBackground
import com.gift.finder.ui.components.premium.GlassCard
import com.gift.finder.ui.components.premium.SkeletonSuggestionCard
import com.gift.finder.ui.theme.GiftGreen
import com.gift.finder.ui.theme.GiftRed
import com.gift.finder.ui.theme.LocalCosmicAura
import com.gift.finder.ui.viewmodels.WishlistUiState
import com.gift.finder.ui.viewmodels.WishlistViewModel
import com.gift.finder.utils.ExportManager

/**
 * Wishlist screen to manage saved gifts for a person.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistScreen(
    personId: Long,
    windowSizeClass: WindowSizeClass,
    viewModel: WishlistViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val aura = LocalCosmicAura.current
    val appCurrency by viewModel.appCurrency.collectAsState()
    var purchasingGift by remember { mutableStateOf<GiftSuggestion?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.wishlist),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = aura.primaryColor
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    val currentState = uiState
                    if (currentState is WishlistUiState.Success) {
                        IconButton(onClick = {
                            val shareText = ExportManager.formatWishlist(context, currentState.person.name, currentState.savedGifts)
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_header, currentState.person.name)))
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share", tint = aura.primaryColor)
                        }
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
                is WishlistUiState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        repeat(3) {
                            SkeletonSuggestionCard()
                        }
                    }
                }
                is WishlistUiState.Success -> {
                    if (state.savedGifts.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                // Empty Halo
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier.size(100.dp),
                                        shape = CircleShape,
                                        color = aura.primaryColor.copy(alpha = 0.15f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, aura.primaryColor.copy(alpha = 0.3f))
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Bookmark,
                                                contentDescription = stringResource(R.string.cd_bookmark_icon),
                                                tint = aura.primaryColor,
                                                modifier = Modifier.size(48.dp)
                                            )
                                        }
                                    }
                                }
                                
                                Text(
                                    stringResource(R.string.wishlist_empty),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = aura.primaryColor
                                )
                                Text(
                                    stringResource(R.string.wishlist_hint),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 32.dp)
                                )
                            }
                        }
                    } else {
                        val columns = when (windowSizeClass.widthSizeClass) {
                            WindowWidthSizeClass.Compact -> 1
                            else -> 2
                        }
                        
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(columns),
                            modifier = Modifier.fillMaxSize().padding(padding),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.savedGifts) { suggestion ->
                                WishlistGiftCard(
                                    suggestion = suggestion,
                                    onRemove = { 
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        viewModel.removeGift(suggestion.category.id) 
                                    },
                                    onShop = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(suggestion.category.getStoreUrl()))
                                        context.startActivity(intent)
                                    },
                                    onMarkPurchased = {
                                        purchasingGift = suggestion
                                    }
                                )
                            }
                        }
                    }
                }
                is WishlistUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        Text(state.message)
                    }
                }
            }

            if (purchasingGift != null) {
                com.gift.finder.ui.screens.suggestions.MarkAsPurchasedDialog(
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
                            snackbarHostState.showSnackbar(context.getString(R.string.gift_marked_purchased))
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun WishlistGiftCard(
    suggestion: GiftSuggestion,
    onRemove: () -> Unit,
    onShop: () -> Unit,
    onMarkPurchased: () -> Unit
) {
    val aura = LocalCosmicAura.current
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, aura.primaryColor.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = aura.primaryColor.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(suggestion.category.emoji, style = MaterialTheme.typography.headlineMedium)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        suggestion.category.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        suggestion.category.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    suggestion.priceDropPercentage?.let { drop ->
                        Surface(
                            color = GiftRed,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingDown,
                                    contentDescription = stringResource(R.string.cd_price_drop_icon),
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "-$drop%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onShop,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = aura.primaryColor),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = stringResource(R.string.cd_shop_icon), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.shop_now).uppercase(), fontWeight = FontWeight.ExtraBold)
                    }
                }
                
                OutlinedButton(
                    onClick = onMarkPurchased,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, aura.primaryColor)
                ) {
                    Text(stringResource(R.string.mark_as_purchased).uppercase(), fontWeight = FontWeight.Bold, color = aura.primaryColor)
                }
            }
        }
    }
}
