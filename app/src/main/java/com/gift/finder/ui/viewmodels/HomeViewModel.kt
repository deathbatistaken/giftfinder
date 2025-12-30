package com.gift.finder.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gift.finder.data.manager.PreferencesManager
import com.gift.finder.data.repository.PersonRepository
import com.gift.finder.data.repository.SpecialDateRepository
import com.gift.finder.domain.model.Person
import com.gift.finder.domain.model.PremiumFeatures
import com.gift.finder.domain.model.SpecialDate
import com.gift.finder.domain.model.SubscriptionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Home screen.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val personRepository: PersonRepository,
    private val specialDateRepository: SpecialDateRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val subscriptionStatus = preferencesManager.subscriptionStatus
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SubscriptionStatus())

    // Separate StateFlows for search and calendar screens
    val persons: StateFlow<List<Person>> = personRepository.getAllPersons()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val upcomingDates: StateFlow<List<SpecialDate>> = specialDateRepository.getUpcomingDates(30)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                personRepository.getAllPersons(),
                specialDateRepository.getUpcomingDates(30),
                personRepository.getPersonCount(),
                preferencesManager.subscriptionStatus
            ) { persons, upcomingDates, personCount, subscription ->
                val limits = PremiumFeatures.getLimits(subscription.isPremium)
                val canAddPerson = subscription.isPremium || personCount < limits.maxPersons
                
                HomeUiState.Success(
                    persons = persons,
                    upcomingDates = upcomingDates,
                    canAddPerson = canAddPerson,
                    isPremium = subscription.isPremium,
                    personCount = personCount
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun deletePerson(personId: Long) {
        viewModelScope.launch {
            personRepository.deletePerson(personId)
        }
    }
}

sealed class HomeUiState {
    data object Loading : HomeUiState()
    data class Success(
        val persons: List<Person>,
        val upcomingDates: List<SpecialDate>,
        val canAddPerson: Boolean,
        val isPremium: Boolean,
        val personCount: Int
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
