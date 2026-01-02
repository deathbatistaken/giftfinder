package com.gift.finder.ui.navigation

/**
 * Screen routes for navigation.
 */
sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Paywall : Screen("paywall")
    data object Home : Screen("home")
    data object PersonList : Screen("person_list")
    data object PersonDetail : Screen("person_detail/{personId}") {
        fun createRoute(personId: Long) = "person_detail/$personId"
        const val ARG_PERSON_ID = "personId"
    }
    data object AddPerson : Screen("add_person")
    data object GiftSuggestions : Screen("gift_suggestions/{personId}") {
        fun createRoute(personId: Long) = "gift_suggestions/$personId"
        const val ARG_PERSON_ID = "personId"
    }
    data object GiftRoulette : Screen("gift_roulette/{personId}") {
        fun createRoute(personId: Long) = "gift_roulette/$personId"
        const val ARG_PERSON_ID = "personId"
    }
    data object Settings : Screen("settings")
    data object BudgetTracker : Screen("budget_tracker")
    data object EditPerson : Screen("edit_person/{personId}") {
        fun createRoute(personId: Long) = "edit_person/$personId"
        const val ARG_PERSON_ID = "personId"
    }
    data object Calendar : Screen("calendar")
    data object Search : Screen("search")
    data object Wishlist : Screen("wishlist/{personId}") {
        fun createRoute(personId: Long) = "wishlist/$personId"
        const val ARG_PERSON_ID = "personId"
    }
    data object ImportContacts : Screen("import_contacts")
}
