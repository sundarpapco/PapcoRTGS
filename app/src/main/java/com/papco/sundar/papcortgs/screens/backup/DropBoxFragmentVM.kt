package com.papco.sundar.papcortgs.screens.backup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.payroll.dropbox.DropBoxAppConfig
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.database.common.MasterDatabase
import com.papco.sundar.papcortgs.dropbox.DropBox
import com.papco.sundar.papcortgs.settings.AppPreferences
import com.papco.sundar.papcortgs.ui.backup.BackupScreenState
import com.papco.sundar.papcortgs.ui.components.ToastMessage
import com.papco.sundar.papcortgs.ui.components.toastMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class DropBoxFragmentVM(application: Application) : AndroidViewModel(application) {

    private val appPreferences = AppPreferences(application)
    private val dropBox = DropBox(application, appPreferences, DropBoxAppConfig())
    val screenState = BackupScreenState()
    private val backupManager = BackupManager(
        MasterDatabase.getInstance(application),
        AppPreferences((application)),
        dropBox
    )

    init {
        initialize()
    }

    private fun initialize() {

        viewModelScope.launch(Dispatchers.IO) {
            dropBox.connectionStatus()
                .collect { connected ->
                    screenState.isDropBoxConnected = connected
                }
        }

        viewModelScope.launch(Dispatchers.IO) {
            dropBox.loggedInAccount()
                .collect {
                    screenState.account = it
                }
        }
    }

    fun refreshDropBoxConnection() {
        viewModelScope.launch {
            dropBox.refreshConnection()
        }
    }

    fun linkToDropBox() {
        viewModelScope.launch {
            dropBox.tryToConnect()
        }
    }

    fun unlinkFromDropBox() {
        viewModelScope.launch {
            dropBox.disConnect()
        }
    }

    fun backupFile() {
        viewModelScope.launch {
            try {
                screenState.showProgressDialog(BackupUpdate.Progress(ToastMessage.Message("")))
                backupManager
                    .doDropBoxBackup()
                    .flowOn(Dispatchers.IO)
                    .collect {
                        when (it) {
                            is BackupUpdate.Progress -> {
                                screenState.showProgressDialog(it)
                            }

                            is BackupUpdate.Success -> {
                                screenState.hideDialog()
                                screenState.toast(ToastMessage.Resource(R.string.backup_complete))
                            }

                            is BackupUpdate.Failed -> {
                                screenState.hideDialog()
                                screenState.toast(it.error.toastMessage())
                            }
                        }
                    }
            } catch (e: Exception) {
                screenState.toast(e.toastMessage())
            }
        }

    }

    fun restoreBackup() {
        viewModelScope.launch {

            screenState.showProgressDialog(BackupUpdate.Progress(ToastMessage.Message("")))
            backupManager
                .restoreDropBoxBackup()
                .flowOn(Dispatchers.IO)
                .collect {

                    when (it) {
                        is BackupUpdate.Progress -> {
                            screenState.showProgressDialog(it)
                        }

                        is BackupUpdate.Success -> {
                            screenState.hideDialog()
                            screenState.toast(ToastMessage.Resource(R.string.restore_success))
                        }

                        is BackupUpdate.Failed -> {
                            screenState.hideDialog()
                            screenState.toast(it.error.toastMessage())
                        }
                    }
                }

        }
    }
}