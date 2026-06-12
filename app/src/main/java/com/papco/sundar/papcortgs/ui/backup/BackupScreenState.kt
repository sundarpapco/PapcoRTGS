package com.papco.sundar.papcortgs.ui.backup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.papco.sundar.papcortgs.dropbox.DropBoxAccount
import com.papco.sundar.papcortgs.screens.backup.BackupUpdate
import com.papco.sundar.papcortgs.ui.components.ToastMessage
import com.papco.sundar.papcortgs.ui.components.ToasterState
import com.papco.sundar.papcortgs.ui.components.toastMessage

class BackupScreenState: ToasterState() {

    var isDropBoxConnected by mutableStateOf(false)
    var account:DropBoxAccount? by mutableStateOf(null)
    var dialog:Dialog? by mutableStateOf(null)
        private set

    fun showRestoreConfirmationDialog(){
        dialog=Dialog.RestoreConfirmation
    }

    suspend fun showProgressDialog(progress: BackupUpdate){
        when(progress){
            is BackupUpdate.Progress ->{
                dialog = Dialog.BackupStatus(progress.progress)
            }

            is BackupUpdate.Success ->{
                hideDialog()
            }

            is BackupUpdate.Failed ->{
                hideDialog()
                toast((progress).error.toastMessage())
            }
        }
    }

    fun hideDialog(){
        dialog=null
    }

    sealed class Dialog{
        data object RestoreConfirmation:Dialog()
        class BackupStatus(val progress: ToastMessage):Dialog()
    }

}