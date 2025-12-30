package com.gift.finder.ui.viewmodels

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gift.finder.data.manager.BillingManager
import com.gift.finder.data.manager.BillingConnectionState
import com.gift.finder.data.manager.PreferencesManager
import com.gift.finder.data.manager.PurchaseState
import com.gift.finder.domain.model.SubscriptionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * ViewModel for Paywall screen.
 */
@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val billingManager: BillingManager,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val billingConnectionState: StateFlow<BillingConnectionState> = billingManager.billingConnectionState

    val purchaseState: StateFlow<PurchaseState> = billingManager.purchaseState

    val products = billingManager.products

    val subscriptionStatus: StateFlow<SubscriptionStatus> = preferencesManager.subscriptionStatus
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SubscriptionStatus())

    init {
        billingManager.initialize()
    }

    fun purchaseWeekly(activity: Activity) {
        billingManager.launchPurchase(activity, BillingManager.PRODUCT_WEEKLY)
    }

    fun purchaseYearly(activity: Activity) {
        billingManager.launchPurchase(activity, BillingManager.PRODUCT_YEARLY)
    }

    fun restorePurchases() {
        billingManager.restorePurchases()
    }

    override fun onCleared() {
        super.onCleared()
        billingManager.endConnection()
    }
}
