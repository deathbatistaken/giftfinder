package com.gift.finder.di

import android.content.Context
import androidx.room.Room
import com.gift.finder.data.local.AppDatabase
import com.gift.finder.data.local.dao.GiftHistoryDao
import com.gift.finder.data.local.dao.PersonDao
import com.gift.finder.data.local.dao.RejectedGiftDao
import com.gift.finder.data.local.dao.SavedGiftDao
import com.gift.finder.data.local.dao.SpecialDateDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for database dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun providePersonDao(database: AppDatabase): PersonDao {
        return database.personDao()
    }

    @Provides
    @Singleton
    fun provideSpecialDateDao(database: AppDatabase): SpecialDateDao {
        return database.specialDateDao()
    }

    @Provides
    @Singleton
    fun provideGiftHistoryDao(database: AppDatabase): GiftHistoryDao {
        return database.giftHistoryDao()
    }

    @Provides
    @Singleton
    fun provideRejectedGiftDao(database: AppDatabase): RejectedGiftDao {
        return database.rejectedGiftDao()
    }

    @Provides
    @Singleton
    fun provideSavedGiftDao(database: AppDatabase): SavedGiftDao {
        return database.savedGiftDao()
    }
}
