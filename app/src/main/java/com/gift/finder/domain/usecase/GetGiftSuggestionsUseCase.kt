package com.gift.finder.domain.usecase

import com.gift.finder.data.repository.GiftRepository
import com.gift.finder.domain.model.BudgetRange
import com.gift.finder.domain.model.GiftStyle
import com.gift.finder.domain.model.GiftSuggestion
import com.gift.finder.domain.model.Person
import javax.inject.Inject

/**
 * Use case for getting gift suggestions for a person.
 */
class GetGiftSuggestionsUseCase @Inject constructor(
    private val giftRepository: GiftRepository
) {
    suspend operator fun invoke(
        person: Person,
        style: GiftStyle? = null,
        budget: BudgetRange? = null
    ): List<GiftSuggestion> {
        return giftRepository.getSuggestions(person, style, budget)
    }
}
