package com.papco.sundar.papcortgs.ui

import com.papco.sundar.papcortgs.database.transaction.Transaction
import kotlinx.serialization.Serializable

@Serializable data object ExcelFileList
@Serializable data class ManageGroup(val groupId: Int)
@Serializable data class TransactionList(val groupId:Int,val groupName:String,val defaultSenderId:Int)
@Serializable data class ManageTransaction(val groupId: Int,val transactionId:Int,val defaultSenderId: Int)
@Serializable data object SendersList
@Serializable data object ReceiversList
@Serializable data class ManageSender(val senderId:Int)
@Serializable data class ManageReceiver(val receiverId:Int)
@Serializable data object SelectSender
@Serializable data class SelectReceiver(val groupId: Int)
@Serializable data class GoogleSignIn(val groupId:Int,val groupName: String,val defaultSenderId: Int)
@Serializable data class EmailList(val groupId: Int,val groupName: String,val defaultSenderId: Int)
@Serializable data class MessageList(val groupId:Int)
@Serializable data object DropBox