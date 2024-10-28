package com.papco.sundar.papcortgs.ui.screens.transaction

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.papco.sundar.papcortgs.database.pojo.CohesiveTransaction
import com.papco.sundar.papcortgs.database.receiver.Receiver
import com.papco.sundar.papcortgs.database.sender.Sender
import com.papco.sundar.papcortgs.database.transaction.Transaction
import com.papco.sundar.papcortgs.R

class ManageTransactionScreenState {

    var isLoading by mutableStateOf(true)
        private set

    var isWaiting by mutableStateOf(false)

    var selectedSender:Sender? by mutableStateOf(null)
        private set

    var selectedReceiver:Receiver? by mutableStateOf(null)
        private set

    var amount:String by mutableStateOf("")
    private set

    var remarks:String? by mutableStateOf(null)
        private set

    fun selectSender(sender:Sender?){
        selectedSender=sender
    }

    fun selectReceiver(receiver:Receiver?){
        selectedReceiver=receiver
    }

    fun setAmountAs(amount:String){
        this.amount=amount
    }

    fun loadRemarks(remarks:String){
        this.remarks=remarks
    }

    fun loadTransaction(transaction:CohesiveTransaction){
        selectedSender=transaction.sender
        selectedReceiver=transaction.receiver
        amount=transaction.transaction.amount.toString()
        remarks=transaction.transaction.remarks

        isLoading=false
    }

    fun createBlankTransaction(
        sender:Sender?,
        receiver:Receiver?,
        amount:Int,
        remarks: String?
    ){
        selectedSender=sender
        selectedReceiver=receiver
        this.amount=amount.toString()
        this.remarks=remarks

        isLoading=false
    }

    fun createTransaction(groupId:Int,transactionId:Int=0):Transaction{

        return Transaction().apply {
            id=transactionId
            this.groupId = groupId
            senderId = selectedSender?.id ?: error("No Sender Selected")
            receiverId = selectedReceiver?.id ?: error("No Receiver Selected")
            this.amount = this@ManageTransactionScreenState.amount.toInt()
            remarks = this@ManageTransactionScreenState.remarks ?: ""
        }

    }

    fun validate(context:Context):Boolean{

        if(selectedSender==null){
            Toast.makeText(
                context,
                context.getString(R.string.no_sender_selected),
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        if(selectedReceiver==null){
            Toast.makeText(
                context,
                context.getString(R.string.no_receiver_selected),
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        if(amount.isBlank() || amount.toInt()==0){
            Toast.makeText(
                context,
                context.getString(R.string.enter_valid_amount),
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        return true
    }

}