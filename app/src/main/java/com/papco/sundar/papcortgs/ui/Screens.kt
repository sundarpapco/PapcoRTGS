package com.papco.sundar.papcortgs.ui

import androidx.navigation3.runtime.NavKey
import com.papco.sundar.papcortgs.database.transaction.Transaction
import kotlinx.serialization.Serializable

@Serializable
data object ExcelFileList : NavKey
@Serializable
data class ManageGroup(val groupId: Int): NavKey
@Serializable
data class TransactionList(val groupId:Int,val groupName:String,val defaultSenderId:Int): NavKey
@Serializable
data class ManageTransaction(val groupId: Int,val transactionId:Int,val defaultSenderId: Int): NavKey
@Serializable
data object SendersList: NavKey
@Serializable
data object ReceiversList: NavKey
@Serializable
data class ManageSender(val senderId:Int): NavKey
@Serializable
data class ManageReceiver(val receiverId:Int): NavKey
@Serializable
data object SelectSender: NavKey
@Serializable
data class SelectReceiver(val groupId: Int): NavKey
@Serializable
data class GoogleSignIn(val groupId:Int,val groupName: String,val defaultSenderId: Int): NavKey
@Serializable
data class EmailList(val groupId: Int,val groupName: String,val defaultSenderId: Int): NavKey
@Serializable
data class MessageList(val groupId:Int): NavKey
@Serializable
data object DropBox: NavKey