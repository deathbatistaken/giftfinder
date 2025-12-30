package com.gift.finder.ui.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gift.finder.R
import com.gift.finder.ui.viewmodels.OnboardingViewModel
import com.airbnb.lottie.compose.*
import com.gift.finder.ui.components.premium.AnimatedMeshBackground
import com.gift.finder.ui.theme.*
import kotlinx.coroutines.launch

/**
 * Onboarding screen with pager layout.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onComplete: (showPaywall: Boolean) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { viewModel.totalPages })
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        viewModel.setPage(pagerState.currentPage)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedMeshBackground()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
        // Skip button
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            AnimatedVisibility(
                visible = pagerState.currentPage < viewModel.totalPages - 1,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                TextButton(onClick = {
                    viewModel.completeOnboarding()
                    onComplete(true)
                }) {
                    Text(stringResource(R.string.skip))
                }
            }
        }

        // Pager content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->
            OnboardingPage(page = page)
        }

        // Page indicators
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(viewModel.totalPages) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (index == pagerState.currentPage) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == pagerState.currentPage)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                )
            }
        }

        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AnimatedVisibility(visible = pagerState.currentPage > 0) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    }
                ) {
                    Text(stringResource(R.string.back))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (pagerState.currentPage == viewModel.totalPages - 1) {
                        viewModel.completeOnboarding()
                        onComplete(true) // Show paywall after onboarding
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                }
            ) {
                Text(
                    if (pagerState.currentPage == viewModel.totalPages - 1)
                        stringResource(R.string.get_started)
                    else
                        stringResource(R.string.next)
                )
            }
        }
        }
    }
}

@Composable
private fun OnboardingPage(page: Int) {
    val (emoji, titleRes, descriptionRes) = when (page) {
        0 -> Triple("🎁", R.string.onboarding_title_1, R.string.onboarding_desc_1)
        1 -> Triple("📝", R.string.onboarding_title_2, R.string.onboarding_desc_2)
        2 -> Triple("🔔", R.string.onboarding_title_3, R.string.onboarding_desc_3)
        3 -> Triple("✨", R.string.onboarding_title_4, R.string.onboarding_desc_4)
        else -> Triple("🎁", R.string.onboarding_title_1, R.string.onboarding_desc_1)
    }

    val lottieRes = when (page) {
        0 -> R.raw.anim_gift
        1 -> R.raw.anim_list
        2 -> R.raw.anim_bell
        3 -> R.raw.anim_sparkle
        else -> R.raw.anim_gift
    }

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieRes))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier
                .size(240.dp)
                .padding(bottom = 32.dp)
        )

        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = stringResource(descriptionRes),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
