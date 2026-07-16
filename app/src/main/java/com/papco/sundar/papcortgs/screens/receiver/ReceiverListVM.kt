package com.papco.sundar.papcortgs.screens.receiver

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.papco.sundar.papcortgs.database.common.MasterDatabase
import com.papco.sundar.papcortgs.database.pojo.Party
import com.papco.sundar.papcortgs.database.pojo.filterWith
import com.papco.sundar.papcortgs.ui.screens.party.ManagePartyScreenState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReceiverListVM(application: Application) : AndroidViewModel(application) {

    val screenState = ManagePartyScreenState()
    private val db = MasterDatabase.getInstance(getApplication())

    init {
        loadReceivers()
    }

    fun deleteReceiver(receiverToDelete: Party) {
        screenState.showWaitDialog()
        viewModelScope.launch(Dispatchers.IO) {
            db.receiverDao.deleteReceiverById(receiverToDelete.id)
            withContext(Dispatchers.Main) {
                screenState.hideWaitDialog()
            }
        }
    }

    private fun loadReceivers() {
        viewModelScope.launch(Dispatchers.IO) {
            db.receiverDao.allReceivers
                .filterWith(screenState.query)
                .collect {
                    screenState.loadData(it)
                }
        }
    }

}