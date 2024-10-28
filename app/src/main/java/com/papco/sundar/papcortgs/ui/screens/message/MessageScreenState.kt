package com.papco.sundar.papcortgs.ui.screens.message

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.work.WorkInfo
import com.papco.sundar.papcortgs.database.pojo.CohesiveTransaction

class MessageScreenState {

    var transactions:List<CohesiveTransaction>? by mutableStateOf(null)
    var dialog:Dialog? by mutableStateOf(null)
    var dispatcherState:WorkInfo.State by mutableStateOf(WorkInfo.State.SUCCEEDED)

    fun showSendConfirmationDialog(){
        dialog=Dialog.SendConfirmation
    }

    fun hideDialog(){
        dialog=null
    }

    sealed class Dialog{
        data object SendConfirmation:Dialog()
    }

}