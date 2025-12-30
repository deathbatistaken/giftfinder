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
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val personId: Long = savedStateHandle[Screen.Wishlist.ARG_PERSON_ID] ?: 0

    private val _uiState = MutableStateFlow<WishlistUiState>(WishlistUiState.Loading)
    val uiState: StateFlow<WishlistUiState> = _uiState.asStateFlow()

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
                    val giftData = giftRepository.getGiftData()
                    val suggestions = savedGifts.mapNotNull { saved ->
                        giftData.categories.find { it.id == saved.categoryId }?.let { category ->
                            GiftSuggestion(
                                category = category,
                                matchScore = 100, // Explicitly saved
                                matchReasons = listOf("Saved to Wishlist"),
                                isPremiumLocked = false
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

    fun getShareText(): String {
        val state = _uiState.value
        return if (state is WishlistUiState.Success) {
            ExportManager.formatWishlist(state.person.name, state.savedGifts)
        } else {
            ""
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
