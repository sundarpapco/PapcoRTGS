package com.papco.sundar.papcortgs.ui.screens.transaction

import com.papco.sundar.papcortgs.database.transaction.TransactionForList
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class TransactionListScreenState {

    var transactions:List<TransactionForList> by mutableStateOf(emptyList())
    var dialog:Dialog? by mutableStateOf(null)

    sealed class Dialog{
        class DeleteConfirmation(val id:Int):Dialog()
        class ReportGenerated(val fileName:String):Dialog()

        data object DatePicker:Dialog()
        data object ChequeNumberDialog:Dialog()
    }
}