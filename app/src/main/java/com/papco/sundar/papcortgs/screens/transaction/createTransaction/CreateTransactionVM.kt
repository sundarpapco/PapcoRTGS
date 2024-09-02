package com.papco.sundar.papcortgs.screens.transaction.createTransaction

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.papco.sundar.papcortgs.database.common.MasterDatabase
import com.papco.sundar.papcortgs.database.receiver.Receiver
import com.papco.sundar.papcortgs.database.sender.Sender
import com.papco.sundar.papcortgs.database.transaction.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateTransactionVM(application: Application) : AndroidViewModel(application) {

    private val db: MasterDatabase = MasterDatabase.getInstance(application)
    val selectedSender = MutableLiveData<Sender?>()
    val selectedReceiver = MutableLiveData<Receiver?>()
    val amount = MutableLiveData(0)
    val remarks = MutableLiveData<String>()
    private var isAlreadyLoaded = false


    // setters -------------------------------------------------
    fun setSender(sender: Sender?) {
        selectedSender.value = sender
    }

    fun setReceiver(receiver: Receiver?) {
        selectedReceiver.value = receiver
    }

    fun setAmount(amount: Int) {
        this.amount.value = amount
    }

    fun setRemarks(remarks: String) {
        this.remarks.value = remarks
    }

    // getters ----------------------------------------------------
    fun getAmount(): LiveData<Int> {
        return amount
    }

    // utility methods ---------------------------------------------------
    fun loadTransaction(transactionId: Int) {

        isAlreadyLoaded = if (isAlreadyLoaded) return else true

        viewModelScope.launch(Dispatchers.IO) {
            val transaction = db.getTransactionDao().getTransaction(transactionId)
            val sender = db.getSenderDao().getSender(transaction!!.senderId)
            val receiver = db.getReceiverDao().getReceiver(
                transaction.receiverId
            )
            withContext(Dispatchers.Main) {
                selectedSender.postValue(sender)
                selectedReceiver.postValue(receiver)
                amount.postValue(transaction.amount)
                remarks.postValue(transaction.remarks)
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

            val firstSender = db.getSenderDao().getFirstSender()
            val defaultSender = db.getSenderDao().getSender(defaultSenderId)
            val firstReceiver = db.getReceiverDao().getFirstReceiverForSelection(groupId)

            withContext(Dispatchers.Main) {
                if (firstSender.size == 0) selectedSender.postValue(null) else if (defaultSender == null) selectedSender.postValue(
                    firstSender[0]
                ) else selectedSender.postValue(defaultSender)
                if (firstReceiver.size == 0) selectedReceiver.postValue(null) else selectedReceiver.postValue(
                    firstReceiver[0]
                )
                amount.postValue(0)
                remarks.postValue("ON ACCOUNT")
            }
        }
    }

    fun selectReceiver(receiverId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val receiver = db.getReceiverDao().getReceiver(receiverId)
            withContext(Dispatchers.Main) {
                selectedReceiver.postValue(receiver)
            }
        }
    }

    fun selectSender(senderId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val sender = db.getSenderDao().getSender(senderId)
            withContext(Dispatchers.Main) {
                selectedSender.postValue(sender)
            }
        }
    }

    fun saveAmount(amountToSave: Int) {
        amount.value = amountToSave
    }

    fun saveRemarks(remarksToSave: String) {
        remarks.value = remarksToSave
    }

    fun saveNewTransaction(groupId: Int) {
        val transaction = Transaction()
        transaction.groupId = groupId
        transaction.senderId = selectedSender.value?.id ?: error("No Sender Selected")
        transaction.receiverId = selectedReceiver.value?.id ?: error("No Receiver Selected")

        amount.value?.let {
            require(it > 0) { "Amount should be greater than zero" }
            transaction.amount = it
        } ?: error("Amount cannot be null")

        remarks.value?.let {
            transaction.remarks = it
        } ?: run {
            transaction.remarks = ""
        }

        viewModelScope.launch(Dispatchers.IO) {
            db.getTransactionDao().addTransaction(transaction)
        }
    }

    fun updateTransaction(groupId: Int, transactionId: Int) {
        val transaction = Transaction()
        transaction.id = transactionId
        transaction.groupId = groupId
        transaction.senderId = selectedSender.value?.id ?: error("No Sender Selected")
        transaction.receiverId = selectedReceiver.value?.id ?: error("No Receiver Selected")

        amount.value?.let {
            require(it > 0) { "Amount should be greater than zero" }
            transaction.amount = it
        } ?: error("Amount cannot be null")

        remarks.value?.let {
            transaction.remarks = it
        } ?: run {
            transaction.remarks = ""
        }

        viewModelScope.launch(Dispatchers.IO) {
            db.getTransactionDao().updateTransaction(transaction)
        }
    }
}
