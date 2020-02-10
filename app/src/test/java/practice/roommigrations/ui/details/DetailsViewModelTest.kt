@file:Suppress("DEPRECATION")

package practice.roommigrations.ui.details

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
 * Tests for implementation of [DetailsViewModel].
 */
class DetailsViewModelTest {

    // SUT
    private lateinit var detailsViewModel: DetailsViewModel

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
        detailsViewModel = DetailsViewModel(repository)
    }

    @Test
    fun loadContact_success_loadingTogglesAndDataLoaded() = runBlocking {
        // WHEN - loading contact
        detailsViewModel.loadContact(testContact1.id)

        // THEN - verify that following events occur
        // progress is shown
        assertThat(LiveDataTestUtil.getValue(detailsViewModel.dataLoading)).isTrue()

        // execute pending coroutines actions
        testContext.triggerActions()

        // progress is hidden
        assertThat(LiveDataTestUtil.getValue(detailsViewModel.dataLoading)).isFalse()

        // and data loaded correctly
        assertThat(LiveDataTestUtil.getValue(detailsViewModel.contact)).isEqualTo(testContact1)
        assertThat(LiveDataTestUtil.getValue(detailsViewModel.dataAvailable)).isTrue()
    }

    @Test
    fun loadContact_error_loadingTogglesAndDataLoaded() = runBlocking {
        // GIVEN - repository returns error
        repository.setShouldReturnError(true)

        // WHEN - loading contact
        detailsViewModel.loadContact(testContact1.id)

        // THEN - verify that following events occur
        // progress is shown
        assertThat(LiveDataTestUtil.getValue(detailsViewModel.dataLoading)).isTrue()

        // execute pending coroutines actions
        testContext.triggerActions()

        // progress is hidden
        assertThat(LiveDataTestUtil.getValue(detailsViewModel.dataLoading)).isFalse()

        // and data not loaded
        assertThat(LiveDataTestUtil.getValue(detailsViewModel.contact)).isNull()
        assertThat(LiveDataTestUtil.getValue(detailsViewModel.dataAvailable)).isFalse()
    }

    @Test
    fun editContact_setsEvent() {
        // GIVEN - initially the contact is loaded
        detailsViewModel.loadContact(testContact1.id)

        // execute pending coroutines actions
        testContext.triggerActions()

        // WHEN - edit contact
        detailsViewModel.editContact()

        // THEN - verify that the edit contact event is triggered
        assertLiveDataEventTriggered(detailsViewModel.editContactEvent, testContact1.id)
    }

    @Test
    fun deleteContact_success_loadingTogglesAndSetsEvent() = runBlocking {
        // GIVEN - initially contact is loaded
        detailsViewModel.loadContact(testContact1.id)

        // execute pending coroutines actions
        testContext.triggerActions()

        // WHEN - deleting contact
        detailsViewModel.deleteContact()

        // THEN - verify that following events occur
        // progress is shown
        assertThat(LiveDataTestUtil.getValue(detailsViewModel.dataLoading)).isTrue()

        // execute pending coroutines actions
        testContext.triggerActions()

        // progress is hidden
        assertThat(LiveDataTestUtil.getValue(detailsViewModel.dataLoading)).isFalse()

        // and data loaded correctly
        assertLiveDataEventTriggered(detailsViewModel.contactDeletedEvent, Unit)
        assertLiveDataEventTriggered(detailsViewModel.showMessageEvent, R.string.deleted_contact)
    }

    @Test
    fun deleteContact_error_loadingTogglesAndSetsEvent() = runBlocking {
        // initially contact is loaded
        detailsViewModel.loadContact(testContact1.id)

        // execute pending coroutines actions
        testContext.triggerActions()

        // GIVEN - repository returns error
        repository.setShouldReturnError(true)

        // WHEN - deleting contact
        detailsViewModel.deleteContact()

        // THEN - verify that following events occur
        // progress is shown
        assertThat(LiveDataTestUtil.getValue(detailsViewModel.dataLoading)).isTrue()

        // execute pending coroutines actions
        testContext.triggerActions()

        // progress is hidden
        assertThat(LiveDataTestUtil.getValue(detailsViewModel.dataLoading)).isFalse()

        // and data loaded correctly
        assertLiveDataEventTriggered(detailsViewModel.showMessageEvent, R.string.error_delete_contact)
    }

}