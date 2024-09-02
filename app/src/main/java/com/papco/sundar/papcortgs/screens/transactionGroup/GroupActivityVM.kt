package com.papco.sundar.papcortgs.screens.transactionGroup

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.papco.sundar.papcortgs.database.common.MasterDatabase
import com.papco.sundar.papcortgs.database.common.TableOperation
import com.papco.sundar.papcortgs.database.transactionGroup.GroupTableWorker
import com.papco.sundar.papcortgs.database.transactionGroup.TransactionGroup
import com.papco.sundar.papcortgs.database.transactionGroup.TransactionGroupListItem
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class GroupActivityVM(application: Application) : AndroidViewModel(application) {

    @JvmField
    val groups: LiveData<List<TransactionGroupListItem>>
    val db: MasterDatabase = MasterDatabase.getInstance(getApplication())

    @JvmField
    var editingGroup: TransactionGroup? = null

    init {
        groups = db.transactionGroupDao.allTransactionGroupsForList
    }

    fun addTransactionGroup(newGroup: TransactionGroup?) {
        GroupTableWorker(getApplication(), TableOperation.CREATE, null).execute(newGroup)
    }

    fun updateTransactionGroup(updated: TransactionGroup?) {
        GroupTableWorker(getApplication(), TableOperation.UPDATE, null).execute(updated)
    }

    fun deleteTransactionGroup(toDel: TransactionGroup?) {
        GroupTableWorker(getApplication(), TableOperation.DELETE, null).execute(toDel)
    }




}

