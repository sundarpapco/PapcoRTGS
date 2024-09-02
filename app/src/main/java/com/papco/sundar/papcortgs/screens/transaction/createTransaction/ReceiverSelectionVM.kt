package com.papco.sundar.papcortgs.screens.transaction.createTransaction

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.papco.sundar.papcortgs.database.common.MasterDatabase
import com.papco.sundar.papcortgs.database.receiver.Receiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReceiverSelectionVM(application: Application) : AndroidViewModel(application) {

    private val db = MasterDatabase.getInstance(application)
    private val receiversList = MutableLiveData<List<Receiver>>()
    fun loadReceivers(groupId: Int) {

        viewModelScope.launch(Dispatchers.IO) {
            val receivers = db.receiverDao.allReceiversNonLive
            val receiversIdsAlreadyInGroup = db.transactionDao.getReceiverIdsOfGroup(groupId)
            receiversIdsAlreadyInGroup.forEach { id ->
                receivers.forEach { receiver ->
                    if (receiver.id == id) receiver.accountNumber = "-1"
                }
            }

            withContext(Dispatchers.Main) {
                receiversList.value = receivers
            }
        }
    }

    val receivers: LiveData<List<Receiver>>
        get() = receiversList
}
