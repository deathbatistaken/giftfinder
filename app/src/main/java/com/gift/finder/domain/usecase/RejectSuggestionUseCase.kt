package com.gift.finder.domain.usecase

import com.gift.finder.data.repository.GiftRepository
import com.gift.finder.domain.model.RejectionReason
import javax.inject.Inject

/**
 * Use case for rejecting a gift suggestion (Shadow Learning).
 */
class RejectSuggestionUseCase @Inject constructor(
    private val giftRepository: GiftRepository
) {
    suspend operator fun invoke(
        personId: Long,
        categoryId: String,
        reason: RejectionReason
    ) {
        giftRepository.rejectSuggestion(personId, categoryId, reason)
    }
}
