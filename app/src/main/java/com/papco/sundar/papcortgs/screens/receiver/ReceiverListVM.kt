package com.papco.sundar.papcortgs.screens.receiver

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.papco.sundar.papcortgs.database.common.MasterDatabase
import com.papco.sundar.papcortgs.database.receiver.Receiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReceiverListVM(application: Application):AndroidViewModel(application) {

    private val db=MasterDatabase.getInstance(getApplication())
    val receiver:LiveData<List<Receiver>> = db.receiverDao.allReceivers

    fun deleteReceiver(receiverToDelete:Receiver){
        viewModelScope.launch(Dispatchers.IO) {
            db.receiverDao.deleteReceiver(receiverToDelete)
        }
    }

}