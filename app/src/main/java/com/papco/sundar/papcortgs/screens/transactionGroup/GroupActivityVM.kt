package com.papco.sundar.papcortgs.screens.transactionGroup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.papco.sundar.papcortgs.database.common.MasterDatabase
import com.papco.sundar.papcortgs.ui.screens.group.ExcelFileListScreenState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class GroupActivityVM(application: Application) : AndroidViewModel(application) {


    val screenState=ExcelFileListScreenState()
    val db: MasterDatabase = MasterDatabase.getInstance(getApplication())

    init {
        loadExcelFiles()
    }

    private fun loadExcelFiles(){
        viewModelScope.launch{
            db.transactionGroupDao.allTransactionGroupsForList
                .flowOn(Dispatchers.IO)
                .collect{
                    screenState.list=it
                }
        }
    }

}

