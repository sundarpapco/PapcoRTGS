package com.papco.sundar.papcortgs.screens.transaction.createTransaction

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.database.common.MasterDatabase
import com.papco.sundar.papcortgs.ui.screens.transaction.ManageTransactionScreenState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateTransactionVM(
    application: Application,
    val transactionId: Int,
    val groupId: Int,
    val defaultSenderId: Int,
) : AndroidViewModel(application) {

    companion object {
        fun factory(
            application: Application,
            transactionId: Int,
            groupId: Int,
            defaultSenderId: Int
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return if (modelClass.isAssignableFrom(CreateTransactionVM::class.java))
                        CreateTransactionVM(application, transactionId, groupId, defaultSenderId) as T
                    else
                        error("Unknown ViewModel class")
                }
            }
        }
    }

    private val db: MasterDatabase = MasterDatabase.getInstance(application)
    private val isEditingMode = transactionId != -1
    val screen = ManageTransactionScreenState(
        titleResource = if(isEditingMode)
            R.string.update_transaction
        else
        R.string.create_transaction
    )

    init {
        if(transactionId==-1)
            createBlankTransaction(groupId,defaultSenderId)
        else
            loadTransaction(transactionId)
    }

    // utility methods ---------------------------------------------------
    private fun loadTransaction(transactionId: Int) {

        //isAlreadyLoaded = if (isAlreadyLoaded) return else true
        viewModelScope.launch(Dispatchers.IO) {
            val transaction = db.getTransactionDao().getCohesiveTransaction(transactionId)

            withContext(Dispatchers.Main) {
                screen.loadTransaction(transaction)
            }
        }
    }

    private fun createBlankTransaction(groupId:Int,defaultSenderId:Int ){

        // This function will create a blank transaction.
        // This function has to select the first available sender and receiver for the initial screen to select
        // This function will set sender and receiver to null if no sender or receiver found in database
        // Also set Amount and remarks to initial values
        //isAlreadyLoaded = if (isAlreadyLoaded) return else true
        viewModelScope.launch(Dispatchers.IO) {

            val defaultSender = db.getSenderDao().getSender(defaultSenderId)
            val firstSender = db.getSenderDao().firstSender
            val firstReceiver = db.getReceiverDao().getFirstReceiverForSelection(groupId)

            withContext(Dispatchers.Main) {

                val sender = when {
                    firstSender.isEmpty() -> { null }
                    else -> { defaultSender }
                }

                val receiver = if (firstReceiver.isEmpty()) null else firstReceiver.first()

                screen.createBlankTransaction(
                    sender = sender,
                    receiver = receiver,
                    amount = 0,
                    remarks = getApplication<Application>().getString(R.string.on_account)
                )

            }
        }
    }

    fun selectReceiver(receiverId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val receiver = db.getReceiverDao().getReceiver(receiverId)
            withContext(Dispatchers.Main) {
                screen.selectReceiver(receiver)
            }
        }
    }

    fun selectSender(senderId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val sender = db.getSenderDao().getSender(senderId)
            withContext(Dispatchers.Main) {
                screen.selectSender(sender)
            }
        }
    }

    fun saveNewTransaction() {

        try {
            val transaction = screen.createTransaction(groupId)
            viewModelScope.launch(Dispatchers.IO) {
                db.getTransactionDao().addTransaction(transaction)
                screen.popupBackStack()
            }
        } catch (e: Exception) {
            screen.toastError(e)
        }
    }

    fun updateTransaction() {
        try {
            val transaction = screen.createTransaction(groupId, transactionId)
            viewModelScope.launch(Dispatchers.IO) {
                db.getTransactionDao().updateTransaction(transaction)
                screen.popupBackStack()
            }
        } catch (e: Exception) {
            screen.toastError(e)
        }
    }

    fun onSaveTransaction(){
        if(isEditingMode)
            updateTransaction()
        else
            saveNewTransaction()
    }
}
