package com.gift.finder.data.manager

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gift.finder.domain.model.SubscriptionStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "gift_finder_prefs")

/**
 * Manager for user preferences using DataStore.
 */
@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        // Onboarding
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        
        // Subscription
        private val KEY_IS_PREMIUM = booleanPreferencesKey("is_premium")
        private val KEY_PRODUCT_ID = stringPreferencesKey("product_id")
        private val KEY_PURCHASE_TOKEN = stringPreferencesKey("purchase_token")
        private val KEY_EXPIRY_TIME = longPreferencesKey("expiry_time")
        
        // App Settings
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode") // "system", "light", "dark"
        private val KEY_HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        private val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val KEY_COSMIC_AURA = stringPreferencesKey("cosmic_aura")
        private val KEY_REMINDER_OFFSETS = stringPreferencesKey("reminder_offsets") // e.g. "0,3,7"
    }

    // Onboarding
    val isOnboardingCompleted: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_COMPLETED] ?: false
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    // Subscription Status
    val subscriptionStatus: Flow<SubscriptionStatus> = dataStore.data.map { prefs ->
        SubscriptionStatus(
            isPremium = prefs[KEY_IS_PREMIUM] ?: false,
            productId = prefs[KEY_PRODUCT_ID],
            purchaseToken = prefs[KEY_PURCHASE_TOKEN],
            expiryTimeMillis = prefs[KEY_EXPIRY_TIME]
        )
    }

    suspend fun updateSubscriptionStatus(status: SubscriptionStatus) {
        dataStore.edit { prefs ->
            prefs[KEY_IS_PREMIUM] = status.isPremium
            status.productId?.let { prefs[KEY_PRODUCT_ID] = it }
            status.purchaseToken?.let { prefs[KEY_PURCHASE_TOKEN] = it }
            status.expiryTimeMillis?.let { prefs[KEY_EXPIRY_TIME] = it }
        }
    }

    suspend fun clearSubscription() {
        dataStore.edit { prefs ->
            prefs[KEY_IS_PREMIUM] = false
            prefs.remove(KEY_PRODUCT_ID)
            prefs.remove(KEY_PURCHASE_TOKEN)
            prefs.remove(KEY_EXPIRY_TIME)
        }
    }

    // Theme
    val themeMode: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_THEME_MODE] ?: "system"
    }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode
        }
    }

    // Haptics
    val hapticsEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_HAPTICS_ENABLED] ?: true
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_HAPTICS_ENABLED] = enabled
        }
    }

    // Notifications
    val notificationsEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_NOTIFICATIONS_ENABLED] ?: true
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_NOTIFICATIONS_ENABLED] = enabled
        }
    }

    // Cosmic Aura
    val cosmicAura: Flow<com.gift.finder.domain.model.CosmicAura> = dataStore.data.map { prefs ->
        val id = prefs[KEY_COSMIC_AURA] ?: "nebula"
        com.gift.finder.domain.model.CosmicAura.fromId(id)
    }

    suspend fun setCosmicAura(aura: com.gift.finder.domain.model.CosmicAura) {
        dataStore.edit { prefs ->
            prefs[KEY_COSMIC_AURA] = aura.id
        }
    }

    // Reminder Offsets
    val reminderOffsets: Flow<List<Int>> = dataStore.data.map { prefs ->
        val offsetString = prefs[KEY_REMINDER_OFFSETS] ?: "0,3,7"
        offsetString.split(",").filter { it.isNotBlank() }.map { it.toInt() }
    }

    suspend fun setReminderOffsets(offsets: List<Int>) {
        dataStore.edit { prefs ->
            prefs[KEY_REMINDER_OFFSETS] = offsets.joinToString(",")
        }
    }

    // Clear all data
    suspend fun clearAllData() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
