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

    sealed class Dialog{
        data object SendersPasswordDialog:Dialog()
        data object ReceiversPasswordDialog:Dialog()

        data class BackUpSharingDialog(val filePath:String): Dialog()

        data object WaitDialog:Dialog()

        data class BackupProgress(val progress: ToastMessage):Dialog()
    }

}