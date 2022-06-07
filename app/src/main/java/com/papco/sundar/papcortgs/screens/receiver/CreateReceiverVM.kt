package com.papco.sundar.papcortgs.screens.receiver

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.common.Event
import com.papco.sundar.papcortgs.database.common.MasterDatabase
import com.papco.sundar.papcortgs.database.receiver.Receiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class CreateReceiverVM(application: Application) :AndroidViewModel(application) {

    private var isAlreadyLoaded = false

    private val _receiver = MutableLiveData<Event<Receiver>>()
    private val _eventStatus = MutableLiveData<Event<String>>()

    val receiver = _receiver
    val eventStatus = _eventStatus
    private val db = MasterDatabase.getInstance(application)


    fun loadReceiver(receiverId: Int) {

        if (isAlreadyLoaded)
            return
        else
            isAlreadyLoaded = true

        viewModelScope.launch(Dispatchers.IO) {
            val receiver = db.receiverDao.getReceiver(receiverId)
            _receiver.postValue(Event(receiver))
        }

    }

    fun addReceiver(newReceiver: Receiver) {
        viewModelScope.launch(Dispatchers.IO) {
            if (isNameAlreadyExists(newReceiver.name)) {
                _eventStatus.postValue(Event(getDuplicateString()))
            } else {
                db.receiverDao.addReceiver(newReceiver)
                _eventStatus.postValue(Event(CreateReceiverFragment.EVENT_SUCCESS))
            }
        }

    }

    fun updateReceiver(receiverToUpdate: Receiver) {
        viewModelScope.launch(Dispatchers.IO) {

            if (isNameAlreadyExistsExceptCurrent(receiverToUpdate)) {
                _eventStatus.postValue(Event(getDuplicateString()))
            } else {
                db.receiverDao.updateReceiver(receiverToUpdate)
                _eventStatus.postValue(Event(CreateReceiverFragment.EVENT_SUCCESS))
            }
        }
    }

    private fun isNameAlreadyExists(givenName: String): Boolean {

        val newNameInLowerCase = givenName.toLowerCase(Locale.getDefault())
        var nameInLowerCase: String
        val receiverNames = db.receiverDao.allReceiverNames
        for (receiverName in receiverNames) {
            nameInLowerCase = receiverName.toLowerCase(Locale.getDefault())
            if (newNameInLowerCase == nameInLowerCase) return true
        }
        return false
    }

    private fun isNameAlreadyExistsExceptCurrent(updatingReceiver: Receiver): Boolean {

        val newNameInLowerCase = updatingReceiver.name.toLowerCase(Locale.getDefault())
        var nameInLowerCase: String
        val receivers = db.receiverDao.allReceiversNonLive
        for (receiver in receivers) {
            if (receiver.id != updatingReceiver.id) {
                nameInLowerCase = receiver.name.toLowerCase(Locale.getDefault())
                if (newNameInLowerCase == nameInLowerCase) return true
            }
        }
        return false
    }

    private fun getDuplicateString(): String =
            getApplication<Application>().getString(R.string.receiver_name_already_exist)

}