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
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var isSpinning by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<GiftSuggestion?>(null) }
    var showResult by remember { mutableStateOf(false) }

    val rotation by rememberInfiniteTransition(label = "spin").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

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
                // Show result
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
            } else {
                // Roulette wheel
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .then(if (isSpinning) Modifier.rotate(rotation) else Modifier)
                        .background(
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    GiftPink,
                                    GiftPurple,
                                    GiftBlue,
                                    GiftGreen,
                                    GiftOrange,
                                    GiftRed,
                                    GiftPink
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(MaterialTheme.colorScheme.surface, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🎰",
                            style = MaterialTheme.typography.displaySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = if (isSpinning) stringResource(R.string.spinning) else stringResource(R.string.tap_to_spin),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = stringResource(R.string.roulette_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (!isSpinning) {
                            isSpinning = true
                            scope.launch {
                                delay(2000) // Spin for 2 seconds
                                result = viewModel.getRandomGift()
                                isSpinning = false
                                showResult = true
                            }
                        }
                    },
                    enabled = !isSpinning,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.spin))
                }
            }
        }
    }
}

@Composable
private fun ResultCard(
    suggestion: GiftSuggestion,
    onOpenStore: () -> Unit,
    onSpinAgain: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎉",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = suggestion.category.emoji,
                style = MaterialTheme.typography.displayMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = suggestion.category.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = suggestion.category.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onSpinAgain,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.spin_again))
                }
                Button(
                    onClick = onOpenStore,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.shop_now))
                }
            }
        }
    }
}
