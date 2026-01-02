package com.gift.finder.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gift.finder.data.repository.GiftRepository
import com.gift.finder.data.repository.PersonRepository
import com.gift.finder.data.repository.SavedGiftRepository
import com.gift.finder.domain.model.GiftSuggestion
import com.gift.finder.domain.model.Person
import com.gift.finder.ui.navigation.Screen
import com.gift.finder.utils.ExportManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Wishlist screen.
 */
@HiltViewModel
class WishlistViewModel @Inject constructor(
    private val savedGiftRepository: SavedGiftRepository,
    private val personRepository: PersonRepository,
    private val giftRepository: GiftRepository,
    private val preferencesManager: com.gift.finder.data.manager.PreferencesManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val personId: Long = savedStateHandle[Screen.Wishlist.ARG_PERSON_ID] ?: 0

    private val _uiState = MutableStateFlow<WishlistUiState>(WishlistUiState.Loading)
    val uiState: StateFlow<WishlistUiState> = _uiState.asStateFlow()

    val appCurrency = preferencesManager.appCurrency
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), "USD")

    init {
        loadWishlist()
    }

    private fun loadWishlist() {
        viewModelScope.launch {
            combine(
                personRepository.getPersonById(personId),
                savedGiftRepository.getWishlistForPerson(personId)
            ) { person, savedGifts ->
                if (person == null) {
                    WishlistUiState.Error("Person not found")
                } else {
                    val categories = giftRepository.getGiftCategories()
                    val suggestions = savedGifts.mapNotNull { saved ->
                        categories.find { it.id == saved.categoryId }?.let { category ->
                            // Simulate Price Radar for Wishlist: Deterministic per day and category (30% chance)
                            val daySeed = System.currentTimeMillis() / (24 * 60 * 60 * 1000)
                            val random = java.util.Random(category.id.hashCode() + daySeed + 1)
                            val priceDrop = if (random.nextFloat() < 0.3f) (10..40).random(kotlin.random.Random(random.nextLong())) else null
                            
                            GiftSuggestion(
                                category = category,
                                matchScore = 100.0,
                                matchReasons = listOf("Saved to Wishlist"),
                                isPremiumLocked = false,
                                priceDropPercentage = priceDrop
                            )
                        }
                    }
                    WishlistUiState.Success(person, suggestions)
                }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun removeGift(categoryId: String) {
        viewModelScope.launch {
            savedGiftRepository.removeGift(personId, categoryId)
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
            savedGiftRepository.removeGift(personId, categoryId) // Remove from wishlist once bought
        }
    }
}


sealed class WishlistUiState {
    data object Loading : WishlistUiState()
    data class Success(
        val person: Person,
        val savedGifts: List<GiftSuggestion>
    ) : WishlistUiState()
    data class Error(val message: String) : WishlistUiState()
}
