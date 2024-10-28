package com.papco.sundar.papcortgs.screens.sender

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.papco.sundar.papcortgs.database.common.MasterDatabase
import com.papco.sundar.papcortgs.database.pojo.Party
import com.papco.sundar.papcortgs.ui.screens.party.ManagePartyScreenState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SendersListVM(application: Application) : AndroidViewModel(application) {

    val screenState = ManagePartyScreenState()
    private val db=MasterDatabase.getInstance(getApplication())

    init {
        loadSenders()
    }

    fun deleteSender(senderToDelete: Party){
        screenState.showWaitDialog()
        viewModelScope.launch(Dispatchers.IO) {
            db.senderDao.deleteSenderById(senderToDelete.id)
            withContext(Dispatchers.Main) {
                screenState.hideWaitDialog()
            }
        }
    }

    private fun loadSenders(){
        viewModelScope.launch(Dispatchers.IO) {
            db.senderDao.allSenders
                .combine(screenState.query){ senders, query->
                    if(query.isBlank())
                        return@combine senders.map {
                            Party(id = it.id, name = it.displayName, highlightWord = "")
                        }

                    senders.filter { receiversToFilter ->
                        receiversToFilter.displayName.contains(query, true)
                    }.map { receiver ->
                        Party(receiver.id, receiver.displayName, highlightWord = query)
                    }

                }.collect{
                    screenState.loadData(it)
                }
        }
    }

}