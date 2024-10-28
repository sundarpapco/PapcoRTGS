package com.papco.sundar.papcortgs.screens.backup

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.papco.sundar.papcortgs.dropbox.DropBox
import com.example.payroll.dropbox.DropBoxAppConfig
import com.papco.sundar.papcortgs.database.common.MasterDatabase
import com.papco.sundar.papcortgs.settings.AppPreferences
import com.papco.sundar.papcortgs.ui.backup.BackupScreenState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class DropBoxFragmentVM(application: Application): AndroidViewModel(application) {

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

    private fun initialize(){

        viewModelScope.launch(Dispatchers.IO) {
            dropBox.connectionStatus()
                .collect{connected->
                    screenState.isDropBoxConnected=connected
                }
        }

        viewModelScope.launch(Dispatchers.IO) {
            dropBox.loggedInAccount()
                .collect{
                    screenState.account=it
                }
        }
    }

    fun refreshDropBoxConnection(){
        viewModelScope.launch{
            dropBox.refreshConnection()
        }
    }

    fun linkToDropBox(){
        viewModelScope.launch {
            dropBox.tryToConnect()
        }
    }

    fun unlinkFromDropBox(){
        viewModelScope.launch {
            dropBox.disConnect()
        }
    }

    fun backupFile(){
        viewModelScope.launch {
            try{
                screenState.showProgressDialog("")
                backupManager
                    .doBackup()
                    .flowOn(Dispatchers.IO)
                    .collect{
                        screenState.showProgressDialog(it)
                    }
            }catch (e:Exception){
                Toast.makeText(
                    getApplication(),
                    e.message ?: "Unknown Error",
                    Toast.LENGTH_LONG
                ).show()
            }finally {
                screenState.hideDialog()
            }
        }

    }

    fun restoreBackup(){
        viewModelScope.launch {
            try{
                screenState.showProgressDialog("")
                backupManager
                    .restoreBackup()
                    .flowOn(Dispatchers.IO)
                    .collect{
                        screenState.showProgressDialog(it)
                    }
            }catch (e:Exception){
                Toast.makeText(
                    getApplication(),
                    e.message ?: "Unknown Error",
                    Toast.LENGTH_LONG
                ).show()
            }finally {
                screenState.hideDialog()
            }
        }
    }
}