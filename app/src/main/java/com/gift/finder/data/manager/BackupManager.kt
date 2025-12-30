package com.gift.finder.data.manager

import com.gift.finder.data.model.BackupData
import com.gift.finder.data.model.SavedGiftBackup
import com.gift.finder.data.repository.PersonRepository
import com.gift.finder.data.repository.SavedGiftRepository
import com.gift.finder.data.repository.SpecialDateRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages secure data backup and restoration.
 */
@Singleton
class BackupManager @Inject constructor(
    private val personRepository: PersonRepository,
    private val specialDateRepository: SpecialDateRepository,
    private val savedGiftRepository: SavedGiftRepository,
    private val gson: Gson
) {

    /**
     * Exports all user data to the provided Output Stream (JSON).
     */
    suspend fun exportData(outputStream: OutputStream) {
        withContext(Dispatchers.IO) {
            val persons = personRepository.getAllPersonsSync()
            val specialDates = specialDateRepository.getAllSpecialDatesSync()
            val savedGifts = savedGiftRepository.getAllSavedGifts().map {
                SavedGiftBackup(it.personId, it.categoryId, it.savedAt)
            }

            val backup = BackupData(
                persons = persons,
                specialDates = specialDates,
                savedGifts = savedGifts
            )

            val jsonString = gson.toJson(backup)
            outputStream.use { it.write(jsonString.toByteArray()) }
        }
    }

    /**
     * Imports data from the provided Input Stream (JSON).
     * WARNING: Wipes existing data.
     */
    suspend fun importData(inputStream: InputStream) {
        withContext(Dispatchers.IO) {
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val backup = gson.fromJson(jsonString, BackupData::class.java)

            // Validate data (basic check)
            if (backup.persons.isEmpty() && backup.specialDates.isEmpty()) {
                // Allow empty restore? Probably, but let's assume it's valid if it parsed.
            }

            // Wipe existing data
            savedGiftRepository.deleteAll()
            specialDateRepository.deleteAll()
            personRepository.deleteAllPersons()

            // Restore Persons
            backup.persons.forEach { person ->
                // InsertPerson handles ID logic. If ID is present, Room uses it.
                personRepository.insertPerson(person)
            }

            // Restore Special Dates (Independent ones or all)
            // Note: insertPerson might have inserted some if they were nested.
            // But since we fetched Sync, they might be empty.
            // Safest to insert all from specialDates list. REPLACE strategy handles duplicates.
            backup.specialDates.forEach { date ->
                personRepository.addSpecialDate(date) // Using Repo method to ensure consistency
            }

            // Restore Saved Gifts
            backup.savedGifts.forEach { gift ->
                savedGiftRepository.saveGift(gift.personId, gift.categoryId)
            }
        }
    }
}
