package com.gift.finder.domain.usecase

import com.gift.finder.data.repository.SpecialDateRepository
import com.gift.finder.domain.model.SpecialDate
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting upcoming special dates.
 */
class GetUpcomingDatesUseCase @Inject constructor(
    private val specialDateRepository: SpecialDateRepository
) {
    operator fun invoke(daysAhead: Int = 30): Flow<List<SpecialDate>> {
        return specialDateRepository.getUpcomingDates(daysAhead)
    }
}
