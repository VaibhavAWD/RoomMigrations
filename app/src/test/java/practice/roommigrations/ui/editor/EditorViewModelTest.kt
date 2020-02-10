@file:Suppress("DEPRECATION")

package practice.roommigrations.ui.editor

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
 * Tests for implementation of [EditorViewModel].
 */
class EditorViewModelTest {

    // SUT
    private lateinit var editorViewModel: EditorViewModel

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
    private val testNewContact = Contact("Test New Contact", "1234567899")
    private val testInvalidMobile = "invalid_mobile_number"

    @Before
    fun setUp() {
        repository = FakeContactsRepository()
        repository.addContacts(testContact1, testContact2)
        editorViewModel = EditorViewModel(repository)
    }

    @Test
    fun loadContact_success_loadingTogglesAndDataLoaded() = runBlocking {
        // WHEN - loading contact
        editorViewModel.loadContact(testContact1.id)

        // THEN - verify that following events occur
        // progress is shown
        assertThat(LiveDataTestUtil.getValue(editorViewModel.dataLoading)).isTrue()

        // execute pending coroutines actions
        testContext.triggerActions()

        // progress is hidden
        assertThat(LiveDataTestUtil.getValue(editorViewModel.dataLoading)).isFalse()

        // and data loaded correctly
        assertThat(LiveDataTestUtil.getValue(editorViewModel.contact)).isEqualTo(testContact1)
        assertThat(LiveDataTestUtil.getValue(editorViewModel.name)).isEqualTo(testContact1.name)
        assertThat(LiveDataTestUtil.getValue(editorViewModel.mobile)).isEqualTo(testContact1.mobile)
        assertThat(LiveDataTestUtil.getValue(editorViewModel.dataAvailable)).isTrue()
    }

    @Test
    fun loadContact_error_loadingTogglesAndDataLoaded() = runBlocking {
        // GIVEN - repository returns error
        repository.setShouldReturnError(true)

        // WHEN - loading contact
        editorViewModel.loadContact(testContact1.id)

        // THEN - verify that following events occur
        // progress is shown
        assertThat(LiveDataTestUtil.getValue(editorViewModel.dataLoading)).isTrue()

        // execute pending coroutines actions
        testContext.triggerActions()

        // progress is hidden
        assertThat(LiveDataTestUtil.getValue(editorViewModel.dataLoading)).isFalse()

        // and data not loaded
        assertThat(LiveDataTestUtil.getValue(editorViewModel.contact)).isNull()
        assertThat(LiveDataTestUtil.getValue(editorViewModel.name)).isNull()
        assertThat(LiveDataTestUtil.getValue(editorViewModel.mobile)).isNull()
        assertThat(LiveDataTestUtil.getValue(editorViewModel.dataAvailable)).isFalse()
    }

    @Test
    fun saveContact_emptyName_errorAndSetsEvent() {
        // GIVEN - name is empty
        editorViewModel.name.value = ""

        // WHEN - saving contact
        editorViewModel.saveContact()

        // THEN - verify that the show message event is triggered
        assertLiveDataEventTriggered(editorViewModel.showMessageEvent, R.string.error_empty_name)
    }

    @Test
    fun saveContact_emptyMobile_errorAndSetsEvent() {
        // GIVEN - mobile is empty
        editorViewModel.name.value = testNewContact.name
        editorViewModel.mobile.value = ""

        // WHEN - saving contact
        editorViewModel.saveContact()

        // THEN - verify that the show message event is triggered
        assertLiveDataEventTriggered(editorViewModel.showMessageEvent, R.string.error_empty_mobile)
    }

    @Test
    fun saveContact_invalidMobile_errorAndSetsEvent() {
        // GIVEN - mobile is empty
        editorViewModel.name.value = testNewContact.name
        editorViewModel.mobile.value = testInvalidMobile

        // WHEN - saving contact
        editorViewModel.saveContact()

        // THEN - verify that the show message event is triggered
        assertLiveDataEventTriggered(
            editorViewModel.showMessageEvent,
            R.string.error_invalid_mobile
        )
    }

    @Test
    fun saveContact_validData_setsCloseSoftKeyboardEvent() {
        // GIVEN - data is valid
        editorViewModel.name.value = testNewContact.name
        editorViewModel.mobile.value = testNewContact.mobile

        // WHEN - saving contact
        editorViewModel.saveContact()

        // THEN - verify that the close soft keyboard event is triggered
        assertLiveDataEventTriggered(editorViewModel.closeSoftKeyboardEvent, Unit)
    }

    @Test
    fun saveContact_newContact_loadingTogglesAndSetsEvent() = runBlocking {
        // GIVEN - initially contact is loaded with no contact id
        editorViewModel.loadContact(null)

        // execute pending coroutines actions
        testContext.triggerActions()

        // WHEN - saving new contact
        editorViewModel.name.value = testNewContact.name
        editorViewModel.mobile.value = testNewContact.mobile
        editorViewModel.saveContact()

        // THEN - verify that following events occur
        // progress is shown
        assertThat(LiveDataTestUtil.getValue(editorViewModel.dataLoading)).isTrue()

        // execute pending coroutines actions
        testContext.triggerActions()

        // progress is hidden
        assertThat(LiveDataTestUtil.getValue(editorViewModel.dataLoading)).isFalse()

        // add sets events
        assertLiveDataEventTriggered(editorViewModel.contactSavedEvent, Unit)
        assertLiveDataEventTriggered(editorViewModel.showMessageEvent, R.string.contact_saved)
    }

    @Test
    fun saveContact_updateContact_loadingTogglesAndSetsEvent() = runBlocking {
        // GIVEN - initially contact is loaded with no contact id
        editorViewModel.loadContact(testContact1.id)

        // execute pending coroutines actions
        testContext.triggerActions()

        // WHEN - saving new contact
        editorViewModel.name.value = testNewContact.name
        editorViewModel.mobile.value = testNewContact.mobile
        editorViewModel.saveContact()

        // THEN - verify that following events occur
        // progress is shown
        assertThat(LiveDataTestUtil.getValue(editorViewModel.dataLoading)).isTrue()

        // execute pending coroutines actions
        testContext.triggerActions()

        // progress is hidden
        assertThat(LiveDataTestUtil.getValue(editorViewModel.dataLoading)).isFalse()

        // add sets events
        assertLiveDataEventTriggered(editorViewModel.contactUpdatedEvent, Unit)
        assertLiveDataEventTriggered(editorViewModel.showMessageEvent, R.string.contact_saved)
    }

    @Test
    fun saveContact_updateContact_error_loadingTogglesAndSetsEvent() = runBlocking {
        // initially contact is loaded with no contact id
        editorViewModel.loadContact(testContact1.id)

        // execute pending coroutines actions
        testContext.triggerActions()

        // GIVEN - repository returns error
        repository.setShouldReturnError(true)

        // WHEN - saving new contact
        editorViewModel.name.value = testNewContact.name
        editorViewModel.mobile.value = testNewContact.mobile
        editorViewModel.saveContact()

        // THEN - verify that following events occur
        // progress is shown
        assertThat(LiveDataTestUtil.getValue(editorViewModel.dataLoading)).isTrue()

        // execute pending coroutines actions
        testContext.triggerActions()

        // progress is hidden
        assertThat(LiveDataTestUtil.getValue(editorViewModel.dataLoading)).isFalse()

        // add sets events
        assertLiveDataEventTriggered(editorViewModel.showMessageEvent, R.string.error_save_contact)
    }

}