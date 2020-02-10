package practice.roommigrations.data

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import practice.roommigrations.data.Result.Error
import practice.roommigrations.data.Result.Success
import practice.roommigrations.data.entities.Contact
import practice.roommigrations.data.source.local.FakeContactsLocalDataSource

/**
 * Tests for implementation of [DefaultContactsRepository].
 */
class DefaultContactsRepositoryTest {

    // SUT
    private lateinit var repository: ContactsRepository

    // Use fake local data source to set up repository
    private lateinit var localDataSource: FakeContactsLocalDataSource

    // test contact data
    private val testContact1 = Contact("Test Contact 1", "1234567891")
    private val testContact2 = Contact("Test Contact 2", "1234567892")
    private val testNewContact = Contact("Test New Contact", "1234567899")
    private val localContacts = listOf(testContact1, testContact2)
    private val newContacts = listOf(testNewContact)

    @Before
    fun setUp() {
        localDataSource = FakeContactsLocalDataSource(localContacts.toMutableList())
        repository = DefaultContactsRepository(
            localDataSource
        )
    }

    @After
    fun reset() {
        runBlocking { repository.deleteAllContacts() }
    }

    @Test
    fun getContacts_emptyRepositoryAndUninitializedCache() = runBlocking {
        // GIVEN - repository is empty
        val emptySource = FakeContactsLocalDataSource()
        repository =
            DefaultContactsRepository(emptySource)

        // WHEN - getting contacts
        val result = repository.getContacts()

        // THEN - verify that the result has expected values
        assertThat(result.succeeded).isTrue()
        assertThat((result as Success).data).isEmpty()
    }

    @Test
    fun getContacts_repositoryCachesAfterLocal() = runBlocking {
        // GIVEN - initially the repository loads contacts from local and caches
        val initial = (repository.getContacts() as Success).data

        // WHEN - update local data source with new contacts
        localDataSource.contacts = newContacts.toMutableList()

        // THEN - verify that the initial and second are equal because cache is not empty
        val second = (repository.getContacts() as Success).data
        assertThat(second).isEqualTo(initial)
    }

    @Test
    fun getContacts_requestAllContactsFromLocal() = runBlocking {
        // WHEN - getting contacts
        val contacts = (repository.getContacts() as Success).data

        // THEN - verify that the result has expected values
        assertThat(contacts).isEqualTo(localContacts)
    }

    @Test
    fun getContacts_localUnavailable_errorReturned() = runBlocking {
        // GIVEN - local data source is unavailable
        localDataSource.contacts = null

        // WHEN - getting contacts
        val result = repository.getContacts()

        // THEN - verify that the result has expected values
        assertThat(result).isInstanceOf(Error::class.java)
    }

    @Test
    fun getContact_repositoryCachesAfterLocal() = runBlocking {
        // GIVEN - contact is loaded, which is from local
        val initial = (repository.getContact(testContact1.id) as Success).data

        // WHEN - updating a contact in local data source
        val newContact = Contact(testNewContact.name, testNewContact.mobile, testContact1.id)
        localDataSource.updateContact(newContact)

        // THEN - verify that the initial and second is equal because the contact is cached
        val second = (repository.getContact(testContact1.id) as Success).data
        assertThat(second).isEqualTo(initial)
    }

    @Test
    fun getContact_contactNotPresent_errorReturned() = runBlocking {
        // GIVEN - a contact is not present in local data source
        localDataSource.deleteContact(testContact1.id)

        // WHEN - getting contact
        val result = repository.getContact(testContact1.id)

        // THEN - verify that the result has expected values
        assertThat(result).isInstanceOf(Error::class.java)
    }

    @Test
    fun saveContact_savesToCacheAndLocal() = runBlocking {
        // GIVEN - new contact is not is the local or cache
        assertThat(localDataSource.contacts).doesNotContain(testNewContact)
        assertThat((repository.getContacts() as Success).data).doesNotContain(testNewContact)

        // WHEN - new contact is saved
        repository.saveContact(testNewContact)

        // THEN - verify that the contact is saved to local and cache
        assertThat((localDataSource.getContacts() as Success).data).contains(testNewContact)
        assertThat((repository.getContacts() as Success).data).contains(testNewContact)
    }

    @Test
    fun updateContact_updatesInCacheAndLocal() = runBlocking {
        // get the initial contact
        val initial = (repository.getContact(testContact1.id) as Success).data

        // WHEN - a contact is updated
        val newContact = Contact(testNewContact.name, testNewContact.mobile, testContact1.id)
        val result = repository.updateContact(newContact)

        // THEN - verify that the contact is updated in local and cache
        assertThat(result.succeeded).isTrue()
        val localContact = (localDataSource.getContact(testContact1.id) as Success).data
        assertThat(localContact).isEqualTo(newContact)
        val second = (repository.getContact(testContact1.id) as Success).data
        assertThat(initial).isNotEqualTo(second)
        assertThat(second).isEqualTo(newContact)
    }

    @Test
    fun updateContact_error_notUpdatedInCacheAndLocal() = runBlocking {
        // get the initial contact
        val initial = (repository.getContact(testContact1.id) as Success).data
        assertThat(localDataSource.contacts).doesNotContain(testNewContact)

        // WHEN - a contact is updated with invalid id
        val newContact = Contact(testNewContact.name, testNewContact.mobile, testNewContact.id)
        val result = repository.updateContact(newContact)

        // THEN - verify that the contact is not updated in local and cache
        assertThat(result).isInstanceOf(Error::class.java)
        val localContact = (localDataSource.getContact(testContact1.id) as Success).data
        assertThat(localContact).isNotEqualTo(newContact)
        val second = (repository.getContact(testContact1.id) as Success).data
        assertThat(initial).isEqualTo(second)
        assertThat(second).isNotEqualTo(newContact)
    }

    @Test
    fun deleteContact_deletesFromCacheAndLocal() = runBlocking {
        // get the initial contacts
        val initial = (repository.getContacts() as Success).data
        assertThat(localDataSource.contacts).contains(testContact1)
        assertThat(initial).contains(testContact1)
        assertThat(initial.size).isEqualTo(2)

        // WHEN - a contact is deleted
        val result = repository.deleteContact(testContact1.id)

        // THEN - verify that the contact is deleted from local and cache
        assertThat(result.succeeded).isTrue()
        assertThat(localDataSource.contacts).doesNotContain(testContact1)
        val second = (repository.getContacts() as Success).data
        assertThat(second.size).isEqualTo(1)
        assertThat(second).doesNotContain(testContact1)
    }

    @Test
    fun deleteContact_error_notDeletedFromCacheAndLocal() = runBlocking {
        // get the initial contacts
        val initial = (repository.getContacts() as Success).data
        assertThat(localDataSource.contacts?.size).isEqualTo(2)
        assertThat(initial.size).isEqualTo(2)

        // WHEN - a contact is deleted with invalid id
        val result = repository.deleteContact(testNewContact.id)

        // THEN - verify that the contact is not deleted from local and cache
        assertThat(result).isInstanceOf(Error::class.java)
        assertThat(localDataSource.contacts?.size).isEqualTo(2)
        val second = (repository.getContacts() as Success).data
        assertThat(second).isEqualTo(initial)
    }

    @Test
    fun deleteAllContacts_contactsDeletedFromCacheAndLocal() = runBlocking {
        // get the initial contacts
        val initial = (repository.getContacts() as Success).data
        assertThat(localDataSource.contacts?.size).isEqualTo(2)
        assertThat(initial.size).isEqualTo(2)

        // WHEN - all contacts are deleted
        val result = repository.deleteAllContacts()

        // THEN - verify that the contact is not deleted from local and cache
        assertThat(result.succeeded).isTrue()
        assertThat(localDataSource.contacts).isEmpty()
        val second = (repository.getContacts() as Success).data
        assertThat(second).isEmpty()
    }

}