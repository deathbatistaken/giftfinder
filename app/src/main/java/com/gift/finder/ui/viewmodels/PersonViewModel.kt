package com.gift.finder.ui.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gift.finder.data.manager.LocalNotificationManager
import com.gift.finder.data.manager.PreferencesManager
import com.gift.finder.data.repository.GiftRepository
import com.gift.finder.data.repository.PersonRepository
import com.gift.finder.domain.model.Archetype
import com.gift.finder.domain.model.GiftHistoryItem
import com.gift.finder.domain.model.Person
import com.gift.finder.domain.model.PremiumFeatures
import com.gift.finder.domain.model.RelationshipType
import com.gift.finder.domain.model.SpecialDate
import com.gift.finder.domain.model.SpecialDateType
import com.gift.finder.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for Person detail and add screens.
 */
@HiltViewModel
class PersonViewModel @Inject constructor(
    private val personRepository: PersonRepository,
    private val giftRepository: GiftRepository,
    private val notificationManager: LocalNotificationManager,
    private val preferencesManager: PreferencesManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val personId: Long? = savedStateHandle[Screen.PersonDetail.ARG_PERSON_ID]

    private val _uiState = MutableStateFlow<PersonUiState>(PersonUiState.Loading)
    val uiState: StateFlow<PersonUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(PersonFormState())
    val formState: StateFlow<PersonFormState> = _formState.asStateFlow()

    val archetypes: List<Archetype> = giftRepository.getArchetypes()

    val subscriptionStatus = preferencesManager.subscriptionStatus
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        if (personId != null && personId > 0) {
            loadPerson(personId)
        } else {
            _uiState.value = PersonUiState.New
        }
    }

    private fun loadPerson(id: Long) {
        viewModelScope.launch {
            personRepository.getPersonById(id).collect { person ->
                if (person != null) {
                    _uiState.value = PersonUiState.Loaded(person)
                    _formState.value = PersonFormState(
                        name = person.name,
                        relationshipType = person.relationshipType,
                        avatarEmoji = person.avatarEmoji,
                        interests = person.interests,
                        dislikes = person.dislikes,
                        notes = person.notes,
                        selectedArchetypeId = person.archetypeId
                    )
                } else {
                    _uiState.value = PersonUiState.Error("Person not found")
                }
            }
        }
    }

    fun updateName(name: String) {
        _formState.value = _formState.value.copy(name = name)
    }

    fun updateRelationshipType(type: RelationshipType) {
        _formState.value = _formState.value.copy(relationshipType = type)
    }

    fun updateAvatarEmoji(emoji: String) {
        _formState.value = _formState.value.copy(avatarEmoji = emoji)
    }

    fun addInterest(interest: String) {
        val current = _formState.value.interests.toMutableList()
        if (interest.isNotBlank() && interest !in current) {
            current.add(interest.trim())
            _formState.value = _formState.value.copy(interests = current)
        }
    }

    fun removeInterest(interest: String) {
        val current = _formState.value.interests.toMutableList()
        current.remove(interest)
        _formState.value = _formState.value.copy(interests = current)
    }

    fun addDislike(dislike: String) {
        val current = _formState.value.dislikes.toMutableList()
        if (dislike.isNotBlank() && dislike !in current) {
            current.add(dislike.trim())
            _formState.value = _formState.value.copy(dislikes = current)
        }
    }

    fun removeDislike(dislike: String) {
        val current = _formState.value.dislikes.toMutableList()
        current.remove(dislike)
        _formState.value = _formState.value.copy(dislikes = current)
    }

    fun updateNotes(notes: String) {
        _formState.value = _formState.value.copy(notes = notes)
    }

    fun selectArchetype(archetype: Archetype) {
        _formState.value = _formState.value.copy(
            selectedArchetypeId = archetype.id,
            interests = archetype.defaultInterests.toList()
        )
    }

    suspend fun savePerson(): Long {
        val form = _formState.value
        
        val person = when (val state = _uiState.value) {
            is PersonUiState.Loaded -> state.person.copy(
                name = form.name,
                relationshipType = form.relationshipType,
                avatarEmoji = form.avatarEmoji,
                interests = form.interests,
                dislikes = form.dislikes,
                notes = form.notes,
                archetypeId = form.selectedArchetypeId,
                updatedAt = System.currentTimeMillis()
            )
            else -> Person(
                name = form.name,
                relationshipType = form.relationshipType,
                avatarEmoji = form.avatarEmoji,
                interests = form.interests,
                dislikes = form.dislikes,
                notes = form.notes,
                archetypeId = form.selectedArchetypeId
            )
        }

        return if (person.id > 0) {
            personRepository.updatePerson(person)
            person.id
        } else {
            personRepository.insertPerson(person)
        }
    }

    suspend fun addSpecialDate(
        title: String,
        dateType: SpecialDateType,
        month: Int,
        dayOfMonth: Int
    ) {
        val currentPerson = (uiState.value as? PersonUiState.Loaded)?.person ?: return
        val subscription = subscriptionStatus.value
        val limits = PremiumFeatures.getLimits(subscription?.isPremium == true)

        // Check limits
        val currentCount = personRepository.getSpecialDateCount(currentPerson.id).first()
        if (!subscription?.isPremium!! && currentCount >= limits.maxSpecialDatesPerPerson) {
            return // Hit limit
        }

        val specialDate = SpecialDate(
            id = UUID.randomUUID().toString(),
            personId = currentPerson.id,
            title = title,
            dateType = dateType,
            month = month,
            dayOfMonth = dayOfMonth
        )

        personRepository.addSpecialDate(specialDate)
        notificationManager.scheduleNotificationsForDate(specialDate, currentPerson.name)
    }

    suspend fun deleteSpecialDate(specialDate: SpecialDate) {
        personRepository.deleteSpecialDate(specialDate.id, specialDate.personId)
        notificationManager.cancelNotificationsForDate(specialDate.id)
    }

    suspend fun markGiftAsPurchased(
        categoryId: String,
        categoryTitle: String,
        price: Double?,
        occasion: String
    ) {
        val currentPerson = (uiState.value as? PersonUiState.Loaded)?.person ?: return
        
        val historyItem = GiftHistoryItem(
            personId = currentPerson.id,
            categoryId = categoryId,
            categoryTitle = categoryTitle,
            price = price,
            occasion = occasion
        )
        
        personRepository.addGiftToHistory(historyItem)
    }
}

sealed class PersonUiState {
    data object Loading : PersonUiState()
    data object New : PersonUiState()
    data class Loaded(val person: Person) : PersonUiState()
    data class Error(val message: String) : PersonUiState()
}

data class PersonFormState(
    val name: String = "",
    val relationshipType: RelationshipType = RelationshipType.FRIEND,
    val avatarEmoji: String = "🎁",
    val interests: List<String> = emptyList(),
    val dislikes: List<String> = emptyList(),
    val notes: String = "",
    val selectedArchetypeId: String? = null
) {
    val isValid: Boolean
        get() = name.isNotBlank()
}
