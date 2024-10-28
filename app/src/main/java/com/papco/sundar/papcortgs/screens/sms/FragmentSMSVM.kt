package com.papco.sundar.papcortgs.screens.sms

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.papco.sundar.papcortgs.database.common.MasterDatabase
import com.papco.sundar.papcortgs.ui.screens.message.MessageScreenState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FragmentSMSVM(application: Application) : AndroidViewModel(application) {

    private val db = MasterDatabase.getInstance(application)
    val screenState = MessageScreenState()
    private var alreadyLoaded: Boolean = false

    fun loadMessagingList(groupId: Int) {

        if (alreadyLoaded) return else alreadyLoaded = true

        viewModelScope.launch(Dispatchers.IO) {
            db.transactionDao
                .getAllCohesiveTransactionsOfGroup(groupId)
                .collect {
                    screenState.transactions=it
                }
        }

        viewModelScope.launch {
            MessageWorker.getWorkStatusFlow(getApplication(), groupId)
                .collect{
                    if(it.isNotEmpty())
                        screenState.dispatcherState=it.first().state
                }
        }
    }
}
