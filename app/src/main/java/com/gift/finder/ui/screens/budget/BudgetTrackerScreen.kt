package com.gift.finder.ui.screens.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gift.finder.R
import com.gift.finder.domain.model.GiftHistoryItem
import com.gift.finder.ui.theme.*
import com.gift.finder.ui.components.premium.AnimatedMeshBackground
import com.gift.finder.ui.components.premium.GlassCard
import com.gift.finder.ui.viewmodels.BudgetPeriod
import com.gift.finder.ui.viewmodels.BudgetTrackerUiState
import com.gift.finder.ui.viewmodels.BudgetTrackerViewModel
import com.gift.finder.ui.viewmodels.PersonSpending
import com.gift.finder.utils.toFormattedDate
import java.text.NumberFormat
import java.util.Locale

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
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
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

                    // Summary cards
                    item {
                        SummaryCards(
                            totalSpent = state.totalSpent,
                            giftCount = state.giftCount,
                            avgPerPerson = state.avgPerPerson
                        )
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
                            SpendingCard(spending = spending)
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
                            GiftHistoryCard(gift = gift)
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
    avgPerPerson: Double
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Total spent
        GlassCard(
            modifier = Modifier.weight(1f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("💰", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    currencyFormat.format(totalSpent),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
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
            modifier = Modifier.weight(1f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🎁", style = MaterialTheme.typography.headlineSmall)
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
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("📊", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    currencyFormat.format(avgPerPerson),
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
private fun SpendingCard(spending: PersonSpending) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

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
                currencyFormat.format(spending.amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = GiftPurple
            )
        }
    }
}

@Composable
private fun GiftHistoryCard(gift: GiftHistoryItem) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

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
                    currencyFormat.format(price),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = GiftGreen
                )
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
        Text("📊", style = MaterialTheme.typography.displayMedium)
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
