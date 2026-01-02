package com.gift.finder.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.gift.finder.data.manager.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Main ViewModel for app-level state.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    val isOnboardingCompleted: Flow<Boolean> = preferencesManager.isOnboardingCompleted
    val subscriptionStatus = preferencesManager.subscriptionStatus
    val cosmicAura = preferencesManager.cosmicAura
    val themeMode = preferencesManager.themeMode
    val appLanguage = preferencesManager.appLanguage
    
    private val _pendingDeepLink = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    val pendingDeepLink: Flow<String?> = _pendingDeepLink.asStateFlow()

    fun setDeepLink(route: String?) {
        _pendingDeepLink.value = route
    }
    
    fun clearDeepLink() {
        _pendingDeepLink.value = null
    }
}
