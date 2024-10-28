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

class ReceiverSelectionVM(application: Application) : AndroidViewModel(application) {

    private val db = MasterDatabase.getInstance(application)
    val screenState = SearchablePartyListState()
    private var isAlreadyLoaded =false

    fun loadReceivers(groupId: Int){
        if(isAlreadyLoaded){
            isAlreadyLoaded=true
            return
        }

        viewModelScope.launch(Dispatchers.IO) {

            val receiversIdsAlreadyInGroup = db.transactionDao.getReceiverIdsOfGroup(groupId)

            db.receiverDao.allReceivers
                .combine(screenState.query){receivers,query->
                    receivers
                        .filter {
                            if(query.isBlank())
                                true
                            else
                                it.displayName.contains(query,true)
                        }.map {
                            Party(
                                id = it.id,
                                name = it.displayName,
                                highlightWord = query,
                                disabled = receiversIdsAlreadyInGroup.contains(it.id)
                            )
                        }
                }.collect{
                    screenState.data=it
                }
        }
    }
}
