package practice.roommigrations.data

import com.google.common.collect.Lists
import kotlinx.coroutines.*
import practice.roommigrations.data.Result.Error
import practice.roommigrations.data.Result.Success
import practice.roommigrations.data.entities.Contact
import java.util.concurrent.ConcurrentHashMap

class DefaultContactsRepository(
    private val contactsLocalDataSource: ContactsDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ContactsRepository {

    private var cachedContacts: ConcurrentHashMap<String, Contact>? = null

    override suspend fun getContacts(): Result<List<Contact>> {
        return withContext(ioDispatcher) {
            if (!cachedContacts.isNullOrEmpty()) {
                return@withContext Success(Lists.newArrayList(cachedContacts!!.values.sortedBy { it.name }))
            }

            val newContacts = fetchContactsFromLocal()

            (newContacts as? Success)?.let { refreshCache(it.data) }

            cachedContacts?.values?.let { cachedContacts ->
                return@withContext Success(cachedContacts.sortedBy { it.name })
            }

            (newContacts as? Success)?.data?.let {
                if (it.isNullOrEmpty()) {
                    return@withContext Success(it)
                }
            }

            return@withContext Error(Exception("Illegal state"))
        }
    }

    private suspend fun fetchContactsFromLocal(): Result<List<Contact>> {
        val localContacts = contactsLocalDataSource.getContacts()
        if (localContacts is Success) return localContacts
        return Error(Exception("Failed to fetch contacts from local data source"))
    }

    override suspend fun getContact(contactId: String): Result<Contact> {
        return withContext(ioDispatcher) {
            cachedContacts?.values?.firstOrNull { it.id == contactId }?.let {
                return@withContext Success(it)
            }

            val newContact = fetchContactFromLocal(contactId)

            (newContact as? Success)?.let { cacheContact(it.data) }

            return@withContext newContact
        }
    }

    private suspend fun fetchContactFromLocal(contactId: String): Result<Contact> {
        val localContact = contactsLocalDataSource.getContact(contactId)
        if (localContact is Success) return localContact
        return Error(Exception("Failed to fetch contact from local data source"))
    }

    override suspend fun saveContact(contact: Contact) {
        cacheAndPerform(contact) {
            coroutineScope {
                launch { contactsLocalDataSource.saveContact(it) }
            }
        }
    }

    override suspend fun updateContact(contact: Contact): Result<*> {
        return withContext(ioDispatcher) {
            val result = contactsLocalDataSource.updateContact(contact)
            return@withContext if (result is Success) {
                cachedContacts?.replace(contact.id, contact)
                Success(Unit)
            } else {
                Error(Exception("Failed to update contact"))
            }
        }
    }

    override suspend fun deleteContact(contactId: String): Result<*> {
        return withContext(ioDispatcher) {
            val result = contactsLocalDataSource.deleteContact(contactId)
            return@withContext if (result is Success) {
                cachedContacts?.remove(contactId)
                Success(Unit)
            } else {
                Error(Exception("Failed to delete contact"))
            }
        }
    }

    override suspend fun deleteAllContacts(): Result<*> {
        return withContext(ioDispatcher) {
            val result = contactsLocalDataSource.deleteAllContacts()
            return@withContext if (result is Success) {
                cachedContacts?.clear()
                Success(Unit)
            } else {
                Error(Exception("Failed to delete all contacts"))
            }
        }
    }

    private fun refreshCache(contacts: List<Contact>) {
        cachedContacts?.clear()
        for (contact in contacts) {
            cacheAndPerform(contact) {}
        }
    }

    private inline fun cacheAndPerform(contact: Contact, perform: (contact: Contact) -> Unit) {
        val cachedContact = cacheContact(contact)
        perform(cachedContact)
    }

    private fun cacheContact(contact: Contact): Contact {
        if (cachedContacts == null) {
            cachedContacts = ConcurrentHashMap()
        }
        cachedContacts?.put(contact.id, contact)
        return contact
    }
}