package practice.roommigrations.data

import practice.roommigrations.data.entities.Contact

interface ContactsRepository {

    suspend fun getContacts(): Result<List<Contact>>

    suspend fun getContact(contactId: String): Result<Contact>

    suspend fun saveContact(contact: Contact)

    suspend fun updateContact(contact: Contact): Result<*>

    suspend fun deleteContact(contactId: String): Result<*>

    suspend fun deleteAllContacts(): Result<*>
}