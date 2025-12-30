package com.gift.finder.di

import android.content.Context
import com.gift.finder.data.local.dao.GiftHistoryDao
import com.gift.finder.data.local.dao.PersonDao
import com.gift.finder.data.local.dao.RejectedGiftDao
import com.gift.finder.data.local.dao.SpecialDateDao
import com.gift.finder.data.repository.GiftRepository
import com.gift.finder.data.repository.PersonRepository
import com.gift.finder.data.repository.SpecialDateRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for repository dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun providePersonRepository(
        personDao: PersonDao,
        specialDateDao: SpecialDateDao,
        giftHistoryDao: GiftHistoryDao,
        rejectedGiftDao: RejectedGiftDao
    ): PersonRepository {
        return PersonRepository(
            personDao,
            specialDateDao,
            giftHistoryDao,
            rejectedGiftDao
        )
    }

    @Provides
    @Singleton
    fun provideGiftRepository(
        @ApplicationContext context: Context,
        giftHistoryDao: GiftHistoryDao,
        rejectedGiftDao: RejectedGiftDao
    ): GiftRepository {
        return GiftRepository(context, giftHistoryDao, rejectedGiftDao)
    }

    @Provides
    @Singleton
    fun provideSpecialDateRepository(
        specialDateDao: SpecialDateDao
    ): SpecialDateRepository {
        return SpecialDateRepository(specialDateDao)
    }
}
