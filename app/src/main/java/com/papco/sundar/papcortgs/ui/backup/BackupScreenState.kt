package com.papco.sundar.papcortgs.ui.backup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.papco.sundar.papcortgs.dropbox.DropBoxAccount

class BackupScreenState {

    var isDropBoxConnected by mutableStateOf(false)
    var account:DropBoxAccount? by mutableStateOf(null)
    var dialog:Dialog? by mutableStateOf(null)
        private set

    fun showRestoreConfirmationDialog(){
        dialog=Dialog.RestoreConfirmation
    }

    fun showProgressDialog(progress:String){
        dialog=Dialog.BackupStatus(progress)
    }

    fun hideDialog(){
        dialog=null
    }

    sealed class Dialog{
        data object RestoreConfirmation:Dialog()
        class BackupStatus(val progress:String):Dialog()
    }

}