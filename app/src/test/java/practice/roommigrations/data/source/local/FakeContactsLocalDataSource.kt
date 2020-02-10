package practice.roommigrations.data.source.local

import practice.roommigrations.data.ContactsDataSource
import practice.roommigrations.data.Result
import practice.roommigrations.data.Result.Error
import practice.roommigrations.data.Result.Success
import practice.roommigrations.data.entities.Contact

class FakeContactsLocalDataSource(
    var contacts: MutableList<Contact>? = mutableListOf()
) : ContactsDataSource {

    override suspend fun getContacts(): Result<List<Contact>> {
        contacts?.let { return Success(it) }
        return Error(Exception("Contacts not found"))
    }

    override suspend fun getContact(contactId: String): Result<Contact> {
        contacts?.firstOrNull { it.id == contactId }?.let { return Success(it) }
        return Error(Exception("Contact not found"))
    }

    override suspend fun saveContact(contact: Contact) {
        contacts?.removeIf { it.id == contact.id }
        contacts?.add(contact)
    }

    override suspend fun updateContact(contact: Contact): Result<*> {
        val result = contacts?.removeIf { it.id == contact.id }
        result?.let { isRemoved ->
            if (isRemoved) {
                contacts?.add(contact)
                return Success(Unit)
            }
        }
        return Error(Exception("Failed to update contact"))
    }

    override suspend fun deleteContact(contactId: String): Result<*> {
        val result = contacts?.removeIf { it.id == contactId }
        result?.let { isRemoved ->
            if (isRemoved) {
                return Success(Unit)
            }
        }
        return Error(Exception("Failed to delete contact"))
    }

    override suspend fun deleteAllContacts(): Result<*> {
        contacts?.clear()
        return Success(Unit)
    }
}
