package practice.roommigrations.data.source.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import practice.roommigrations.data.entities.Contact

/**
 * Tests for implementation of [ContactsDao].
 */
@RunWith(AndroidJUnit4::class)
@SmallTest
class ContactsDaoTest {

    // SUT
    private lateinit var contactsDao: ContactsDao

    // Use database to access dao
    private lateinit var database: ContactsDatabase

    // Use context to create database
    private val context = ApplicationProvider.getApplicationContext<Context>()

    // test contact data
    private val testContact1 = Contact("Test Contact 1", "1234567891")
    private val testContact2 = Contact("Test Contact 2", "1234567892")
    private val testNewContact = Contact("Test New Contact", "1234567899")

    @Before
    fun setUp() {
        // we create in-memory database which does not persist after use
        database = Room.inMemoryDatabaseBuilder(context, ContactsDatabase::class.java).build()
        contactsDao = database.contactsDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun getAllContacts_emptyDatabase_emptyListReturned() = runBlocking {
        // GIVEN - database is empty

        // WHEN - getting all contacts
        val contacts = contactsDao.getAllContacts()

        // THEN - verify that contacts is empty
        assertThat(contacts).isEmpty()
    }

    @Test
    fun insertContact_gettingContacts() = runBlocking {
        // WHEN - inserting a contact
        contactsDao.insertContact(testContact1)

        // THEN - verify that contacts has expected values
        val contacts = contactsDao.getAllContacts()
        assertThat(contacts.size).isEqualTo(1)
        assertThat(contacts[0]).isEqualTo(testContact1)
    }

    @Test
    fun insertContact_sameId_contactReplaced() = runBlocking {
        // GIVEN - a contact is inserted
        contactsDao.insertContact(testContact1)

        // WHEN - inserting a contact with same id
        val newContact = Contact(testNewContact.name, testNewContact.mobile, testContact1.id)
        contactsDao.insertContact(newContact)

        // THEN - verify that contact is replaced
        val contacts = contactsDao.getAllContacts()
        assertThat(contacts.size).isEqualTo(1)
        assertThat(contacts[0]).isEqualTo(newContact)
    }

    @Test
    fun getContactById_emptyDatabase_contactNotReturned() = runBlocking {
        // GIVEN - database is empty

        // WHEN - getting a contact by id
        val contact = contactsDao.getContactById(testNewContact.id)

        // THEN - verify that contact is not returned
        assertThat(contact).isNull()
    }

    @Test
    fun getContactById_validId_contactReturned() = runBlocking {
        // GIVEN - a contact is inserted
        contactsDao.insertContact(testNewContact)

        // WHEN - getting a contact by id
        val contact = contactsDao.getContactById(testNewContact.id)

        // THEN - verify that contact is returned
        assertThat(contact).isNotNull()
        assertThat(contact).isEqualTo(testNewContact)
    }

    @Test
    fun getContactById_invalidId_contactNotReturned() = runBlocking {
        // GIVEN - a contact is inserted
        contactsDao.insertContact(testNewContact)

        // WHEN - getting a contact by invalid id
        val contact = contactsDao.getContactById(testContact1.id)

        // THEN - verify that contact is not returned
        assertThat(contact).isNull()
    }

    @Test
    fun updateContact_validId_contactUpdated() = runBlocking {
        // GIVEN - a contact is inserted
        contactsDao.insertContact(testContact1)

        // WHEN - updating a contact
        val newContact = Contact(testNewContact.name, testNewContact.mobile, testContact1.id)
        val affectedRows = contactsDao.updateContact(newContact)

        // THEN - verify that contact is updated
        assertThat(affectedRows).isGreaterThan(0)
        val contact = contactsDao.getContactById(testContact1.id)
        assertThat(contact).isEqualTo(newContact)
    }

    @Test
    fun updateContact_invalidId_contactUpdated() = runBlocking {
        // GIVEN - a contact is inserted
        contactsDao.insertContact(testContact1)

        // WHEN - updating a contact by invalid id
        val newContact = Contact(testNewContact.name, testNewContact.mobile, testNewContact.id)
        val affectedRows = contactsDao.updateContact(newContact)

        // THEN - verify that contact is not updated
        assertThat(affectedRows).isEqualTo(0)
        val contact = contactsDao.getContactById(testContact1.id)
        assertThat(contact).isNotEqualTo(newContact)
        assertThat(contact).isEqualTo(testContact1)
    }

    @Test
    fun deleteContactById_validId_contactDeleted() = runBlocking {
        // GIVEN - two contacts are inserted
        contactsDao.insertContact(testContact1)
        contactsDao.insertContact(testContact2)
        assertThat(contactsDao.getAllContacts().size).isEqualTo(2)

        // WHEN - deleting a contact
        val affectedRows = contactsDao.deleteContactById(testContact1.id)

        // THEN - verify that contact is deleted
        assertThat(affectedRows).isGreaterThan(0)
        val contacts = contactsDao.getAllContacts()
        assertThat(contacts.size).isEqualTo(1)
        assertThat(contacts[0]).isEqualTo(testContact2)
    }

    @Test
    fun deleteAContactById_invalidId_contactDeleted() = runBlocking {
        // GIVEN - two contacts are inserted
        contactsDao.insertContact(testContact1)
        contactsDao.insertContact(testContact2)
        assertThat(contactsDao.getAllContacts().size).isEqualTo(2)

        // WHEN - deleting a contact by invalid id
        val affectedRows = contactsDao.deleteContactById(testNewContact.id)

        // THEN - verify that contact is not deleted
        assertThat(affectedRows).isEqualTo(0)
        val contacts = contactsDao.getAllContacts()
        assertThat(contacts.size).isEqualTo(2)
    }

    @Test
    fun deleteAllContacts_emptyDatabase() = runBlocking {
        // GIVEN - two contacts are inserted
        contactsDao.insertContact(testContact1)
        contactsDao.insertContact(testContact2)
        assertThat(contactsDao.getAllContacts().size).isEqualTo(2)

        // WHEN - deleting all contacts
        val affectedRows = contactsDao.deleteAllContacts()

        // THEN - verify that all contacts are deleted
        assertThat(affectedRows).isGreaterThan(0)
        val contacts = contactsDao.getAllContacts()
        assertThat(contacts).isEmpty()
    }

}