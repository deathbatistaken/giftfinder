package com.gift.finder.data.repository

import android.content.Context
import android.provider.ContactsContract
import com.gift.finder.domain.model.Contact
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ContactRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun getContacts(): List<Contact> = withContext(Dispatchers.IO) {
        val contacts = mutableListOf<Contact>()
        
        // Ensure we have permission
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_CONTACTS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            return@withContext emptyList()
        }

        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.Contacts.PHOTO_URI
        )
        
        val sortOrder = "${ContactsContract.Contacts.DISPLAY_NAME_PRIMARY} ASC"

        context.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID)
            val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
            val photoIndex = cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI)

            while (cursor.moveToNext()) {
                val id = cursor.getString(idIndex)
                val name = cursor.getString(nameIndex) ?: continue // Skip empty names
                val photoUri = cursor.getString(photoIndex)
                
                val initial = if (name.isNotBlank()) name.first().uppercase() else "?"

                // Only query birthday if needed to avoid N+1 too heavily, but for now we do it
                // A better approach is to query Data table once for all birthdays and map them
                val birthday = getContactBirthday(id)
                
                contacts.add(
                    Contact(
                        id = id,
                        name = name,
                        initial = initial,
                        photoUri = photoUri,
                        birthdayDate = birthday
                    )
                )
            }
        }
        
        return@withContext contacts
    }

    private fun getContactBirthday(contactId: String): String? {
        var birthday: String? = null
        
        val projection = arrayOf(ContactsContract.CommonDataKinds.Event.START_DATE)
        
        val selection = "${ContactsContract.Data.CONTACT_ID} = ? AND " +
                "${ContactsContract.Data.MIMETYPE} = ? AND " +
                "${ContactsContract.CommonDataKinds.Event.TYPE} = ?"
                
        val selectionArgs = arrayOf(
            contactId,
            ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY.toString()
        )

        try {
            context.contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val dateIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE)
                    if (dateIndex != -1) {
                         birthday = cursor.getString(dateIndex)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return birthday
    }
}
