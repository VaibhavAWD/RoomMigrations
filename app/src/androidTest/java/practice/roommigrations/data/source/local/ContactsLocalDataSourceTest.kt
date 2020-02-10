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
import practice.roommigrations.data.Result.Error
import practice.roommigrations.data.Result.Success
import practice.roommigrations.data.entities.Contact
import practice.roommigrations.data.succeeded

/**
 * Tests for implementation of [ContactsLocalDataSource].
 */
@RunWith(AndroidJUnit4::class)
@SmallTest
class ContactsLocalDataSourceTest {

    // SUT
    private lateinit var contactsLocalDataSource: ContactsLocalDataSource

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
        val contactsDao = database.contactsDao()
        contactsLocalDataSource = ContactsLocalDataSource(contactsDao)
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun getContacts_emptyDatabase_emptyListReturned() = runBlocking {
        // WHEN - getting contacts
        val result = contactsLocalDataSource.getContacts()

        // THEN - verify that the result has expected values
        assertThat(result.succeeded).isTrue()
        assertThat((result as Success).data).isEmpty()
    }

    @Test
    fun saveContact_gettingContacts() = runBlocking {
        // WHEN - saving a contact
        contactsLocalDataSource.saveContact(testNewContact)

        // THEN - verify that the result has expected values
        val contacts = (contactsLocalDataSource.getContacts() as Success).data
        assertThat(contacts.size).isEqualTo(1)
    }

    @Test
    fun getContact_emptyDatabase_errorReturned() = runBlocking {
        // WHEN - getting a contact
        val result = contactsLocalDataSource.getContact(testNewContact.id)

        // THEN - verify that the result has expected values
        assertThat(result).isInstanceOf(Error::class.java)
    }

    @Test
    fun getContact_contactAvailable_contactReturned() = runBlocking {
        // GIVEN - a contact is saved
        contactsLocalDataSource.saveContact(testNewContact)

        // WHEN - getting a contact
        val result = contactsLocalDataSource.getContact(testNewContact.id)

        // THEN - verify that the result has expected values
        assertThat(result.succeeded).isTrue()
        assertThat((result as Success).data).isEqualTo(testNewContact)
    }

    @Test
    fun updateContact_validId_contactUpdated() = runBlocking {
        // GIVEN - a contact is saved
        contactsLocalDataSource.saveContact(testContact1)

        // WHEN - updating a contact
        val newContact = Contact(testNewContact.name, testNewContact.mobile, testContact1.id)
        val result = contactsLocalDataSource.updateContact(newContact)

        // THEN - verify that the result has expected values
        assertThat(result.succeeded).isTrue()
    }

    @Test
    fun updateContact_invalidId_contactUpdated() = runBlocking {
        // GIVEN - a contact is saved
        contactsLocalDataSource.saveContact(testContact1)

        // WHEN - updating a contact by invalid id
        val newContact = Contact(testNewContact.name, testNewContact.mobile, testNewContact.id)
        val result = contactsLocalDataSource.updateContact(newContact)

        // THEN - verify that the result has expected values
        assertThat(result).isInstanceOf(Error::class.java)
    }

    @Test
    fun deleteContact_validId_contactDeleted() = runBlocking {
        // GIVEN - a contact is saved
        contactsLocalDataSource.saveContact(testContact1)

        // WHEN - deleting a contact
        val result = contactsLocalDataSource.deleteContact(testContact1.id)

        // THEN - verify that the result has expected values
        assertThat(result.succeeded).isTrue()
    }

    @Test
    fun deleteContact_invalidId_contactDeleted() = runBlocking {
        // GIVEN - a contact is saved
        contactsLocalDataSource.saveContact(testContact1)

        // WHEN - deleting a contact by invalid id
        val result = contactsLocalDataSource.deleteContact(testNewContact.id)

        // THEN - verify that the result has expected values
        assertThat(result).isInstanceOf(Error::class.java)
    }

    @Test
    fun deleteAllContact_allContactDeleted() = runBlocking {
        // GIVEN - two contacts are saved
        contactsLocalDataSource.saveContact(testContact1)
        contactsLocalDataSource.saveContact(testContact2)

        // WHEN - deleting all contacts
        val result = contactsLocalDataSource.deleteAllContacts()

        // THEN - verify that the result has expected values
        assertThat(result.succeeded).isTrue()
    }
}