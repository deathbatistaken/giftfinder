package com.gift.finder.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.gift.finder.domain.manager.HapticEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * Shared ViewModel to provide access to HapticEngine in Composables.
 */
@HiltViewModel
class HapticViewModel @Inject constructor(
    val hapticEngine: HapticEngine
) : ViewModel()
