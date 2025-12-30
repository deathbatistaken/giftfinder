package com.gift.finder.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gift.finder.data.repository.GiftRepository
import com.gift.finder.data.repository.PersonRepository
import com.gift.finder.domain.model.GiftCategory
import com.gift.finder.domain.model.Person
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for search functionality.
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val personRepository: PersonRepository,
    private val giftRepository: GiftRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag = _selectedTag.asStateFlow()

    val searchUiState: StateFlow<SearchUiState> = combine(
        _searchQuery,
        _selectedTag,
        personRepository.getAllPersons()
    ) { query, tag, persons ->
        if (query.isBlank() && tag == null) {
            SearchUiState.Idle
        } else {
            val filter = query.ifBlank { tag ?: "" }
            
            val filteredPersons = persons.filter { person ->
                person.name.contains(filter, ignoreCase = true) ||
                        person.interests.any { it.contains(filter, ignoreCase = true) }
            }
            
            val filteredCategories = giftRepository.getGiftCategories().filter { category ->
                category.title.contains(filter, ignoreCase = true) ||
                        category.description.contains(filter, ignoreCase = true) ||
                        category.interestTags.any { it.contains(filter, ignoreCase = true) }
            }
            
            if (filteredPersons.isEmpty() && filteredCategories.isEmpty()) {
                SearchUiState.NoResults
            } else {
                SearchUiState.Success(filteredPersons, filteredCategories)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchUiState.Idle)

    fun onQueryChange(query: String) {
        _searchQuery.value = query
        if (query.isNotBlank()) _selectedTag.value = null
    }

    fun onTagSelect(tag: String?) {
        _selectedTag.value = tag
        if (tag != null) _searchQuery.value = ""
    }
}

sealed class SearchUiState {
    data object Idle : SearchUiState()
    data object NoResults : SearchUiState()
    data class Success(
        val persons: List<Person>,
        val categories: List<GiftCategory>
    ) : SearchUiState()
}
