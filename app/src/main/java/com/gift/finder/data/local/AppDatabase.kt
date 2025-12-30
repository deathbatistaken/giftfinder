package com.gift.finder.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gift.finder.data.local.dao.SavedGiftDao
import com.gift.finder.data.local.dao.SpecialDateDao
import com.gift.finder.data.local.entities.GiftHistoryEntity
import com.gift.finder.data.local.entities.PersonEntity
import com.gift.finder.data.local.entities.RejectedGiftEntity
import com.gift.finder.data.local.entities.SavedGiftEntity
import com.gift.finder.data.local.entities.SpecialDateEntity

/**
 * Main Room Database for GiftFinder app.
 */
@Database(
    entities = [
        PersonEntity::class,
        SpecialDateEntity::class,
        GiftHistoryEntity::class,
        RejectedGiftEntity::class,
        SavedGiftEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun personDao(): PersonDao
    abstract fun specialDateDao(): SpecialDateDao
    abstract fun giftHistoryDao(): GiftHistoryDao
    abstract fun rejectedGiftDao(): RejectedGiftDao
    abstract fun savedGiftDao(): SavedGiftDao
    
    companion object {
        const val DATABASE_NAME = "gift_finder_database"
    }
}
