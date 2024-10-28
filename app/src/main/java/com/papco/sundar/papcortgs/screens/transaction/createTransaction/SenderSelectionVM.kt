package com.papco.sundar.papcortgs.screens.transaction.createTransaction

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.papco.sundar.papcortgs.database.common.MasterDatabase
import com.papco.sundar.papcortgs.database.pojo.Party
import com.papco.sundar.papcortgs.database.sender.Sender
import com.papco.sundar.papcortgs.ui.screens.party.SearchablePartyListState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class SenderSelectionVM(application: Application) : AndroidViewModel(application) {

    private val db=MasterDatabase.getInstance(application)
    val screenState = SearchablePartyListState()
    val senders: Flow<List<Sender>> = db.senderDao.allSenders

    init {
        loadSenders()
    }

    private fun loadSenders(){
        viewModelScope.launch(Dispatchers.IO) {
            db.senderDao.allSenders
                .combine(screenState.query){senders,query->
                    senders.filter {
                        if(query.isBlank())
                            true
                        else
                            it.displayName.contains(query,ignoreCase = true)
                    }.map {
                        Party(
                            id=it.id,
                            name = it.displayName,
                            highlightWord = query
                        )
                    }
                }.collect{
                    screenState.data=it
                }
        }
    }

}
