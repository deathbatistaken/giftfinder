package com.gift.finder.ui.screens.wishlist

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
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
import com.gift.finder.ui.theme.GiftGreen
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
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Wishlist",
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
                    Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = aura.primaryColor)
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
                                    ) {}
                                    Text("🔖", style = MaterialTheme.typography.displayLarge)
                                }
                                
                                Text(
                                    "Wishlist is Empty",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = aura.primaryColor
                                )
                                Text(
                                    "Save your favorite cosmic gift ideas here!",
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
        }
    }
}

@Composable
private fun WishlistGiftCard(
    suggestion: GiftSuggestion,
    onRemove: () -> Unit,
    onShop: () -> Unit
) {
    val aura = LocalCosmicAura.current
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, aura.primaryColor.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(suggestion.category.emoji, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        suggestion.category.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        suggestion.category.description,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onShop,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = aura.primaryColor),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Shop now".uppercase(), fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}
