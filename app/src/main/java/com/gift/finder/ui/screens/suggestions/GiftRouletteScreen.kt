package com.gift.finder.ui.screens.suggestions

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gift.finder.R
import com.gift.finder.domain.model.GiftSuggestion
import com.gift.finder.ui.theme.*
import com.gift.finder.ui.viewmodels.GiftSuggestionsViewModel
import com.gift.finder.ui.components.premium.AnimatedMeshBackground
import com.gift.finder.ui.components.premium.GlassCard
import com.gift.finder.ui.components.premium.ConfettiEffect
import com.gift.finder.ui.components.premium.SpinningLoadingIcon
import com.gift.finder.ui.components.premium.GradientText
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.ArrowDropDown
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Gift Roulette screen for random gift selection.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GiftRouletteScreen(
    personId: Long,
    viewModel: GiftSuggestionsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    hapticEngine: com.gift.finder.domain.manager.HapticEngine = hiltViewModel<com.gift.finder.ui.viewmodels.HapticViewModel>().hapticEngine
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var isSpinning by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<GiftSuggestion?>(null) }
    var showResult by remember { mutableStateOf(false) }

    val rotation = remember { Animatable(0f) }

    // Haptic feedback loop during spin
    LaunchedEffect(isSpinning) {
        if (isSpinning) {
            var delayTime = 50L
            while (isSpinning) {
                hapticEngine.spin()
                delay(delayTime)
                // Gradually slow down haptics as the wheel slows down
                if (delayTime < 200) delayTime += 5
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.gift_roulette),
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
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
            if (showResult && result != null) {
                // Show result with celebration
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    ConfettiEffect()
                    ResultCard(
                        suggestion = result!!,
                        onOpenStore = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(result!!.category.getStoreUrl()))
                            context.startActivity(intent)
                        },
                        onSpinAgain = {
                            showResult = false
                            result = null
                        }
                    )
                }
            } else {
                val aura = LocalCosmicAura.current
                // Cinematic Roulette wheel
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer glow
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        aura.primaryColor.copy(alpha = 0.4f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    )
                    
                    // The Wheel
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .rotate(rotation.value)
                            .background(
                                brush = Brush.sweepGradient(
                                    colors = listOf(
                                        aura.primaryColor, 
                                        aura.primaryColor.copy(alpha = 0.5f), 
                                        aura.primaryColor.copy(alpha = 0.8f),
                                        aura.primaryColor.copy(alpha = 0.3f),
                                        aura.primaryColor
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Inner ring
                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.2f), CircleShape)
                        )
                        
                        // Center icon
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(MaterialTheme.colorScheme.surface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Casino,
                                contentDescription = stringResource(R.string.cd_casino_icon),
                                tint = aura.primaryColor,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                    
                    // Pointer
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.TopCenter)
                            .offset(y = (-12).dp),
                        tint = aura.primaryColor
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = if (isSpinning) stringResource(R.string.spinning) else stringResource(R.string.tap_to_spin),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = stringResource(R.string.roulette_description),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (!isSpinning) {
                            scope.launch {
                                isSpinning = true
                                // Dramatic spin: fast acceleration, slow deceleration
                                launch {
                                    rotation.animateTo(
                                        targetValue = rotation.value + 360f * 10f, // 10 full rotations
                                        animationSpec = tween(
                                            durationMillis = 4000,
                                            easing = FastOutSlowInEasing
                                        )
                                    )
                                }
                                delay(4000)
                                result = viewModel.getRandomGift()
                                hapticEngine.celebration()
                                isSpinning = false
                                showResult = true
                            }
                        }
                    },
                    enabled = !isSpinning,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(
                        stringResource(R.string.spin).uppercase(),
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }  // else (roulette wheel)

            }  // Column
        }  // Box
    }  // Scaffold
}  // GiftRouletteScreen

@Composable
private fun ResultCard(
    suggestion: GiftSuggestion,
    onOpenStore: () -> Unit,
    onSpinAgain: () -> Unit
) {
    val aura = LocalCosmicAura.current
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, aura.primaryColor.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Celebration icon
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = GiftGold.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(2.dp, GiftGold.copy(alpha = 0.3f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Celebration,
                        contentDescription = null,
                        tint = GiftGold,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Category icon container
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = aura.primaryColor.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = suggestion.category.emoji,
                        style = MaterialTheme.typography.headlineLarge
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = suggestion.category.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = suggestion.category.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(28.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onSpinAgain,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, aura.primaryColor)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.cd_refresh_icon), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.spin_again), fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onOpenStore,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = aura.primaryColor)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = stringResource(R.string.cd_shop_icon), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.shop_now), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
