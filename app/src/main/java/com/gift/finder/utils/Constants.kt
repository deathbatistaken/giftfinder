package com.gift.finder.utils

/**
 * Application constants.
 */
object Constants {
    // Database
    const val DATABASE_NAME = "gift_finder_database"
    
    // Notification
    const val NOTIFICATION_CHANNEL_REMINDERS = "reminder_channel"
    const val NOTIFICATION_CHANNEL_GENERAL = "general_channel"
    
    // Premium limits
    const val FREE_MAX_PERSONS = 1
    const val FREE_MAX_SPECIAL_DATES = 1
    const val FREE_MAX_VISIBLE_SUGGESTIONS = 2
    
    // Timing
    const val ONE_YEAR_MILLIS = 365L * 24 * 60 * 60 * 1000
    const val ONE_DAY_MILLIS = 24L * 60 * 60 * 1000
    
    // Notification offsets (days before event)
    val DEFAULT_NOTIFICATION_OFFSETS = listOf(7, 3, 0)
    
    // Default notification time
    const val NOTIFICATION_HOUR = 9
    const val NOTIFICATION_MINUTE = 0
}
