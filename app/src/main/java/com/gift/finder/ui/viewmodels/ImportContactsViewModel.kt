package com.gift.finder.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gift.finder.data.repository.ContactRepository
import com.gift.finder.data.repository.PersonRepository
import com.gift.finder.domain.model.Contact
import com.gift.finder.domain.model.Person
import com.gift.finder.domain.model.RelationshipType
import com.gift.finder.domain.model.SpecialDate
import com.gift.finder.domain.model.SpecialDateType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ImportContactsUiState(
    val isLoading: Boolean = false,
    val contacts: List<Contact> = emptyList(),
    val selectedContactIds: Set<String> = emptySet(),
    val error: String? = null,
    val isImporting: Boolean = false,
    val importComplete: Boolean = false
)

@HiltViewModel
class ImportContactsViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val personRepository: PersonRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportContactsUiState())
    val uiState: StateFlow<ImportContactsUiState> = _uiState.asStateFlow()

    fun loadContacts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val contacts = contactRepository.getContacts().sortedBy { it.name }
                _uiState.update { it.copy(isLoading = false, contacts = contacts) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load contacts") }
            }
        }
    }

    fun toggleSelection(contactId: String) {
        _uiState.update { state ->
            val cur = state.selectedContactIds
            val newSelection = if (cur.contains(contactId)) cur - contactId else cur + contactId
            state.copy(selectedContactIds = newSelection)
        }
    }
    
    fun selectAll() {
        _uiState.update { state ->
             state.copy(selectedContactIds = state.contacts.map { it.id }.toSet())
        }
    }

    fun deselectAll() {
        _uiState.update { state ->
            state.copy(selectedContactIds = emptySet())
        }
    }
    
    fun importSelectedContacts() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.selectedContactIds.isEmpty()) return@launch
            
            _uiState.update { it.copy(isImporting = true) }
            
            val selectedContacts = state.contacts.filter { it.id in state.selectedContactIds }
            
            selectedContacts.forEach { contact ->
                val specialDates = mutableListOf<SpecialDate>()
                
                // Parse birthday if available (Assuming YYYY-MM-DD format as standard SQL date or --MM-DD)
                // Note: ContactsContract usually returns string, depends on device locale sometimes but standard is ISO 8601-like
                contact.birthdayDate?.let { dateStr ->
                    try {
                        // Handle format like "1990-05-20" or "--05-20"
                        if (dateStr.length >= 7) {
                             // Simple heuristic parsing
                             val parts = dateStr.replace("--", "").split("-")
                             // if --05-20 -> 05, 20
                             // if 1990-05-20 -> 1990, 05, 20
                             
                             val month: Int
                             val day: Int
                             var year: Int? = null
                             
                             if (parts.size == 3) {
                                 year = parts[0].toIntOrNull()
                                 month = parts[1].toInt()
                                 day = parts[2].toInt()
                             } else if (parts.size == 2) {
                                  // Assumed MM-DD or similar logic
                                  // Usually Android returns --MM-DD
                                  if (dateStr.startsWith("--")) {
                                       month = parts[0].toInt()
                                       day = parts[1].toInt()
                                  } else {
                                      // Fallback
                                      month = parts[0].toInt()
                                      day = parts[1].toInt()
                                  }
                             } else {
                                 month = 0
                                 day = 0
                             }
                             
                             if (month > 0 && day > 0) {
                                 specialDates.add(
                                     SpecialDate(
                                         id = UUID.randomUUID().toString(),
                                         personId = 0L, // Will be ignored/updated by repository insert logic
                                         title = "Birthday", 
                                         dateType = SpecialDateType.BIRTHDAY,
                                         month = month,
                                         dayOfMonth = day,
                                         year = year
                                     )
                                 )
                             }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            
                val person = Person(
                    name = contact.name,
                    relationshipType = RelationshipType.OTHER,
                    specialDates = specialDates
                )
                
                personRepository.insertPerson(person)
            }
            
            _uiState.update { it.copy(isImporting = false, importComplete = true) }
        }
    }
}
