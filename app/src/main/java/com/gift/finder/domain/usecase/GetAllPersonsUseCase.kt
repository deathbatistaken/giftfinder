package com.gift.finder.domain.usecase

import com.gift.finder.data.repository.PersonRepository
import com.gift.finder.domain.model.Person
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting all persons.
 */
class GetAllPersonsUseCase @Inject constructor(
    private val personRepository: PersonRepository
) {
    operator fun invoke(): Flow<List<Person>> {
        return personRepository.getAllPersons()
    }
}
