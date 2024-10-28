package com.papco.sundar.papcortgs.ui.screens.group

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.papco.sundar.papcortgs.database.pojo.Party
import com.papco.sundar.papcortgs.database.transactionGroup.TransactionGroup
import com.papco.sundar.papcortgs.database.transactionGroup.TransactionGroupListItem

class ManageGroupScreenState {

    private var groupId=0

    //DisplayFields
    var groupName:String by mutableStateOf("")
    var selectedSender: Party? by mutableStateOf(null)
    var sendersList:List<Party> by mutableStateOf(emptyList())
    private set

    var dialog:Dialog? by mutableStateOf(null)

    val isEditingMode:Boolean
        get() = groupId > 0

    fun loadGroup(transactionGroup:TransactionGroupListItem){
        groupId=transactionGroup.transactionGroup.id
        groupName=transactionGroup.transactionGroup.name
        selectedSender=Party(
            id=transactionGroup.sender.id,
            name=transactionGroup.sender.displayName,
            highlightWord = ""
        )
    }

    fun getLoadedGroup():TransactionGroup?{

        return selectedSender?.let{
            TransactionGroup().apply {
                id=groupId
                name=groupName
                defaultSenderId=it.id
            }
        }
    }

    fun loadSendersList(senders:List<Party>){
        sendersList=senders
        if(selectedSender==null && senders.isNotEmpty())
            selectedSender=senders.first()
    }

    fun showDeleteConfirmationDialog(){
        dialog=Dialog.DeleteConfirmation
    }

    sealed class Dialog{
        data object WaitDialog:Dialog()
       data object DeleteConfirmation:Dialog()
    }
}