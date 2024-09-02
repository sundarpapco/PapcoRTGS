package com.papco.sundar.papcortgs.screens.backup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.payroll.dropbox.DropBox
import com.example.payroll.dropbox.DropBoxAppConfig
import com.papco.sundar.papcortgs.database.common.MasterDatabase
import com.papco.sundar.papcortgs.settings.AppPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class DropBoxFragmentVM(application: Application): AndroidViewModel(application) {

    private val appPreferences = AppPreferences(application)
    private val dropBox = DropBox(application, appPreferences, DropBoxAppConfig())

    private val _backupOperationStatus=MutableLiveData("");
    val backupOperationStatus:LiveData<String> = _backupOperationStatus

    private var isWorking=false

    private val backupManager = BackupManager(
        MasterDatabase.getInstance(application),
        AppPreferences((application)),
        dropBox
    )

    suspend fun isDropBoxConnected():Boolean{
        return dropBox.isConnected()
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
        if(isWorking)
            return

        viewModelScope.launch {
            try{
                isWorking=true
                backupManager
                    .doBackup()
                    .flowOn(Dispatchers.IO)
                    .collect{
                        _backupOperationStatus.value=it
                    }
            }catch (e:Exception){
                _backupOperationStatus.value=e.message ?: "Unknown Error"
            }finally {
                isWorking=false
            }
        }

    }

    fun restoreBackup(){
        if(isWorking)
            return

        viewModelScope.launch {
            try{
                isWorking=true
                backupManager
                    .restoreBackup()
                    .flowOn(Dispatchers.IO)
                    .collect{
                        _backupOperationStatus.value=it
                    }
            }catch (e:Exception){
                _backupOperationStatus.value=e.message ?: "Unknown Error"
            }finally {
                isWorking=false
            }
        }
    }
}