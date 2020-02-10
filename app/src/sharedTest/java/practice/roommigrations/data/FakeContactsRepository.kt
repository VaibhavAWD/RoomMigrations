package practice.roommigrations.data

import com.google.common.collect.Lists
import practice.roommigrations.data.Result.Error
import practice.roommigrations.data.Result.Success
import practice.roommigrations.data.entities.Contact

class FakeContactsRepository : ContactsRepository {

    private var contactsServiceData: LinkedHashMap<String, Contact> = LinkedHashMap()

    private var shouldReturnError = false

    fun setShouldReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getContacts(): Result<List<Contact>> {
        if (shouldReturnError) {
            return Error(Exception("Test exception"))
        }
        val contacts = Lists.newArrayList(contactsServiceData.values)
        return Success(contacts)
    }

    override suspend fun getContact(contactId: String): Result<Contact> {
        if (shouldReturnError) {
            return Error(Exception("Test exception"))
        }
        contactsServiceData.values.firstOrNull { it.id == contactId }?.let { return Success(it) }
        return Error(Exception("Contact not found"))
    }

    override suspend fun saveContact(contact: Contact) {
        contactsServiceData[contact.id] = contact
    }

    override suspend fun updateContact(contact: Contact): Result<*> {
        if (shouldReturnError) {
            return Error(Exception("Test exception"))
        }
        contactsServiceData[contact.id] = contact
        return Success(Unit)
    }

    override suspend fun deleteContact(contactId: String): Result<*> {
        if (shouldReturnError) {
            return Error(Exception("Test exception"))
        }
        contactsServiceData.remove(contactId)
        return Success(Unit)
    }

    override suspend fun deleteAllContacts(): Result<*> {
        if (shouldReturnError) {
            return Error(Exception("Test exception"))
        }
        contactsServiceData.clear()
        return Success(Unit)
    }

    fun addContacts(vararg contacts: Contact) {
        for (contact in contacts) {
            contactsServiceData[contact.id] = contact
        }
    }
}
