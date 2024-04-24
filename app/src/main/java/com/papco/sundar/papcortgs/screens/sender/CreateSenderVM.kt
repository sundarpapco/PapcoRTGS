package com.papco.sundar.papcortgs.screens.sender

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.common.Event
import com.papco.sundar.papcortgs.database.common.MasterDatabase
import com.papco.sundar.papcortgs.database.sender.Sender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class CreateSenderVM(application: Application) : AndroidViewModel(application) {

    private var isAlreadyLoaded = false

    private val _sender = MutableLiveData<Event<Sender>>()
    private val _eventStatus = MutableLiveData<Event<String>>()

    val sender = _sender
    val eventStatus = _eventStatus
    private val db = MasterDatabase.getInstance(application)


    fun loadSender(senderId: Int) {

        if (isAlreadyLoaded)
            return
        else
            isAlreadyLoaded = true

        viewModelScope.launch(Dispatchers.IO) {
            val sender = db.senderDao.getSender(senderId)
            _sender.postValue(Event(sender))
        }

    }

    fun addSender(newSender: Sender) {
        viewModelScope.launch(Dispatchers.IO) {
            if (isNameAlreadyExists(newSender.displayName)) {
                _eventStatus.postValue(Event(getDuplicateString()))
            } else {
                db.senderDao.addSender(newSender)
                _eventStatus.postValue(Event(CreateSenderFragment.EVENT_SUCCESS))
            }
        }

    }

    fun updateSender(senderToUpdate: Sender) {
        viewModelScope.launch(Dispatchers.IO) {

            if (isNameAlreadyExistsExceptCurrent(senderToUpdate)) {
                _eventStatus.postValue(Event(getDuplicateString()))
            } else {
                db.senderDao.updateSender(senderToUpdate)
                _eventStatus.postValue(Event(CreateSenderFragment.EVENT_SUCCESS))
            }
        }
    }

    private fun isNameAlreadyExists(givenName: String): Boolean {

        val newNameInLowerCase = givenName.lowercase(Locale.getDefault())
        var nameInLowerCase: String
        val senderNames = db.senderDao.allSenderNames
        for (senderName in senderNames) {
            nameInLowerCase = senderName.lowercase(Locale.getDefault())
            if (newNameInLowerCase == nameInLowerCase) return true
        }
        return false
    }

    private fun isNameAlreadyExistsExceptCurrent(updatingSender: Sender): Boolean {

        val newNameInLowerCase = updatingSender.displayName.lowercase(Locale.getDefault())
        var nameInLowerCase: String
        val senders = db.senderDao.allSendersNonLive
        for (sender in senders) {
            if (sender.id != updatingSender.id) {
                nameInLowerCase = sender.displayName.lowercase(Locale.getDefault())
                if (newNameInLowerCase == nameInLowerCase) return true
            }
        }
        return false
    }

    private fun getDuplicateString(): String =
            getApplication<Application>().getString(R.string.sender_name_already_exist)


}