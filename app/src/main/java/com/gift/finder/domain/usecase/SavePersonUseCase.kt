package com.gift.finder.domain.usecase

import com.gift.finder.data.repository.PersonRepository
import com.gift.finder.domain.model.Person
import javax.inject.Inject

/**
 * Use case for saving a person (insert or update).
 */
class SavePersonUseCase @Inject constructor(
    private val personRepository: PersonRepository
) {
    suspend operator fun invoke(person: Person): Long {
        return if (person.id > 0) {
            personRepository.updatePerson(person)
            person.id
        } else {
            personRepository.insertPerson(person)
        }
    }
}
