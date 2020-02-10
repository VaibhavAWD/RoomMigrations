package practice.roommigrations.ui.contacts

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

class ContactsViewModel(private val repository: ContactsRepository) : ViewModel() {

    private val _contacts = MutableLiveData<List<Contact>>()
    val contacts: LiveData<List<Contact>> = _contacts

    private val _dataLoading = MutableLiveData<Boolean>()
    val dataLoading: LiveData<Boolean> = _dataLoading

    private val _showMessageEvent = MutableLiveData<Event<Int>>()
    val showMessageEvent: LiveData<Event<Int>> = _showMessageEvent

    private val _deleteAllContactsAlertEvent = MutableLiveData<Event<Unit>>()
    val deleteAllContactsAlertEvent: LiveData<Event<Unit>> = _deleteAllContactsAlertEvent

    private val _addNewContactEvent = MutableLiveData<Event<Unit>>()
    val addNewContactEvent: LiveData<Event<Unit>> = _addNewContactEvent

    private val _openContactEvent = MutableLiveData<Event<String>>()
    val openContactEvent: LiveData<Event<String>> = _openContactEvent

    fun loadContacts() {
        _dataLoading.value = true
        viewModelScope.launch {
            val result = repository.getContacts()
            if (result is Success) {
                _contacts.value = result.data
            } else {
                _contacts.value = emptyList()
                _showMessageEvent.value = Event(R.string.error_loading_contacts)
            }
            _dataLoading.value = false
        }
    }

    fun showDeleteAllContactsAlert() {
        _deleteAllContactsAlertEvent.value = Event(Unit)
    }

    fun deleteAllContacts() {
        _dataLoading.value = true
        viewModelScope.launch {
            val result = repository.deleteAllContacts()
            if (result is Success) {
                loadContacts()
                _showMessageEvent.value = Event(R.string.all_contacts_deleted)
            } else {
                _showMessageEvent.value = Event(R.string.error_deleting_all_contacts)
            }
            _dataLoading.value = false
        }
    }

    fun addNewContact() {
        _addNewContactEvent.value = Event(Unit)
    }

    fun openContact(contactId: String) {
        _openContactEvent.value = Event(contactId)
    }

}