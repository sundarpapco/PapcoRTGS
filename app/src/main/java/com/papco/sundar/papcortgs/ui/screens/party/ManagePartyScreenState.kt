package com.papco.sundar.papcortgs.ui.screens.party

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.papco.sundar.papcortgs.database.pojo.Party
import kotlinx.coroutines.flow.StateFlow

class ManagePartyScreenState {

    var listState = SearchablePartyListState()
    var dialogState:ManagePartyScreenDialogs? by mutableStateOf(null)
    val query:StateFlow<String>
        get() = listState.query

    val data:List<Party>?
        get() = listState.data

    fun loadData(data:List<Party>){
        listState.data=data
    }

    fun showDeleteConfirmationDialog(party:Party){
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