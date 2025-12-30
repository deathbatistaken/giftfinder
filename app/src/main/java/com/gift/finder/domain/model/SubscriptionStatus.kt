package com.gift.finder.domain.model

/**
 * User subscription status.
 */
data class SubscriptionStatus(
    val isPremium: Boolean = false,
    val productId: String? = null,
    val purchaseToken: String? = null,
    val expiryTimeMillis: Long? = null
) {
    val isExpired: Boolean
        get() = expiryTimeMillis?.let { it < System.currentTimeMillis() } ?: !isPremium
}

/**
 * App usage limits for free tier.
 */
data class UsageLimits(
    val maxPersons: Int = 1,
    val maxSpecialDatesPerPerson: Int = 1,
    val maxVisibleSuggestions: Int = 2,
    val showAds: Boolean = true
)

/**
 * Premium features configuration.
 */
object PremiumFeatures {
    val FREE_LIMITS = UsageLimits()
    val PREMIUM_LIMITS = UsageLimits(
        maxPersons = Int.MAX_VALUE,
        maxSpecialDatesPerPerson = Int.MAX_VALUE,
        maxVisibleSuggestions = Int.MAX_VALUE,
        showAds = false
    )
    
    fun getLimits(isPremium: Boolean): UsageLimits {
        return if (isPremium) PREMIUM_LIMITS else FREE_LIMITS
    }
}
