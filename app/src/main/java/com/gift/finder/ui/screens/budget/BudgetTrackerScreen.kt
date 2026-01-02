package com.gift.finder.ui.screens.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gift.finder.R
import com.gift.finder.ui.theme.*
import com.gift.finder.ui.viewmodels.BudgetTrackerViewModel
import com.gift.finder.ui.viewmodels.BudgetTrackerUiState
import com.gift.finder.ui.viewmodels.BudgetPeriod
import com.gift.finder.ui.viewmodels.PersonSpending
import com.gift.finder.domain.model.GiftHistoryItem
import com.gift.finder.ui.components.premium.CinematicRadialChart
import com.gift.finder.ui.components.premium.AnimatedSpendingBar
import com.gift.finder.ui.components.premium.AnimatedMeshBackground
import com.gift.finder.ui.components.premium.GlassCard
import com.gift.finder.ui.components.premium.SkeletonCard
import com.gift.finder.ui.components.premium.SkeletonText
import com.gift.finder.ui.components.premium.ProgressRing
import com.gift.finder.utils.toFormattedDate
import com.gift.finder.util.CurrencyUtils

/**
 * Budget Tracker screen for spending analytics.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetTrackerScreen(
    viewModel: BudgetTrackerViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val appCurrency by viewModel.appCurrency.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.budget_tracker),
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
            
            when (val state = uiState) {
            is BudgetTrackerUiState.Loading -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Period chips skeleton
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(3) { SkeletonText(width = 80.dp, height = 36.dp) }
                    }
                    // Chart skeleton
                    SkeletonCard(height = 200.dp)
                    // Stats skeleton
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SkeletonCard(modifier = Modifier.weight(1f), height = 100.dp)
                        SkeletonCard(modifier = Modifier.weight(1f), height = 100.dp)
                    }
                    // List skeleton
                    SkeletonCard(height = 80.dp)
                    SkeletonCard(height = 80.dp)
                }
            }

            is BudgetTrackerUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Period selector
                    item {
                        PeriodSelector(
                            selectedPeriod = selectedPeriod,
                            onPeriodSelected = viewModel::setPeriod
                        )
                    }

                    // Budget Progress Card
                    item {
                        BudgetProgressCard(
                            totalSpent = state.totalSpent,
                            limit = state.monthlyBudgetLimit,
                            appCurrency = appCurrency,
                            onLimitChange = viewModel::setMonthlyBudget
                        )
                    }

                    // Summary cards
                    item {
                        SummaryCards(
                            totalSpent = state.totalSpent,
                            giftCount = state.giftCount,
                            avgPerPerson = state.avgPerPerson,
                            appCurrency = appCurrency
                        )
                    }

                    // Insights Banner
                    state.insights?.let { insight ->
                        item {
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                cornerRadius = 16.dp,
                                border = androidx.compose.foundation.BorderStroke(1.dp, GiftPurple.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        modifier = Modifier.size(40.dp),
                                        shape = CircleShape,
                                        color = GiftPurple.copy(alpha = 0.15f)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Lightbulb,
                                                contentDescription = stringResource(R.string.cd_gift_icon),
                                                tint = GiftPurple,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        insight,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    // Top spending
                    if (state.topSpending.isNotEmpty()) {
                        item {
                            Text(
                                stringResource(R.string.top_spending),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        items(state.topSpending) { spending ->
                            SpendingCard(spending = spending, appCurrency = appCurrency)
                        }
                    }

                    // Analytics View
                    if (state.totalSpent > 0) {
                        item {
                            Text(
                                stringResource(R.string.visual_analysis),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        item {
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CinematicRadialChart(
                                        portions = state.portions,
                                        centerText = CurrencyUtils.formatPrice(state.totalSpent, appCurrency),
                                        modifier = Modifier.size(200.dp).padding(16.dp),
                                        strokeWidth = 20.dp
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    state.topSpending.take(3).forEach { spending ->
                                        val portion = state.portions.find { it.label == spending.person.name }
                                        AnimatedSpendingBar(
                                            label = spending.person.name,
                                            value = (spending.amount / state.totalSpent).toFloat(),
                                            amountText = CurrencyUtils.formatPrice(spending.amount, appCurrency),
                                            color = portion?.color ?: GiftPurple,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Relationship Breakdown
                        if (state.relationshipPortions.isNotEmpty()) {
                            item {
                                Text(
                                    stringResource(R.string.relationship_breakdown),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            item {
                                GlassCard(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        state.relationshipPortions.forEach { portion ->
                                            AnimatedSpendingBar(
                                                label = portion.label,
                                                value = portion.percentage,
                                                amountText = "${(portion.percentage * 100).toInt()}%",
                                                color = portion.color,
                                                modifier = Modifier.padding(vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Recent gifts
                    if (state.recentGifts.isNotEmpty()) {
                        item {
                            Text(
                                stringResource(R.string.recent_gifts),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        items(state.recentGifts) { gift ->
                            GiftHistoryCard(gift = gift, appCurrency = appCurrency)
                        }
                    }

                    // Empty state
                    if (state.giftCount == 0) {
                        item {
                            EmptyBudgetState()
                        }
                    }
                }
            }
            }
        }
    }
}

@Composable
private fun PeriodSelector(
    selectedPeriod: BudgetPeriod,
    onPeriodSelected: (BudgetPeriod) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BudgetPeriod.entries.forEach { period ->
            FilterChip(
                selected = selectedPeriod == period,
                onClick = { onPeriodSelected(period) },
                label = {
                    Text(
                        when (period) {
                            BudgetPeriod.THIS_MONTH -> stringResource(R.string.this_month)
                            BudgetPeriod.THIS_YEAR -> stringResource(R.string.this_year)
                            BudgetPeriod.ALL_TIME -> stringResource(R.string.all_time)
                        }
                    )
                }
            )
        }
    }
}

@Composable
private fun SummaryCards(
    totalSpent: Double,
    giftCount: Int,
    avgPerPerson: Double,
    appCurrency: String
) {
    val aura = LocalCosmicAura.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Total spent
        GlassCard(
            modifier = Modifier.weight(1f),
            border = androidx.compose.foundation.BorderStroke(1.dp, aura.primaryColor.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.emoji_money), style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    CurrencyUtils.formatPrice(totalSpent, appCurrency),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = aura.primaryColor
                )
                Text(
                    stringResource(R.string.total_spent),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Gift count
        GlassCard(
            modifier = Modifier.weight(1f),
            border = androidx.compose.foundation.BorderStroke(1.dp, GiftGreen.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Icon(
                    imageVector = Icons.Default.CardGiftcard,
                    contentDescription = stringResource(R.string.cd_gift_icon),
                    tint = GiftGreen,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "$giftCount",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = GiftGreen
                )
                Text(
                    stringResource(R.string.gifts_given),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Average per person
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(1.dp, GiftBlue.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.emoji_chart), style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    CurrencyUtils.formatPrice(avgPerPerson, appCurrency),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GiftBlue
                )
                Text(
                    stringResource(R.string.avg_per_person),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SpendingCard(spending: PersonSpending, appCurrency: String) {

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(spending.person.avatarEmoji, style = MaterialTheme.typography.titleLarge)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(spending.person.name, fontWeight = FontWeight.Medium)
                Text(
                    "${spending.person.giftHistory.size} " + stringResource(R.string.gifts),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                CurrencyUtils.formatPrice(spending.amount, appCurrency),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = GiftPurple
            )
        }
    }
}

@Composable
private fun GiftHistoryCard(gift: GiftHistoryItem, appCurrency: String) {

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(gift.categoryTitle, fontWeight = FontWeight.Medium)
                Text(
                    gift.purchaseDate.toFormattedDate(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            gift.price?.let { price ->
                Text(
                    CurrencyUtils.formatPrice(price, appCurrency),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = GiftGreen
                )
            }
        }
    }
}

@Composable
private fun BudgetProgressCard(
    totalSpent: Double,
    limit: Double,
    appCurrency: String,
    onLimitChange: (Double) -> Unit
) {
    val aura = LocalCosmicAura.current
    val progress = (totalSpent / limit).toFloat().coerceIn(0f, 1f)
    val remaining = limit - totalSpent
    val isExceeded = totalSpent > limit

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            if (isExceeded) GiftRed.copy(alpha = 0.5f) else aura.primaryColor.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.budget_progress),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    if (isExceeded) 
                        stringResource(R.string.budget_exceeded, CurrencyUtils.formatPrice(Math.abs(remaining), appCurrency))
                    else
                        stringResource(R.string.budget_remaining, CurrencyUtils.formatPrice(remaining, appCurrency)),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isExceeded) GiftRed else GiftGreen
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = if (isExceeded) GiftRed else aura.primaryColor,
                trackColor = aura.primaryColor.copy(alpha = 0.1f)
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                stringResource(R.string.set_monthly_budget),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Slider(
                value = limit.toFloat(),
                onValueChange = { onLimitChange(it.toDouble()) },
                valueRange = 100f..5000f,
                steps = 49,
                modifier = Modifier.fillMaxWidth()
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(CurrencyUtils.formatPrice(100.0, appCurrency), style = MaterialTheme.typography.labelSmall)
                Text(CurrencyUtils.formatPrice(limit, appCurrency), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Text(CurrencyUtils.formatPrice(5000.0, appCurrency), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun EmptyBudgetState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.emoji_chart), style = MaterialTheme.typography.displayMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(R.string.no_spending_data),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            stringResource(R.string.mark_gifts_purchased),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
