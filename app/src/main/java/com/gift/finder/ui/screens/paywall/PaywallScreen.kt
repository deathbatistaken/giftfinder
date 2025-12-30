package com.gift.finder.ui.screens.paywall

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.billingclient.api.ProductDetails
import com.gift.finder.R
import com.gift.finder.data.manager.BillingConnectionState
import com.gift.finder.data.manager.BillingManager
import com.gift.finder.data.manager.PurchaseState
import com.gift.finder.ui.theme.*
import com.gift.finder.ui.viewmodels.SettingsViewModel
import com.gift.finder.ui.components.premium.AnimatedMeshBackground
import com.gift.finder.ui.components.premium.GlassCard

/**
 * Paywall screen for subscription purchases.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onDismiss: () -> Unit,
    onPurchaseSuccess: () -> Unit
) {
    val products by viewModel.products.collectAsState()
    val billingState by viewModel.billingConnectionState.collectAsState()
    val purchaseState by viewModel.purchaseState.collectAsState()
    val context = LocalContext.current
    val aura = LocalCosmicAura.current

    LaunchedEffect(purchaseState) {
        if (purchaseState is PurchaseState.Success) {
            onPurchaseSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
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
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            // Header with Halo
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    color = aura.primaryColor.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, aura.primaryColor.copy(alpha = 0.3f))
                ) {}
                Text(
                    text = "🎁",
                    style = MaterialTheme.typography.displayLarge
                )
            }

            Text(
                text = stringResource(R.string.paywall_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = stringResource(R.string.paywall_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Features
            FeatureList(auraColor = aura.primaryColor)

            Spacer(modifier = Modifier.height(32.dp))

            // Subscription options
            when (billingState) {
                is BillingConnectionState.Connected -> {
                    products.forEach { product ->
                        SubscriptionOption(
                            product = product,
                            isRecommended = product.productId == BillingManager.PRODUCT_YEARLY,
                            auraColor = aura.primaryColor,
                            onClick = {
                                (context as? Activity)?.let { activity ->
                                    // billingManager.launchPurchase would be called here
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                is BillingConnectionState.Connecting -> {
                    CircularProgressIndicator(color = aura.primaryColor)
                }
                else -> {
                    // Fallback UI for demo
                    DemoSubscriptionOptions(auraColor = aura.primaryColor)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Restore purchases
            TextButton(onClick = { viewModel.restorePurchases() }) {
                Text(stringResource(R.string.restore_purchases), color = aura.primaryColor)
            }

            // Terms
            Text(
                text = stringResource(R.string.subscription_terms),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp)
            )
            }
        }
    }
}

@Composable
private fun FeatureList(auraColor: Color) {
    val features = listOf(
        R.string.feature_unlimited_persons,
        R.string.feature_unlimited_dates,
        R.string.feature_all_suggestions,
        R.string.feature_advanced_reminders,
        R.string.feature_no_ads
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        features.forEach { featureRes ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = auraColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(featureRes),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun SubscriptionOption(
    product: ProductDetails,
    isRecommended: Boolean,
    auraColor: Color,
    onClick: () -> Unit
) {
    val price = product.subscriptionOfferDetails?.firstOrNull()?.pricingPhases
        ?.pricingPhaseList?.firstOrNull()?.formattedPrice ?: ""

    GlassCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        border = if (isRecommended) 
            androidx.compose.foundation.BorderStroke(2.dp, auraColor) 
        else 
            androidx.compose.foundation.BorderStroke(1.dp, auraColor.copy(alpha = 0.2f))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (isRecommended) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    color = auraColor,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.best_value),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isRecommended) auraColor else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = price,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = auraColor
                )
            }
        }
    }
}

@Composable
private fun DemoSubscriptionOptions(auraColor: Color) {
    // Demo options when billing is not available
    listOf(
        Triple(stringResource(R.string.yearly_plan), "$29.99/year", true),
        Triple(stringResource(R.string.weekly_plan), "$2.99/week", false)
    ).forEach { (name, price, isRecommended) ->
        GlassCard(
            onClick = { },
            modifier = Modifier.fillMaxWidth(),
            border = if (isRecommended) 
                androidx.compose.foundation.BorderStroke(2.dp, auraColor) 
            else 
                androidx.compose.foundation.BorderStroke(1.dp, auraColor.copy(alpha = 0.2f))
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                if (isRecommended) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        color = auraColor,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.best_value),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isRecommended) auraColor else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = price,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = auraColor
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}
