package com.gift.finder.domain.model

/**
 * Model representing a contact from the device's address book.
 */
data class Contact(
    val id: String,
    val name: String,
    val initial: String,
    val phoneNumber: String? = null,
    val email: String? = null,
    val photoUri: String? = null,
    val birthdayDate: String? = null // Format: YYYY-MM-DD or --MM-DD
) {
    // Helper to get initials if name is empty (fallback)
    fun getInitials(): String {
        return if (initial.isNotEmpty()) initial else name.take(1).uppercase()
    }
}
