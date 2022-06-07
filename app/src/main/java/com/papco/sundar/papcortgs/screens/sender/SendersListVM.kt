package com.papco.sundar.papcortgs.screens.sender

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.papco.sundar.papcortgs.database.common.MasterDatabase
import com.papco.sundar.papcortgs.database.sender.Sender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SendersListVM(application: Application) : AndroidViewModel(application) {

    val sendersList:LiveData<List<Sender>>
    val db:MasterDatabase = MasterDatabase.getInstance(application)

    init {
        sendersList=db.senderDao.allSenders
    }

    fun deleteSender(senderToDelete : Sender){
        viewModelScope.launch(Dispatchers.IO) {
            db.senderDao.deleteSender(senderToDelete)
        }
    }

}