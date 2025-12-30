package com.gift.finder.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gift.finder.data.local.dao.GiftHistoryDao
import com.gift.finder.data.repository.PersonRepository
import com.gift.finder.domain.model.GiftHistoryItem
import com.gift.finder.domain.model.Person
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * ViewModel for Budget Tracker screen.
 */
@HiltViewModel
class BudgetTrackerViewModel @Inject constructor(
    private val personRepository: PersonRepository,
    private val giftHistoryDao: GiftHistoryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow<BudgetTrackerUiState>(BudgetTrackerUiState.Loading)
    val uiState: StateFlow<BudgetTrackerUiState> = _uiState.asStateFlow()

    private val _selectedPeriod = MutableStateFlow(BudgetPeriod.THIS_YEAR)
    val selectedPeriod: StateFlow<BudgetPeriod> = _selectedPeriod.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                personRepository.getAllPersons(),
                _selectedPeriod
            ) { persons, period ->
                val (startTime, endTime) = getTimeRange(period)
                
                val spendingByPerson = mutableMapOf<Long, Double>()
                var totalSpent = 0.0
                val allGifts = mutableListOf<GiftHistoryItem>()

                persons.forEach { person ->
                    val personGifts = person.giftHistory.filter { 
                        it.purchaseDate in startTime..endTime 
                    }
                    val personSpent = personGifts.sumOf { it.price ?: 0.0 }
                    spendingByPerson[person.id] = personSpent
                    totalSpent += personSpent
                    allGifts.addAll(personGifts)
                }

                val avgPerPerson = if (persons.isNotEmpty()) totalSpent / persons.size else 0.0
                val topSpending = persons
                    .filter { (spendingByPerson[it.id] ?: 0.0) > 0 }
                    .sortedByDescending { spendingByPerson[it.id] ?: 0.0 }
                    .take(5)
                    .map { PersonSpending(it, spendingByPerson[it.id] ?: 0.0) }

                BudgetTrackerUiState.Success(
                    totalSpent = totalSpent,
                    giftCount = allGifts.size,
                    avgPerPerson = avgPerPerson,
                    topSpending = topSpending,
                    recentGifts = allGifts.sortedByDescending { it.purchaseDate }.take(10)
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun setPeriod(period: BudgetPeriod) {
        _selectedPeriod.value = period
    }

    private fun getTimeRange(period: BudgetPeriod): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val endTime = System.currentTimeMillis()

        val startTime = when (period) {
            BudgetPeriod.THIS_MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.timeInMillis
            }
            BudgetPeriod.THIS_YEAR -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.timeInMillis
            }
            BudgetPeriod.ALL_TIME -> 0L
        }

        return startTime to endTime
    }
}

sealed class BudgetTrackerUiState {
    data object Loading : BudgetTrackerUiState()
    data class Success(
        val totalSpent: Double,
        val giftCount: Int,
        val avgPerPerson: Double,
        val topSpending: List<PersonSpending>,
        val recentGifts: List<GiftHistoryItem>
    ) : BudgetTrackerUiState()
}

data class PersonSpending(
    val person: Person,
    val amount: Double
)

enum class BudgetPeriod {
    THIS_MONTH,
    THIS_YEAR,
    ALL_TIME
}
