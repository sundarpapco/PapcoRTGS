package com.papco.sundar.papcortgs.screens.transactionGroup.manage

import android.app.Application
import android.app.usage.UsageEvents
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.papco.sundar.papcortgs.common.Event
import com.papco.sundar.papcortgs.database.common.MasterDatabase
import com.papco.sundar.papcortgs.database.sender.Sender
import com.papco.sundar.papcortgs.database.transactionGroup.TransactionGroup
import com.papco.sundar.papcortgs.database.transactionGroup.TransactionGroupListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ManageTransactionGroupVM(application: Application) : AndroidViewModel(application) {

    private val db=MasterDatabase.getInstance(getApplication())
    private var isAlreadyLoaded=false

    private val _eventStatus=MutableLiveData<Event<String>>()
    private val _loadedGroup=MutableLiveData<Event<TransactionGroupListItem>>()

    val senders:LiveData<List<Sender>> = db.senderDao.allSenders
    val eventStatus:LiveData<Event<String>> = _eventStatus
    val transactionGroup:LiveData<Event<TransactionGroupListItem>> = _loadedGroup

    fun loadTransactionGroup(groupId:Int){

        if(isAlreadyLoaded)
            return
        else
            isAlreadyLoaded=true

        viewModelScope.launch(Dispatchers.IO) {
            val group=db.transactionGroupDao.getTransactionGroupListItem(groupId)
            _loadedGroup.postValue(Event(group))
        }

    }

    fun addGroup(newGroup:TransactionGroup){

        viewModelScope.launch(Dispatchers.IO) {

            try{
                db.transactionGroupDao.addTransactionGroup(newGroup)
                _eventStatus.postValue(Event(Event.SUCCESS))
            }catch (e:Exception){
                _eventStatus.postValue(Event(e.message))
            }

        }
    }

    fun updateGroup(updateGroup:TransactionGroup){

        viewModelScope.launch(Dispatchers.IO) {

            try{
                db.transactionGroupDao.updateTransactionGroup(updateGroup)
                _eventStatus.postValue(Event(Event.SUCCESS))
            }catch (e:Exception){
                _eventStatus.postValue(Event(e.message))
            }

        }

    }

    fun deleteGroup(groupId:Int){

        viewModelScope.launch(Dispatchers.IO){

            try{
                db.transactionGroupDao.deleteTransactionGroup(groupId)
                _eventStatus.postValue(Event(Event.SUCCESS))
            }catch (e:Exception){
                _eventStatus.postValue(Event(e.message))
            }

        }

    }

}