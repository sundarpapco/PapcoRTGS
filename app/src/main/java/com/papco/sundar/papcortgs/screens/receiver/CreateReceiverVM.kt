package com.papco.sundar.papcortgs.screens.receiver

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.common.Event
import com.papco.sundar.papcortgs.database.common.MasterDatabase
import com.papco.sundar.papcortgs.database.receiver.Receiver
import com.papco.sundar.papcortgs.ui.screens.party.AddEditPartyState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.*

class CreateReceiverVM(application: Application) :AndroidViewModel(application) {

    private var isAlreadyLoaded = false

    val screenState= AddEditPartyState(application)
    private val _eventStatus: MutableStateFlow<Event<String>?> = MutableStateFlow(null)
    val eventStatus: Flow<Event<String>?> = _eventStatus

    private val db = MasterDatabase.getInstance(application)


    fun loadReceiver(receiverId: Int) {

        if (isAlreadyLoaded)
            return
        else
            isAlreadyLoaded = true

        viewModelScope.launch(Dispatchers.IO) {
            screenState.isWaiting=true
            val receiver = db.receiverDao.getReceiver(receiverId)
            screenState.loadReceiver(receiver)
            screenState.isWaiting=false
        }

    }

    fun addReceiver(newReceiver: Receiver) {
        viewModelScope.launch(Dispatchers.IO) {
            screenState.isWaiting=true
            if (isNameAlreadyExists(newReceiver.displayName)) {
                screenState.isWaiting=false
                _eventStatus.value=Event(getDuplicateString())
            } else {
                db.receiverDao.addReceiver(newReceiver)
                _eventStatus.value=Event(CreateReceiverFragment.EVENT_SUCCESS)
            }
        }

    }

    fun updateReceiver(receiverToUpdate: Receiver) {
        viewModelScope.launch(Dispatchers.IO) {
            screenState.isWaiting=true
            if (isNameAlreadyExistsExceptCurrent(receiverToUpdate)) {
                screenState.isWaiting=false
                _eventStatus.value=Event(getDuplicateString())
            } else {
                db.receiverDao.updateReceiver(receiverToUpdate)
                _eventStatus.value=Event(CreateReceiverFragment.EVENT_SUCCESS)
            }
        }
    }

    private fun isNameAlreadyExists(givenName: String): Boolean {

        val newNameInLowerCase = givenName.lowercase(Locale.getDefault())
        var nameInLowerCase: String
        val receiverNames = db.receiverDao.allReceiverDisplayNames
        for (receiverName in receiverNames) {
            nameInLowerCase = receiverName.lowercase(Locale.getDefault())
            if (newNameInLowerCase == nameInLowerCase) return true
        }
        return false
    }

    private fun isNameAlreadyExistsExceptCurrent(updatingReceiver: Receiver): Boolean {

        val newNameInLowerCase = updatingReceiver.displayName.lowercase(Locale.getDefault())
        var nameInLowerCase: String
        val receivers = db.receiverDao.allReceiversNonLive
        for (receiver in receivers) {
            if (receiver.id != updatingReceiver.id) {
                nameInLowerCase = receiver.displayName.lowercase(Locale.getDefault())
                if (newNameInLowerCase == nameInLowerCase) return true
            }
        }
        return false
    }

    private fun getDuplicateString(): String =
            getApplication<Application>().getString(R.string.receiver_name_already_exist)

}