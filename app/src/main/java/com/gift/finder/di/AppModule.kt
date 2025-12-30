package com.gift.finder.di

import android.content.Context
import com.gift.finder.data.manager.BillingManager
import com.gift.finder.data.manager.LocalNotificationManager
import com.gift.finder.data.manager.PreferencesManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for application-level dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePreferencesManager(
        @ApplicationContext context: Context
    ): PreferencesManager {
        return PreferencesManager(context)
    }

    @Provides
    @Singleton
    fun provideLocalNotificationManager(
        @ApplicationContext context: Context
    ): LocalNotificationManager {
        return LocalNotificationManager(context)
    }

    @Provides
    @Singleton
    fun provideBillingManager(
        @ApplicationContext context: Context,
        preferencesManager: PreferencesManager
    ): BillingManager {
        return BillingManager(context, preferencesManager)
    }
}
