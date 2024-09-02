package com.papco.sundar.papcortgs.screens.transaction.createTransaction

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.papco.sundar.papcortgs.database.common.MasterDatabase
import com.papco.sundar.papcortgs.database.sender.Sender

class SenderSelectionVM(application: Application) : AndroidViewModel(application) {

    val senders: LiveData<List<Sender>>
    private val db=MasterDatabase.getInstance(application)
    init {
        senders = db.getSenderDao().getAllSenders()
    }
}
