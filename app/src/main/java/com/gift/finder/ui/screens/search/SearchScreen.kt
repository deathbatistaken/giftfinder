package com.gift.finder.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.hilt.navigation.compose.hiltViewModel
import com.gift.finder.R
import com.gift.finder.domain.model.Person
import com.gift.finder.ui.theme.*
import com.gift.finder.ui.viewmodels.SearchViewModel
import com.gift.finder.ui.viewmodels.SearchUiState
import com.gift.finder.ui.components.premium.AnimatedMeshBackground
import com.gift.finder.ui.components.premium.GlassCard
import com.gift.finder.ui.components.premium.SkeletonPersonCard
import com.gift.finder.ui.components.premium.BouncingDotsIndicator
import com.gift.finder.domain.model.GiftCategory
import androidx.compose.animation.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.style.TextOverflow
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext

/**
 * Search screen for finding people and gifts.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    windowSizeClass: WindowSizeClass,
    viewModel: SearchViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToPersonDetail: (Long) -> Unit
) {
    val uiState by viewModel.searchUiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedTag by viewModel.selectedTag.collectAsState()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    val quickTags = listOf(
        stringResource(R.string.tag_tech),
        stringResource(R.string.tag_art),
        stringResource(R.string.tag_outdoor),
        stringResource(R.string.tag_books),
        stringResource(R.string.tag_gaming),
        stringResource(R.string.tag_wellness)
    )

    val aura = LocalCosmicAura.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onQueryChange(it) },
                        placeholder = { Text(stringResource(R.string.search_placeholder)) },
                        modifier = Modifier.fillMaxWidth().padding(end = 8.dp),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = aura.primaryColor) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onQueryChange("") }) {
                                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = aura.primaryColor,
                            unfocusedBorderColor = Color.Transparent,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedMeshBackground()
            
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                // Quick Tags
                LazyRow(
                    modifier = Modifier.padding(vertical = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(quickTags) { tag ->
                        FilterChip(
                            selected = selectedTag == tag,
                            onClick = { 
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.onTagSelect(if (selectedTag == tag) null else tag) 
                            },
                            label = { Text(tag) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = aura.primaryColor.copy(alpha = 0.2f),
                                selectedLabelColor = aura.primaryColor
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedTag == tag,
                                borderColor = aura.primaryColor.copy(alpha = 0.3f),
                                selectedBorderColor = aura.primaryColor
                            )
                        )
                    }
                }

                AnimatedContent(
                    targetState = uiState,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "SearchAnimations"
                ) { state ->
                    when (state) {
                        is SearchUiState.Idle -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(stringResource(R.string.emoji_search), style = MaterialTheme.typography.displayMedium)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(stringResource(R.string.search_hint), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        is SearchUiState.NoResults -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(stringResource(R.string.emoji_empty), style = MaterialTheme.typography.displayMedium)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(stringResource(R.string.no_results))
                                }
                            }
                        }
                        is SearchUiState.Success -> {
                            val columns = when (windowSizeClass.widthSizeClass) {
                                WindowWidthSizeClass.Compact -> 1
                                WindowWidthSizeClass.Medium -> 2
                                WindowWidthSizeClass.Expanded -> 3
                                else -> 1
                            }
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(columns),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (state.persons.isNotEmpty()) {
                                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(columns) }) {
                                        Text(stringResource(R.string.people), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = aura.primaryColor)
                                    }
                                    items(state.persons) { person ->
                                        SearchResultCard(
                                            person = person,
                                            onClick = { 
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                onNavigateToPersonDetail(person.id) 
                                            }
                                        )
                                    }
                                }
                                if (state.categories.isNotEmpty()) {
                                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(columns) }) {
                                        Text(stringResource(R.string.gift_ideas).uppercase(), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = aura.primaryColor, modifier = Modifier.padding(top = 16.dp))
                                    }
                                    items(state.categories) { category ->
                                        GiftCategoryResultCard(
                                            category = category,
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(category.getStoreUrl()))
                                                context.startActivity(intent)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GiftCategoryResultCard(
    category: GiftCategory,
    onClick: () -> Unit
) {
    val aura = LocalCosmicAura.current
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        border = androidx.compose.foundation.BorderStroke(1.dp, aura.primaryColor.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = aura.primaryColor.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, aura.primaryColor.copy(alpha = 0.3f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(category.emoji, style = MaterialTheme.typography.titleLarge)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = category.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = category.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SearchResultCard(
    person: Person,
    onClick: () -> Unit
) {
    val aura = LocalCosmicAura.current
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        border = androidx.compose.foundation.BorderStroke(1.dp, aura.primaryColor.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = aura.primaryColor.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, aura.primaryColor.copy(alpha = 0.3f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(person.avatarEmoji, style = MaterialTheme.typography.titleLarge)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = person.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (person.interests.isNotEmpty()) {
                    Text(
                        text = person.interests.take(3).joinToString(", "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
