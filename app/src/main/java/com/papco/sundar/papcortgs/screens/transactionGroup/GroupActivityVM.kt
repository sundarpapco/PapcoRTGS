package com.papco.sundar.papcortgs.screens.transactionGroup

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.payroll.dropbox.DropBoxAppConfig
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.database.common.MasterDatabase
import com.papco.sundar.papcortgs.dropbox.DropBox
import com.papco.sundar.papcortgs.screens.backup.BackupManager
import com.papco.sundar.papcortgs.screens.backup.BackupUpdate
import com.papco.sundar.papcortgs.settings.AppPreferences
import com.papco.sundar.papcortgs.ui.screens.group.ExcelFileListScreenState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GroupActivityVM(application: Application) : AndroidViewModel(application) {



    val db: MasterDatabase = MasterDatabase.getInstance(getApplication())
    private val appPreferences = AppPreferences(application)
    private val dropBox = DropBox(application, appPreferences, DropBoxAppConfig())
    private val backupManager = BackupManager(db, appPreferences, dropBox)

    private val _excelFiles = db.transactionGroupDao.allTransactionGroupsForList()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val screenState = ExcelFileListScreenState(_excelFiles)

    fun createBackupFile() {
        viewModelScope.launch {
            backupManager.createBackupFile()
                .flowOn(Dispatchers.IO)
                .collect {
                    when (it) {
                        is BackupUpdate.Progress -> {
                            screenState.updateBackupProgress(it.progress)
                        }

                        is BackupUpdate.Success -> {
                            screenState.showBackupSharingDialog(appPreferences.getLocalBackupFilePath())
                        }

                        is BackupUpdate.Failed -> {
                            screenState.hideDialog()
                            screenState.toastError(it.error)
                        }
                    }
                }
        }
    }

    fun restoreBackupFile(uri: Uri){

        viewModelScope.launch {
            backupManager.restoreFromFile(getApplication(),uri)
                .flowOn(Dispatchers.IO)
                .collect {
                    when (it) {
                        is BackupUpdate.Progress -> {
                            screenState.updateBackupProgress(it.progress)
                        }

                        is BackupUpdate.Success -> {
                            screenState.hideDialog()
                            screenState.toastResource(R.string.restore_success)
                        }

                        is BackupUpdate.Failed -> {
                            screenState.hideDialog()
                            screenState.toastError(it.error)
                        }
                    }
                }
        }
    }

    fun clearAllPayments(){
        viewModelScope.launch {
            try {
                screenState.showWaitDialog()
                db.transactionGroupDao.deleteAllTransactionGroups()
            } catch (e: Exception) {
                screenState.toastError(e)
            } finally {
                screenState.hideDialog()
            }
        }
    }

}

