package com.papco.sundar.papcortgs.screens.transactionGroup.manage

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.database.common.MasterDatabase
import com.papco.sundar.papcortgs.database.pojo.Party
import com.papco.sundar.papcortgs.ui.screens.group.ManageGroupScreenState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManageTransactionGroupVM(
    application: Application,
    val groupId: Int
) : ViewModel() {

    companion object {
        fun factory(application: Application, groupId: Int): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(ManageTransactionGroupVM::class.java))
                        return ManageTransactionGroupVM(application, groupId) as T
                    else
                        error("Unknown ViewModel Class")
                }
            }
        }
    }

    private val db = MasterDatabase.getInstance(application)
    private val _sendersList = db.senderDao.allSenders
        .map {
            it.map { sender -> Party(id = sender.id, name = sender.displayName, highlightWord = "") }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val screen = ManageGroupScreenState(
        titleResource = if (groupId == -1) R.string.create_xl_file else R.string.update_xl_file,
        sendersList = _sendersList
    )

    init {
        if (groupId != -1)
            loadTransactionGroup(groupId)
    }

    fun loadTransactionGroup(groupId: Int) {

        viewModelScope.launch(Dispatchers.IO) {
            val group = db.transactionGroupDao.getTransactionGroupListItem(groupId)
            withContext(Dispatchers.Main) {
                screen.loadGroup(group)
            }
        }

    }

    fun onSave() {

        viewModelScope.launch(Dispatchers.IO) {
            screen.getLoadedGroup()?.let {
                if (groupId != -1)
                    db.transactionGroupDao.updateTransactionGroup(it)
                else
                    db.transactionGroupDao.addTransactionGroup(it)
                screen.popUpBackStack()
            }
        }
    }

    fun deleteGroup(groupId: Int) {

        viewModelScope.launch(Dispatchers.IO) {
            screen.showWaitDialog()
            db.transactionGroupDao.deleteTransactionGroup(groupId)
            screen.dismissDialog()
            screen.popUpBackStack()
        }

    }

}