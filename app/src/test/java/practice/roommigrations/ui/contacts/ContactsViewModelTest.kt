@file:Suppress("DEPRECATION")

package practice.roommigrations.ui.contacts

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineContext
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import practice.roommigrations.R
import practice.roommigrations.data.FakeContactsRepository
import practice.roommigrations.data.entities.Contact
import practice.roommigrations.util.LiveDataTestUtil
import practice.roommigrations.util.ViewModelScopeMainDispatcherRule
import practice.roommigrations.util.assertLiveDataEventTriggered

/**
 * Tests for implementation of [ContactsViewModel].
 */
class ContactsViewModelTest {

    // SUT
    private lateinit var contactsViewModel: ContactsViewModel

    // Use fake repository to set up view model
    private lateinit var repository: FakeContactsRepository

    // Use test context that can be controlled from tests
    private val testContext = TestCoroutineContext()

    // Set main coroutines dispatcher for unit testing
    @ExperimentalCoroutinesApi
    @get:Rule
    var coroutinesMainDispatcherRule = ViewModelScopeMainDispatcherRule(testContext)

    // Executes each task synchronously using Architecture components.
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    // test contact data
    private val testContact1 = Contact("Test Contact 1", "1234567891")
    private val testContact2 = Contact("Test Contact 2", "1234567892")

    @Before
    fun setUp() {
        repository = FakeContactsRepository()
        repository.addContacts(testContact1, testContact2)
        contactsViewModel = ContactsViewModel(repository)
    }

    @Test
    fun loadContacts_success_loadingTogglesAndDataLoaded() = runBlocking {
        // WHEN - loading contacts
        contactsViewModel.loadContacts()

        // THEN - verify that following events occur
        // progress is shown
        assertThat(LiveDataTestUtil.getValue(contactsViewModel.dataLoading)).isTrue()

        // execute pending coroutines actions
        testContext.triggerActions()

        // progress is hidden
        assertThat(LiveDataTestUtil.getValue(contactsViewModel.dataLoading)).isFalse()

        // and data loaded correctly
        assertThat(LiveDataTestUtil.getValue(contactsViewModel.contacts)).hasSize(2)
    }

    @Test
    fun loadContacts_error_loadingTogglesAndDataNotLoadedAndSetsEvent() = runBlocking {
        // GIVEN - repository returns error
        repository.setShouldReturnError(true)

        // WHEN - loading contacts
        contactsViewModel.loadContacts()

        // THEN - verify that following events occur
        // progress is shown
        assertThat(LiveDataTestUtil.getValue(contactsViewModel.dataLoading)).isTrue()

        // execute pending coroutines actions
        testContext.triggerActions()

        // progress is hidden
        assertThat(LiveDataTestUtil.getValue(contactsViewModel.dataLoading)).isFalse()

        // and data loaded correctly
        assertThat(LiveDataTestUtil.getValue(contactsViewModel.contacts)).isEmpty()

        // and sets show message event
        assertLiveDataEventTriggered(contactsViewModel.showMessageEvent, R.string.error_loading_contacts)
    }

    @Test
    fun showDeleteAllContactsAlert_setsEvent() {
        // WHEN - show delete all contacts alert
        contactsViewModel.showDeleteAllContactsAlert()

        // THEN - verify that the delete all contacts alert event is triggered
        assertLiveDataEventTriggered(contactsViewModel.deleteAllContactsAlertEvent, Unit)
    }

    @Test
    fun deleteAllContacts_loadingTogglesAndSetsEvent() = runBlocking {
        // GIVEN - contacts are loaded
        contactsViewModel.loadContacts()

        // execute pending coroutines actions
        testContext.triggerActions()

        // verify that the contacts are loaded
        assertThat(LiveDataTestUtil.getValue(contactsViewModel.contacts)).hasSize(2)

        // WHEN - deleting all contacts
        contactsViewModel.deleteAllContacts()

        // THEN - verify that following events occur
        // progress is shown
        assertThat(LiveDataTestUtil.getValue(contactsViewModel.dataLoading)).isTrue()

        // execute pending coroutines actions
        testContext.triggerActions()

        // progress is hidden
        assertThat(LiveDataTestUtil.getValue(contactsViewModel.dataLoading)).isFalse()

        // and all contacts deleted
        assertThat(LiveDataTestUtil.getValue(contactsViewModel.contacts)).isEmpty()

        // and sets show message event
        assertLiveDataEventTriggered(contactsViewModel.showMessageEvent, R.string.all_contacts_deleted)
    }

    @Test
    fun deleteAllContacts_error_loadingTogglesAndSetsEvent() = runBlocking {
        // initially contacts are loaded
        contactsViewModel.loadContacts()

        // execute pending coroutines actions
        testContext.triggerActions()

        // verify that the contacts are loaded
        assertThat(LiveDataTestUtil.getValue(contactsViewModel.contacts)).hasSize(2)

        // GIVEN - repository returns error
        repository.setShouldReturnError(true)

        // WHEN - deleting all contacts
        contactsViewModel.deleteAllContacts()

        // THEN - verify that following events occur
        // progress is shown
        assertThat(LiveDataTestUtil.getValue(contactsViewModel.dataLoading)).isTrue()

        // execute pending coroutines actions
        testContext.triggerActions()

        // progress is hidden
        assertThat(LiveDataTestUtil.getValue(contactsViewModel.dataLoading)).isFalse()

        // and all contacts deleted
        assertThat(LiveDataTestUtil.getValue(contactsViewModel.contacts)).hasSize(2)

        // and sets show message event
        assertLiveDataEventTriggered(contactsViewModel.showMessageEvent, R.string.error_deleting_all_contacts)
    }

    @Test
    fun addNewContact_setsEvent() {
        // WHEN - adding new contact
        contactsViewModel.addNewContact()

        // THEN - verify that add new contact event is triggered
        assertLiveDataEventTriggered(contactsViewModel.addNewContactEvent, Unit)
    }

    @Test
    fun openContact_setsEvent() {
        // WHEN - opening contact
        contactsViewModel.openContact(testContact1.id)

        // THEN - verify that open contact event is triggered
        assertLiveDataEventTriggered(contactsViewModel.openContactEvent, testContact1.id)
    }

}