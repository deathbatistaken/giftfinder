package com.gift.finder.data.manager

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.gift.finder.domain.model.SubscriptionStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for Google Play Billing.
 */
@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: PreferencesManager
) : PurchasesUpdatedListener {

    companion object {
        const val PRODUCT_WEEKLY = "giftfinder_weekly"
        const val PRODUCT_YEARLY = "giftfinder_yearly"
    }

    private var billingClient: BillingClient? = null
    
    private val _billingConnectionState = MutableStateFlow<BillingConnectionState>(BillingConnectionState.Disconnected)
    val billingConnectionState: StateFlow<BillingConnectionState> = _billingConnectionState.asStateFlow()

    private val _products = MutableStateFlow<List<ProductDetails>>(emptyList())
    val products: StateFlow<List<ProductDetails>> = _products.asStateFlow()

    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()

    /**
     * Initialize billing client and connect.
     */
    fun initialize() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        
        connect()
    }

    private fun connect() {
        _billingConnectionState.value = BillingConnectionState.Connecting
        
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    _billingConnectionState.value = BillingConnectionState.Connected
                    queryProducts()
                    queryExistingPurchases()
                } else {
                    _billingConnectionState.value = BillingConnectionState.Error(
                        billingResult.debugMessage
                    )
                }
            }

            override fun onBillingServiceDisconnected() {
                _billingConnectionState.value = BillingConnectionState.Disconnected
            }
        })
    }

    private fun queryProducts() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_WEEKLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_YEARLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient?.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                _products.value = productDetailsList
            }
        }
    }

    private fun queryExistingPurchases() {
        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                handlePurchases(purchases)
            }
        }
    }

    /**
     * Launch purchase flow.
     */
    fun launchPurchase(activity: Activity, productDetails: ProductDetails) {
        val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
            ?: return

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        _purchaseState.value = PurchaseState.Loading
        billingClient?.launchBillingFlow(activity, billingFlowParams)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.let { handlePurchases(it) }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _purchaseState.value = PurchaseState.Cancelled
            }
            else -> {
                _purchaseState.value = PurchaseState.Error(billingResult.debugMessage)
            }
        }
    }

    private fun handlePurchases(purchases: List<Purchase>) {
        purchases.forEach { purchase ->
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                // Acknowledge purchase if not already
                if (!purchase.isAcknowledged) {
                    acknowledgePurchase(purchase)
                }
                
                // Update subscription status
                kotlinx.coroutines.runBlocking {
                    preferencesManager.updateSubscriptionStatus(
                        SubscriptionStatus(
                            isPremium = true,
                            productId = purchase.products.firstOrNull(),
                            purchaseToken = purchase.purchaseToken,
                            expiryTimeMillis = null // Would need to get from backend
                        )
                    )
                }
                _purchaseState.value = PurchaseState.Success
            }
        }
        
        if (purchases.isEmpty()) {
            // No active subscriptions
            kotlinx.coroutines.runBlocking {
                preferencesManager.clearSubscription()
            }
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        
        billingClient?.acknowledgePurchase(params) { billingResult ->
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                // Handle acknowledgment error
            }
        }
    }

    /**
     * Restore purchases.
     */
    fun restorePurchases() {
        queryExistingPurchases()
    }

    /**
     * End billing connection.
     */
    fun endConnection() {
        billingClient?.endConnection()
        billingClient = null
    }
}

sealed class BillingConnectionState {
    data object Disconnected : BillingConnectionState()
    data object Connecting : BillingConnectionState()
    data object Connected : BillingConnectionState()
    data class Error(val message: String) : BillingConnectionState()
}

sealed class PurchaseState {
    data object Idle : PurchaseState()
    data object Loading : PurchaseState()
    data object Success : PurchaseState()
    data object Cancelled : PurchaseState()
    data class Error(val message: String) : PurchaseState()
}
