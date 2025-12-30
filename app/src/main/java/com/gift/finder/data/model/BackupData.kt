package com.gift.finder.data.model

import com.gift.finder.domain.model.Person
import com.gift.finder.domain.model.SpecialDate

/**
 * Data Transfer Objects for Backup/Restore functionality.
 */
data class BackupData(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val persons: List<Person>,
    val specialDates: List<SpecialDate>,
    val savedGifts: List<SavedGiftBackup>
)

data class SavedGiftBackup(
    val personId: Long,
    val categoryId: String,
    val savedAt: Long
)
