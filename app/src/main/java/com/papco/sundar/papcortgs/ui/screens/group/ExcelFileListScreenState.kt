package com.papco.sundar.papcortgs.ui.screens.group

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.papco.sundar.papcortgs.database.transactionGroup.TransactionGroupListItem
import com.papco.sundar.papcortgs.ui.components.ToastMessage
import com.papco.sundar.papcortgs.ui.components.ToasterState

class ExcelFileListScreenState: ToasterState(){

    var list:List<TransactionGroupListItem> by mutableStateOf(emptyList())
    var dialogState:Dialog? by mutableStateOf(null)
    private set

    fun showWaitDialog(){
        dialogState= Dialog.WaitDialog
    }

    fun hideDialog(){
        dialogState=null
    }

    fun showSendersPasswordDialog(){
        dialogState= Dialog.SendersPasswordDialog
    }

    fun showReceiversPasswordDialog(){
        dialogState= Dialog.ReceiversPasswordDialog
    }

    fun showBackupSharingDialog(filePath:String){
        dialogState= Dialog.BackUpSharingDialog(filePath)
    }

    fun updateBackupProgress(progress:ToastMessage) {
        dialogState=Dialog.BackupProgress(progress)
    }

    fun showDeletePaymentsConfirmation(){
        dialogState=Dialog.DeleteAllPaymentsConfirmation
    }


    sealed class Dialog{
        data object SendersPasswordDialog:Dialog()
        data object ReceiversPasswordDialog:Dialog()

        data class BackUpSharingDialog(val filePath:String): Dialog()

        data object WaitDialog:Dialog()

        data class BackupProgress(val progress: ToastMessage):Dialog()
        data object DeleteAllPaymentsConfirmation:Dialog()
    }

}