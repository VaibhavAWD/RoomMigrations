package practice.roommigrations.data.source.local

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import practice.roommigrations.data.ContactsDataSource
import practice.roommigrations.data.Result
import practice.roommigrations.data.Result.Error
import practice.roommigrations.data.Result.Success
import practice.roommigrations.data.entities.Contact

class ContactsLocalDataSource(
    private val contactsDao: ContactsDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ContactsDataSource {

    override suspend fun getContacts(): Result<List<Contact>> {
        return withContext(ioDispatcher) {
            return@withContext try {
                Success(contactsDao.getAllContacts())
            } catch (e: Exception) {
                Error(e)
            }
        }
    }

    override suspend fun getContact(contactId: String): Result<Contact> {
        return withContext(ioDispatcher) {
            return@withContext try {
                val contact = contactsDao.getContactById(contactId)
                if (contact != null) {
                    Success(contact)
                } else {
                    Error(Exception("Contact not found"))
                }
            } catch (e: Exception) {
                Error(e)
            }
        }
    }

    override suspend fun saveContact(contact: Contact) {
        withContext(ioDispatcher) {
            contactsDao.insertContact(contact)
        }
    }

    override suspend fun updateContact(contact: Contact): Result<*> {
        return withContext(ioDispatcher) {
            return@withContext try {
                val affectedRows = contactsDao.updateContact(contact)
                if (affectedRows > 0) {
                    Success(Unit)
                } else {
                    Error(Exception("Failed to update contact"))
                }
            } catch (e: Exception) {
                Error(e)
            }
        }
    }

    override suspend fun deleteContact(contactId: String): Result<*> {
        return withContext(ioDispatcher) {
            return@withContext try {
                val affectedRows = contactsDao.deleteContactById(contactId)
                if (affectedRows > 0) {
                    Success(Unit)
                } else {
                    Error(Exception("Failed to delete contact"))
                }
            } catch (e: Exception) {
                Error(e)
            }
        }
    }

    override suspend fun deleteAllContacts(): Result<*> {
        return withContext(ioDispatcher) {
            return@withContext try {
                val affectedRows = contactsDao.deleteAllContacts()
                if (affectedRows > 0) {
                    Success(Unit)
                } else {
                    Error(Exception("Failed to delete all contacts"))
                }
            } catch (e: Exception) {
                Error(e)
            }
        }
    }
}