package com.papco.sundar.papcortgs.screens.transaction.createTransaction

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.papco.sundar.papcortgs.database.common.MasterDatabase
import com.papco.sundar.papcortgs.database.pojo.Party
import com.papco.sundar.papcortgs.ui.screens.party.SearchablePartyListState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class SenderSelectionVM(application: Application) : AndroidViewModel(application) {

    private val db=MasterDatabase.getInstance(application)
    val screenState = SearchablePartyListState()

    init {
        loadSenders()
    }

    private fun loadSenders(){
        viewModelScope.launch(Dispatchers.IO) {
            db.senderDao.allSenders
                .combine(screenState.query){senders,query->
                    val trimmedQuery = query.trim()
                    val filteredList = if(trimmedQuery.isBlank())
                        senders
                    else
                        senders.filter { it.displayName.contains(trimmedQuery,true) }
                    filteredList.map {
                        Party(it.id,it.displayName,trimmedQuery)
                    }
                }
                .collect{
                    screenState.data=it
                }
        }
    }

}
