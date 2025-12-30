package com.gift.finder.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gift.finder.data.manager.PreferencesManager
import com.gift.finder.data.repository.GiftRepository
import com.gift.finder.data.repository.PersonRepository
import com.gift.finder.domain.model.BudgetRange
import com.gift.finder.domain.model.GiftStyle
import com.gift.finder.domain.model.GiftSuggestion
import com.gift.finder.domain.model.Person
import com.gift.finder.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Gift Roulette screen.
 */
@HiltViewModel
class GiftRouletteViewModel @Inject constructor(
    private val giftRepository: GiftRepository,
    private val personRepository: PersonRepository,
    private val preferencesManager: PreferencesManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val personId: Long = savedStateHandle[Screen.GiftRoulette.ARG_PERSON_ID] ?: 0

    private val _uiState = MutableStateFlow<RouletteUiState>(RouletteUiState.Ready)
    val uiState: StateFlow<RouletteUiState> = _uiState.asStateFlow()

    private val _currentPerson = MutableStateFlow<Person?>(null)
    val currentPerson: StateFlow<Person?> = _currentPerson.asStateFlow()

    private val _selectedStyle = MutableStateFlow<GiftStyle?>(null)
    val selectedStyle: StateFlow<GiftStyle?> = _selectedStyle.asStateFlow()

    private val _selectedBudget = MutableStateFlow<BudgetRange?>(null)
    val selectedBudget: StateFlow<BudgetRange?> = _selectedBudget.asStateFlow()

    init {
        loadPerson()
    }

    private fun loadPerson() {
        viewModelScope.launch {
            personRepository.getPersonById(personId).collect { person ->
                _currentPerson.value = person
            }
        }
    }

    fun setStyle(style: GiftStyle?) {
        _selectedStyle.value = style
    }

    fun setBudget(budget: BudgetRange?) {
        _selectedBudget.value = budget
    }

    fun spin() {
        viewModelScope.launch {
            _uiState.value = RouletteUiState.Spinning

            // Simulate spinning delay
            kotlinx.coroutines.delay(2000)

            val person = _currentPerson.value
            if (person != null) {
                val result = giftRepository.getRandomGift(
                    person = person,
                    style = _selectedStyle.value,
                    budget = _selectedBudget.value
                )

                _uiState.value = if (result != null) {
                    RouletteUiState.Result(result)
                } else {
                    RouletteUiState.NoResult
                }
            } else {
                _uiState.value = RouletteUiState.NoResult
            }
        }
    }

    fun reset() {
        _uiState.value = RouletteUiState.Ready
    }
}

sealed class RouletteUiState {
    data object Ready : RouletteUiState()
    data object Spinning : RouletteUiState()
    data class Result(val suggestion: GiftSuggestion) : RouletteUiState()
    data object NoResult : RouletteUiState()
}
