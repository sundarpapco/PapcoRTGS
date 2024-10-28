package com.papco.sundar.papcortgs.ui.screens.mail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.work.WorkInfo
import com.papco.sundar.papcortgs.database.pojo.CohesiveTransaction

class MailScreenState {

    var transactions:List<CohesiveTransaction>? by mutableStateOf(null)
    var loggedInGmail:String? by mutableStateOf(null)
    var dialog:Dialog? by mutableStateOf(null)
    var dispatcherState:WorkInfo.State by mutableStateOf(WorkInfo.State.SUCCEEDED)

    fun showSendConfirmationDialog(){
        dialog=Dialog.SendConfirmation
    }

    fun showSignOutConfirmationDialog(){
        dialog=Dialog.SignOutConfirmation
    }

    fun hideDialog(){
        dialog=null
    }

    sealed class Dialog{
        data object SendConfirmation:Dialog()
        data object SignOutConfirmation:Dialog()
    }

}