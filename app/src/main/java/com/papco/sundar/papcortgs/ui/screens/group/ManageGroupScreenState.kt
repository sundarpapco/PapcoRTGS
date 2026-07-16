package com.papco.sundar.papcortgs.ui.screens.group

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.papco.sundar.papcortgs.database.pojo.Party
import com.papco.sundar.papcortgs.database.transactionGroup.TransactionGroup
import com.papco.sundar.papcortgs.database.transactionGroup.TransactionGroupListItem
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow

class ManageGroupScreenState(
    val titleResource:Int,
    val sendersList: StateFlow<List<Party>>
) {

    private var groupId=0

    //DisplayFields
    var groupName:String by mutableStateOf("")
    var selectedSender: Party? by mutableStateOf(null)
    /*var sendersList:List<Party> by mutableStateOf(emptyList())
    private set*/

    var dialog:Dialog? by mutableStateOf(null)
    private set

    val isEditingMode:Boolean
        get() = groupId > 0

    private val _popUpBackStack = Channel<Boolean>(capacity = BUFFERED)
    val popUpBackStack = _popUpBackStack.receiveAsFlow()

    fun popUpBackStack(){
        _popUpBackStack.trySend(true)
    }

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

    fun showDeleteConfirmationDialog(){
        dialog=Dialog.DeleteConfirmation
    }

    fun showWaitDialog(){
        dialog=Dialog.WaitDialog
    }

    fun dismissDialog(){
        dialog=null
    }

    sealed class Dialog{
        data object WaitDialog:Dialog()
       data object DeleteConfirmation:Dialog()
    }
}