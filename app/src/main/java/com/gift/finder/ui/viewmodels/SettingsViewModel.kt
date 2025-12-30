package com.gift.finder.ui.viewmodels

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gift.finder.data.local.AppDatabase
import com.gift.finder.data.manager.BillingConnectionState
import com.gift.finder.data.manager.BillingManager
import com.gift.finder.data.manager.PreferencesManager
import com.gift.finder.data.manager.PurchaseState
import com.gift.finder.domain.model.SubscriptionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Settings screen.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val billingManager: BillingManager,
    private val appDatabase: AppDatabase
) : ViewModel() {

    val subscriptionStatus: StateFlow<SubscriptionStatus> = preferencesManager.subscriptionStatus
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SubscriptionStatus())

    val themeMode = preferencesManager.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    val hapticsEnabled = preferencesManager.hapticsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val notificationsEnabled = preferencesManager.notificationsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val billingConnectionState: StateFlow<BillingConnectionState> = billingManager.billingConnectionState

    val purchaseState: StateFlow<PurchaseState> = billingManager.purchaseState

    val products = billingManager.products

    init {
        billingManager.initialize()
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            preferencesManager.setThemeMode(mode)
        }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setHapticsEnabled(enabled)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setNotificationsEnabled(enabled)
        }
    }

    fun restorePurchases() {
        billingManager.restorePurchases()
    }

    fun deleteAllData() {
        viewModelScope.launch {
            // Clear database
            appDatabase.clearAllTables()
            // Clear preferences
            preferencesManager.clearAllData()
        }
    }

    override fun onCleared() {
        super.onCleared()
        billingManager.endConnection()
    }
}
