package com.gift.finder.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.gift.finder.data.manager.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
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
}
