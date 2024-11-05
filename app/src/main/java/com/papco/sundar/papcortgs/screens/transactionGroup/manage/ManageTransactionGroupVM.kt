package com.papco.sundar.papcortgs.screens.transactionGroup.manage

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.papco.sundar.papcortgs.common.Event
import com.papco.sundar.papcortgs.database.common.MasterDatabase
import com.papco.sundar.papcortgs.database.pojo.Party
import com.papco.sundar.papcortgs.ui.screens.group.ManageGroupScreenState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManageTransactionGroupVM(application: Application) : AndroidViewModel(application) {

    private val db=MasterDatabase.getInstance(getApplication())
    private var isAlreadyLoaded=false
    val screenState = ManageGroupScreenState()

    private var _event: MutableStateFlow<Event<String>?> = MutableStateFlow(null)
    val event:Flow<Event<String>?> = _event
    init{
        loadSenders()
    }

    fun loadTransactionGroup(groupId:Int){

        if(isAlreadyLoaded)
            return
        else
            isAlreadyLoaded=true

        viewModelScope.launch(Dispatchers.IO) {
            val group=db.transactionGroupDao.getTransactionGroupListItem(groupId)
            withContext(Dispatchers.Main){
                screenState.loadGroup(group)
            }
        }

    }

    private fun loadSenders(){
        viewModelScope.launch(Dispatchers.Main) {
            db.senderDao.allSenders
                .map {
                    it.map {sender->
                        Party(
                            id=sender.id,
                            name = sender.displayName,
                            highlightWord = ""
                        )
                    }
                }.flowOn(Dispatchers.IO)
                .collect{
                    screenState.loadSendersList(it)
                }
        }
    }

    fun addGroup(){

        viewModelScope.launch(Dispatchers.IO) {
            screenState.getLoadedGroup()?.let{
                db.transactionGroupDao.addTransactionGroup(it)
                _event.value= Event("Success")
            }
        }
    }

    fun updateGroup(){

        viewModelScope.launch(Dispatchers.IO) {
            screenState.getLoadedGroup()?.let{
                db.transactionGroupDao.updateTransactionGroup(it)
                _event.value= Event("Success")
            }
        }

    }

    fun deleteGroup(groupId:Int){

        viewModelScope.launch(Dispatchers.IO){
            screenState.dialog=ManageGroupScreenState.Dialog.WaitDialog
            db.transactionGroupDao.deleteTransactionGroup(groupId)
            _event.value= Event("Success")
        }

    }

}