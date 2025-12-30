package com.gift.finder.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import com.gift.finder.ui.screens.home.HomeScreen
import com.gift.finder.ui.screens.onboarding.OnboardingScreen
import com.gift.finder.ui.screens.paywall.PaywallScreen
import com.gift.finder.ui.screens.person.AddPersonScreen
import com.gift.finder.ui.screens.person.PersonDetailScreen
import com.gift.finder.ui.screens.settings.SettingsScreen
import com.gift.finder.ui.screens.suggestions.GiftRouletteScreen
import com.gift.finder.ui.screens.suggestions.GiftSuggestionsScreen
import com.gift.finder.ui.viewmodels.MainViewModel

/**
 * Main navigation host for the app.
 */
@Composable
fun GiftFinderNavHost(
    navController: NavHostController = rememberNavController(),
    mainViewModel: MainViewModel = hiltViewModel(),
    windowSizeClass: WindowSizeClass
) {
    val isOnboardingCompleted by mainViewModel.isOnboardingCompleted.collectAsState(initial = null)
    
    // Wait for initial state to load
    if (isOnboardingCompleted == null) return

    val startDestination = if (isOnboardingCompleted == true) {
        Screen.Home.route
    } else {
        Screen.Onboarding.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Onboarding
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = { showPaywall ->
                    if (showPaywall) {
                        navController.navigate(Screen.Paywall.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        // Paywall
        composable(Screen.Paywall.route) {
            PaywallScreen(
                onDismiss = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Paywall.route) { inclusive = true }
                    }
                },
                onPurchaseSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Paywall.route) { inclusive = true }
                    }
                }
            )
        }

        // Home
        composable(Screen.Home.route) {
            HomeScreen(
                windowSizeClass = windowSizeClass,
                onNavigateToAddPerson = {
                    navController.navigate(Screen.AddPerson.route)
                },
                onNavigateToPersonDetail = { personId ->
                    navController.navigate(Screen.PersonDetail.createRoute(personId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToPaywall = {
                    navController.navigate(Screen.Paywall.route)
                }
            )
        }

        // Add Person
        composable(Screen.AddPerson.route) {
            AddPersonScreen(
                onNavigateBack = { navController.popBackStack() },
                onPersonCreated = { personId ->
                    navController.popBackStack()
                    navController.navigate(Screen.PersonDetail.createRoute(personId))
                }
            )
        }

        // Person Detail
        composable(
            route = Screen.PersonDetail.route,
            arguments = listOf(
                navArgument(Screen.PersonDetail.ARG_PERSON_ID) {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val personId = backStackEntry.arguments?.getLong(Screen.PersonDetail.ARG_PERSON_ID) ?: return@composable
            PersonDetailScreen(
                personId = personId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSuggestions = {
                    navController.navigate(Screen.GiftSuggestions.createRoute(personId))
                },
                onNavigateToRoulette = {
                    navController.navigate(Screen.GiftRoulette.createRoute(personId))
                }
            )
        }

        // Gift Suggestions
        composable(
            route = Screen.GiftSuggestions.route,
            arguments = listOf(
                navArgument(Screen.GiftSuggestions.ARG_PERSON_ID) {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val personId = backStackEntry.arguments?.getLong(Screen.GiftSuggestions.ARG_PERSON_ID) ?: return@composable
            GiftSuggestionsScreen(
                personId = personId,
                windowSizeClass = windowSizeClass,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRoulette = {
                    navController.navigate(Screen.GiftRoulette.createRoute(personId))
                },
                onNavigateToPaywall = {
                    navController.navigate(Screen.Paywall.route)
                }
            )
        }

        // Gift Roulette
        composable(
            route = Screen.GiftRoulette.route,
            arguments = listOf(
                navArgument(Screen.GiftRoulette.ARG_PERSON_ID) {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val personId = backStackEntry.arguments?.getLong(Screen.GiftRoulette.ARG_PERSON_ID) ?: return@composable
            GiftRouletteScreen(
                personId = personId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Settings
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPaywall = {
                    navController.navigate(Screen.Paywall.route)
                },
                onNavigateToBudgetTracker = {
                    navController.navigate(Screen.BudgetTracker.route)
                }
            )
        }

        // Budget Tracker
        composable(Screen.BudgetTracker.route) {
            com.gift.finder.ui.screens.budget.BudgetTrackerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Edit Person
        composable(
            route = Screen.EditPerson.route,
            arguments = listOf(
                navArgument(Screen.EditPerson.ARG_PERSON_ID) {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val personId = backStackEntry.arguments?.getLong(Screen.EditPerson.ARG_PERSON_ID) ?: return@composable
            com.gift.finder.ui.screens.person.EditPersonScreen(
                personId = personId,
                onNavigateBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        // Calendar
        composable(Screen.Calendar.route) {
            com.gift.finder.ui.screens.calendar.CalendarScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPersonDetail = { personId ->
                    navController.navigate(Screen.PersonDetail.createRoute(personId))
                }
            )
        }

        // Search
        composable(Screen.Search.route) {
            com.gift.finder.ui.screens.search.SearchScreen(
                windowSizeClass = windowSizeClass,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPersonDetail = { personId ->
                    navController.navigate(Screen.PersonDetail.createRoute(personId))
                }
            )
        }
    }
}
