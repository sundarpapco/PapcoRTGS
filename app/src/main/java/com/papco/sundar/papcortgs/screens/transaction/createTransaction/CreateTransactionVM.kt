package com.papco.sundar.papcortgs.screens.transaction.createTransaction

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.database.common.MasterDatabase
import com.papco.sundar.papcortgs.ui.screens.transaction.ManageTransactionScreenState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateTransactionVM(application: Application) : AndroidViewModel(application) {

    private val db: MasterDatabase = MasterDatabase.getInstance(application)
    val screenState = ManageTransactionScreenState()
    private var isAlreadyLoaded = false

    private val _navigateBack:MutableStateFlow<Boolean> = MutableStateFlow(false)
    val navigateBack: Flow<Boolean> = _navigateBack


    // utility methods ---------------------------------------------------
    fun loadTransaction(transactionId: Int) {

        isAlreadyLoaded = if (isAlreadyLoaded) return else true

        viewModelScope.launch(Dispatchers.IO) {
            val transaction = db.getTransactionDao().getCohesiveTransaction(transactionId)

            withContext(Dispatchers.Main) {
                screenState.loadTransaction(transaction)
            }
        }
    }

    fun createBlankTransaction(groupId: Int, defaultSenderId: Int) {

        // This function will create a blank transaction.
        // This function has to select the first available sender and receiver for the initial screen to select
        // This function will set sender and receiver to null if no sender or receiver found in database
        // Also set Amount and remarks to initial values
        isAlreadyLoaded = if (isAlreadyLoaded) return else true

        viewModelScope.launch(Dispatchers.IO) {

            val defaultSender = db.getSenderDao().getSender(defaultSenderId)
            val firstSender = db.getSenderDao().getFirstSender()
            val firstReceiver = db.getReceiverDao().getFirstReceiverForSelection(groupId)

            withContext(Dispatchers.Main) {

                val sender = when {
                    firstSender.isEmpty() -> { null }
                    defaultSender == null -> { firstSender[0] }
                    else -> { defaultSender }
                }

                val receiver = if (firstReceiver.isEmpty()) null else firstReceiver.first()

                screenState.createBlankTransaction(
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
                screenState.selectReceiver(receiver)
            }
        }
    }

    fun selectSender(senderId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val sender = db.getSenderDao().getSender(senderId)
            withContext(Dispatchers.Main) {
                screenState.selectSender(sender)
            }
        }
    }

    fun saveNewTransaction(groupId: Int) {

        try {
            screenState.isWaiting=true
            val transaction = screenState.createTransaction(groupId)
            viewModelScope.launch(Dispatchers.IO) {
                db.getTransactionDao().addTransaction(transaction)
                _navigateBack.value=true
            }
        } catch (e: Exception) {
            screenState.isWaiting=false
            toastError(e)
        }
    }

    fun updateTransaction(groupId: Int, transactionId: Int) {
        try {
            screenState.isWaiting=true
            val transaction = screenState.createTransaction(groupId, transactionId)
            viewModelScope.launch(Dispatchers.IO) {
                db.getTransactionDao().updateTransaction(transaction)
                _navigateBack.value=true
            }
        } catch (e: Exception) {
            screenState.isWaiting=false
            toastError(e)
        }
    }

    private fun toastError(e:Exception){
        val error= e.message ?: getApplication<Application>().getString(R.string.unknown_error)
        Toast.makeText(
            getApplication(),
            error,
            Toast.LENGTH_LONG
        ).show()
    }
}
