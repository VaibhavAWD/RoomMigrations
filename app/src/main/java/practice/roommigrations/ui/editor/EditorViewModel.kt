package practice.roommigrations.ui.editor

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import practice.roommigrations.R
import practice.roommigrations.data.ContactsRepository
import practice.roommigrations.data.Result.Success
import practice.roommigrations.data.entities.Contact
import practice.roommigrations.util.Event
import java.util.regex.Pattern

class EditorViewModel(private val repository: ContactsRepository) : ViewModel() {

    // Two-way DataBinding
    val name = MutableLiveData<String>()
    val mobile = MutableLiveData<String>()

    private val _contact = MutableLiveData<Contact>()
    val contact: LiveData<Contact> = _contact

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _dataAvailable = MutableLiveData<Boolean>()
    val dataAvailable: LiveData<Boolean> = _dataAvailable

    private val _showMessageEvent = MutableLiveData<Event<Int>>()
    val showMessageEvent: LiveData<Event<Int>> = _showMessageEvent

    private val _contactSavedEvent = MutableLiveData<Event<Unit>>()
    val contactSavedEvent: LiveData<Event<Unit>> = _contactSavedEvent

    private val _contactUpdatedEvent = MutableLiveData<Event<Unit>>()
    val contactUpdatedEvent: LiveData<Event<Unit>> = _contactUpdatedEvent

    private val _closeSoftKeyboardEvent = MutableLiveData<Event<Unit>>()
    val closeSoftKeyboardEvent: LiveData<Event<Unit>> = _closeSoftKeyboardEvent

    private var isNewContact = false

    private var contactId: String? = null

    fun loadContact(contactId: String?) {
        if (contactId == null) {
            isNewContact = true
            return
        }

        this.contactId = contactId

        _dataLoading.value = true
        viewModelScope.launch {
            val result = repository.getContact(contactId)
            if (result is Success) {
                _contact.value = result.data
                name.value = _contact.value!!.name
                mobile.value = _contact.value!!.mobile
                _dataAvailable.value = true
            } else {
                _contact.value = null
                name.value = null
                mobile.value = null
                _dataAvailable.value = false
            }
            _dataLoading.value = false
        }
    }

    fun saveContact() {
        if (!hasValidData()) return
        _closeSoftKeyboardEvent.value = Event(Unit)
        if (isNewContact && contactId == null) {
            val contact = Contact(name.value!!, mobile.value!!)
            createNewContact(contact)
        } else {
            val contact = Contact(name.value!!, mobile.value!!, contactId!!)
            updateContact(contact)
        }
    }

    private fun hasValidData(): Boolean {
        var hasValidData = false

        val currentName = name.value
        val currentMobile = mobile.value

        if (currentName.isNullOrEmpty()) {
            _showMessageEvent.value = Event(R.string.error_empty_name)
        } else if (currentMobile.isNullOrEmpty()) {
            _showMessageEvent.value = Event(R.string.error_empty_mobile)
        } else if (!Pattern.compile("[0-9]+").matcher(currentMobile).matches()) {
            _showMessageEvent.value = Event(R.string.error_invalid_mobile)
        } else {
            hasValidData = true
        }

        return hasValidData
    }

    private fun createNewContact(contact: Contact) {
        _dataLoading.value = true
        viewModelScope.launch {
            repository.saveContact(contact)
            _contactSavedEvent.value = Event(Unit)
            _showMessageEvent.value = Event(R.string.contact_saved)
            _dataLoading.value = false
        }
    }

    private fun updateContact(contact: Contact) {
        _dataLoading.value = true
        viewModelScope.launch {
            val result = repository.updateContact(contact)
            if (result is Success) {
                _contactUpdatedEvent.value = Event(Unit)
                _showMessageEvent.value = Event(R.string.contact_saved)
            } else {
                _showMessageEvent.value = Event(R.string.error_save_contact)
            }
            _dataLoading.value = false
        }
    }

}