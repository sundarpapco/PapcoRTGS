package com.papco.sundar.papcortgs.screens.transactionGroup

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.papco.sundar.papcortgs.database.common.MasterDatabase
import com.papco.sundar.papcortgs.database.common.TableOperation
import com.papco.sundar.papcortgs.database.transactionGroup.GroupTableWorker
import com.papco.sundar.papcortgs.database.transactionGroup.TransactionGroup
import com.papco.sundar.papcortgs.database.transactionGroup.TransactionGroupListItem
import com.papco.sundar.papcortgs.ui.screens.group.ExcelFileListScreenState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class GroupActivityVM(application: Application) : AndroidViewModel(application) {


    val screenState=ExcelFileListScreenState()
    val db: MasterDatabase = MasterDatabase.getInstance(getApplication())

    init {
        loadExcelFiles()
    }

    private fun loadExcelFiles(){
        viewModelScope.launch(Dispatchers.IO) {
            db.transactionGroupDao.allTransactionGroupsForList
                .collect{
                    screenState.list=it
                }
        }
    }

}

