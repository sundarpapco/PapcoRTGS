package com.papco.sundar.papcortgs.screens.sender

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.common.Event
import com.papco.sundar.papcortgs.database.common.MasterDatabase
import com.papco.sundar.papcortgs.database.sender.Sender
import com.papco.sundar.papcortgs.ui.screens.party.AddEditPartyState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class CreateSenderVM(application: Application) : AndroidViewModel(application) {

    companion object {
        const val EVENT_SUCCESS = "EVENT_SUCCESS"
    }

    private var isAlreadyLoaded = false

    val screenState=AddEditPartyState(application)
    private val _eventStatus: MutableStateFlow<Event<String>?> = MutableStateFlow(null)
    val eventStatus:Flow<Event<String>?> = _eventStatus

    private val db = MasterDatabase.getInstance(application)


    fun loadSender(senderId: Int) {

        if (isAlreadyLoaded)
            return
        else
            isAlreadyLoaded = true

        viewModelScope.launch(Dispatchers.IO) {
            screenState.isWaiting=true
            val sender = db.senderDao.getSender(senderId)
            screenState.loadSender(sender)
            screenState.isWaiting=false
        }

    }

    fun addSender(newSender: Sender) {
        viewModelScope.launch(Dispatchers.IO) {
            screenState.isWaiting=true
            if (isNameAlreadyExists(newSender.displayName)) {
                screenState.isWaiting=false
                _eventStatus.value=Event(getDuplicateString())
            } else {
                db.senderDao.addSender(newSender)
                _eventStatus.value=Event(EVENT_SUCCESS)
            }
        }

    }

    fun updateSender(senderToUpdate: Sender) {
        viewModelScope.launch(Dispatchers.IO) {
            screenState.isWaiting=true
            if (isNameAlreadyExistsExceptCurrent(senderToUpdate)) {
                screenState.isWaiting=false
                _eventStatus.value=Event(getDuplicateString())
            } else {
                db.senderDao.updateSender(senderToUpdate)
                _eventStatus.value=Event(EVENT_SUCCESS)
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