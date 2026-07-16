package com.papco.sundar.papcortgs.ui.screens.party

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.papco.sundar.papcortgs.database.pojo.Party

class ManagePartyScreenState: SearchablePartyListState() {
    var dialogState:ManagePartyScreenDialogs? by mutableStateOf(null)

    fun loadData(data:List<Party>){
        this.data=data
    }

    fun showDeleteConfirmationDialog(party: Party){
        dialogState=ManagePartyScreenDialogs.DeletePartyDialog(party)
    }

   fun dismissDialog(){
       dialogState=null
   }

    fun showWaitDialog(){
        dialogState=ManagePartyScreenDialogs.WaitDialog
    }

    fun hideWaitDialog(){
        if(dialogState!=null)
            dialogState=null
    }

}