package practice.roommigrations.ui.details

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

class DetailsViewModel(private val repository: ContactsRepository) : ViewModel() {

    private val _contact = MutableLiveData<Contact>()
    val contact: LiveData<Contact> = _contact

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _dataAvailable = MutableLiveData<Boolean>()
    val dataAvailable: LiveData<Boolean> = _dataAvailable

    private val _showMessageEvent = MutableLiveData<Event<Int>>()
    val showMessageEvent: LiveData<Event<Int>> = _showMessageEvent

    private val _editContactEvent = MutableLiveData<Event<String>>()
    val editContactEvent: LiveData<Event<String>> = _editContactEvent

    private val _contactDeletedEvent = MutableLiveData<Event<Unit>>()
    val contactDeletedEvent: LiveData<Event<Unit>> = _contactDeletedEvent

    private lateinit var contactId: String

    fun loadContact(contactId: String) {
        this.contactId = contactId
        _dataLoading.value = true
        viewModelScope.launch {
            val result = repository.getContact(contactId)
            if (result is Success) {
                _contact.value = result.data
                _dataAvailable.value = true
            } else {
                _contact.value = null
                _dataAvailable.value = false
            }
            _dataLoading.value = false
        }
    }

    fun deleteContact() {
        _dataLoading.value = true
        viewModelScope.launch {
            val result = repository.deleteContact(contactId)
            if (result is Success) {
                _contactDeletedEvent.value = Event(Unit)
                _showMessageEvent.value = Event(R.string.deleted_contact)
            } else {
                _showMessageEvent.value = Event(R.string.error_delete_contact)
            }
            _dataLoading.value = false
        }
    }

    fun editContact() {
        _editContactEvent.value = Event(contactId)
    }

}