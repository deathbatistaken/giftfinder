package com.gift.finder.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gift.finder.data.manager.PreferencesManager
import com.gift.finder.data.repository.GiftRepository
import com.gift.finder.data.repository.PersonRepository
import com.gift.finder.data.repository.SavedGiftRepository
import com.gift.finder.domain.model.BudgetRange
import com.gift.finder.domain.model.GiftStyle
import com.gift.finder.domain.model.GiftSuggestion
import com.gift.finder.domain.model.Person
import com.gift.finder.domain.model.PremiumFeatures
import com.gift.finder.domain.model.RejectionReason
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
 * ViewModel for Gift Suggestions screen.
 */
@HiltViewModel
class GiftSuggestionsViewModel @Inject constructor(
    private val giftRepository: GiftRepository,
    private val personRepository: PersonRepository,
    private val savedGiftRepository: SavedGiftRepository,
    private val preferencesManager: PreferencesManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val personId: Long = savedStateHandle[Screen.GiftSuggestions.ARG_PERSON_ID] ?: 0

    private val _uiState = MutableStateFlow<SuggestionsUiState>(SuggestionsUiState.Loading)
    val uiState: StateFlow<SuggestionsUiState> = _uiState.asStateFlow()

    private val _selectedStyle = MutableStateFlow<GiftStyle?>(null)
    val selectedStyle: StateFlow<GiftStyle?> = _selectedStyle.asStateFlow()

    private val _selectedBudget = MutableStateFlow<BudgetRange?>(null)
    val selectedBudget: StateFlow<BudgetRange?> = _selectedBudget.asStateFlow()

    val subscriptionStatus = preferencesManager.subscriptionStatus
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val appCurrency: StateFlow<String> = preferencesManager.appCurrency
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "USD")

    private var currentPerson: Person? = null

    init {
        loadPerson()
    }

    private fun loadPerson() {
        viewModelScope.launch {
            personRepository.getPersonById(personId).collect { person ->
                if (person != null) {
                    currentPerson = person
                    loadSuggestions()
                } else {
                    _uiState.value = SuggestionsUiState.Error("Person not found")
                }
            }
        }
    }

    fun setStyle(style: GiftStyle?) {
        _selectedStyle.value = style
        loadSuggestions()
    }

    fun setBudget(budget: BudgetRange?) {
        _selectedBudget.value = budget
        loadSuggestions()
    }

    private fun loadSuggestions() {
        viewModelScope.launch {
            val person = currentPerson ?: return@launch
            val subscription = preferencesManager.subscriptionStatus.first()
            val isPremium = subscription.isPremium
            val creativityLevel = preferencesManager.personaCreativity.first()
            val limits = PremiumFeatures.getLimits(isPremium)

            val suggestions = giftRepository.getSuggestions(
                person = person,
                style = _selectedStyle.value,
                budget = _selectedBudget.value,
                creativityLevel = creativityLevel
            )

            // Apply free tier limits
            val processedSuggestions = suggestions.mapIndexed { index, suggestion ->
                if (!isPremium && index >= limits.maxVisibleSuggestions) {
                    suggestion.copy(isPremiumLocked = true)
                } else {
                    suggestion
                }
            }

            _uiState.value = SuggestionsUiState.Success(
                person = person,
                suggestions = processedSuggestions,
                isPremium = isPremium
            )
        }
    }

    fun rejectSuggestion(categoryId: String, reason: RejectionReason) {
        viewModelScope.launch {
            giftRepository.rejectSuggestion(personId, categoryId, reason)
            loadSuggestions() // Reload to remove rejected
        }
    }

    fun saveToWishlist(categoryId: String) {
        viewModelScope.launch {
            savedGiftRepository.saveGift(personId, categoryId)
            // Optional: Show success snackbar or haptic in UI
        }
    }

    fun purchaseGift(categoryId: String, categoryTitle: String, price: Double?, occasion: String) {
        viewModelScope.launch {
            val historyItem = com.gift.finder.domain.model.GiftHistoryItem(
                personId = personId,
                categoryId = categoryId,
                categoryTitle = categoryTitle,
                price = price,
                occasion = occasion
            )
            personRepository.addGiftToHistory(historyItem)
            loadSuggestions() // Reload to exclude purchased
        }
    }

    suspend fun getRandomGift(): GiftSuggestion? {
        val person = currentPerson ?: return null
        return giftRepository.getRandomGift(
            person = person,
            style = _selectedStyle.value,
            budget = _selectedBudget.value
        )
    }
}

sealed class SuggestionsUiState {
    data object Loading : SuggestionsUiState()
    data class Success(
        val person: Person,
        val suggestions: List<GiftSuggestion>,
        val isPremium: Boolean
    ) : SuggestionsUiState()
    data class Error(val message: String) : SuggestionsUiState()
}
