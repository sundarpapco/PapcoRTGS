package com.papco.sundar.papcortgs.ui.screens.transaction

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.database.pojo.CohesiveTransaction
import com.papco.sundar.papcortgs.database.receiver.Receiver
import com.papco.sundar.papcortgs.database.sender.Sender
import com.papco.sundar.papcortgs.database.transaction.Transaction
import com.papco.sundar.papcortgs.ui.util.ToasterState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class ManageTransactionScreenState(
    val titleResource: Int
) : ToasterState() {

    private val _popupBackstack = Channel<Boolean>(capacity = Channel.BUFFERED)
    val popUpBackStack = _popupBackstack.receiveAsFlow()

    var isLoading by mutableStateOf(true)
        private set
    var selectedSender: Sender? by mutableStateOf(null)
        private set
    var selectedReceiver: Receiver? by mutableStateOf(null)
        private set
    var amount by mutableStateOf(TextFieldValue(""))
        private set
    var remarks: String? by mutableStateOf(null)
        private set


    suspend fun popupBackStack() {
        _popupBackstack.send(true)
    }

    fun selectSender(sender: Sender?) {
        selectedSender = sender
    }

    fun selectReceiver(receiver: Receiver?) {
        selectedReceiver = receiver
    }

    fun setAmountAs(amount: TextFieldValue) {
        this.amount = amount
    }

    fun loadRemarks(remarks: String) {
        this.remarks = remarks
    }

    fun loadTransaction(transaction: CohesiveTransaction) {
        selectedSender = transaction.sender
        selectedReceiver = transaction.receiver
        amount = TextFieldValue(transaction.transaction.amount.toString())
        remarks = transaction.transaction.remarks

        isLoading = false
    }

    fun createBlankTransaction(
        sender: Sender?,
        receiver: Receiver?,
        amount: Int,
        remarks: String?
    ) {
        selectedSender = sender
        selectedReceiver = receiver
        this.amount = TextFieldValue(amount.toString())
        this.remarks = remarks

        isLoading = false
    }

    fun createTransaction(groupId: Int, transactionId: Int = 0): Transaction {

        return Transaction().apply {
            id = transactionId
            this.groupId = groupId
            senderId = selectedSender?.id ?: error("No Sender Selected")
            receiverId = selectedReceiver?.id ?: error("No Receiver Selected")
            this.amount = this@ManageTransactionScreenState.amount.text.toInt()
            remarks = this@ManageTransactionScreenState.remarks ?: ""
        }

    }

    fun validate(): Boolean {

        if (selectedSender == null) {
            toastResource(R.string.no_sender_selected)
            return false
        }

        if (selectedReceiver == null) {
            toastResource(R.string.no_receiver_selected)
            return false
        }

        if (amount.text.isBlank() || amount.text.toInt() == 0) {
            toastResource(R.string.enter_valid_amount)
            return false
        }

        return true
    }

}