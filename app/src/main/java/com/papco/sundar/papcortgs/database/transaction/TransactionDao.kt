package com.papco.sundar.papcortgs.database.transaction

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.papco.sundar.papcortgs.database.pojo.CohesiveTransaction
import com.papco.sundar.papcortgs.screens.mail.MailDispatcher
import com.papco.sundar.papcortgs.screens.sms.MessageDispatcher
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("select * from `Transaction` where groupId=:id")

    //used by fileExporter to export a group to excel file and mail
    suspend fun getTransactionsNonLive(id: Int): List<Transaction>

    @Query("select receiverId from `Transaction` where groupId=:groupId")
    suspend fun getReceiverIdsOfGroup(groupId: Int): List<Int>

    @Query("select * from `Transaction`")
    suspend fun getAllTransactions(): List<Transaction>

    @Query(
        "select `Transaction`.id AS id, Sender.displayName AS sender, Receiver.displayName as receiver, Receiver.mobileNumber as receiverMobile, `Transaction`.amount as" + " amount from `Transaction` inner join Sender on `Transaction`.senderId=Sender.id " + "inner join Receiver on `Transaction`.receiverId=Receiver.id where `Transaction`.groupId=:id"
    )
    fun getAllTransactionListItems(id: Int): Flow<List<TransactionForList>>

    @Query("select * from `Transaction` where id=:id")
    suspend fun getTransaction(id: Int): Transaction?

    @Query("delete from `Transaction`where id=:id")
    suspend fun deleteTransactionById(id: Int): Int

    @Update
    suspend fun updateTransaction(updatedTransaction: Transaction): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addTransaction(newTransaction: Transaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAllTransactions(transactions: List<Transaction>)

    @Query("delete from `Transaction`")
    suspend fun deleteAllTransactions()

    @androidx.room.Transaction
    @Query("select * from `transaction` where id=:id")
    suspend fun getCohesiveTransaction(id:Int):CohesiveTransaction

    @androidx.room.Transaction
    @Query("select * from `transaction` where groupId=:groupId")
    fun getAllCohesiveTransactionsOfGroup(groupId: Int): Flow<List<CohesiveTransaction>>

    @Query("update `Transaction` set mailSent=${MailDispatcher.QUEUED} where groupId=:groupId and mailSent <> ${MailDispatcher.SENT}")
    suspend fun queueUpTransactionsForMail(groupId:Int)

    @Query("update `Transaction` set messageSent=${MessageDispatcher.QUEUED} where groupId=:groupId and messageSent <> ${MessageDispatcher.SENT}")
    suspend fun queueUpTransactionsForMessage(groupId:Int)

    @Query("update `Transaction` set messageSent=${MessageDispatcher.ERROR} where groupId=:groupId and messageSent = ${MessageDispatcher.QUEUED}")
    suspend fun timeoutMessagesQueuedForMessage(groupId:Int)

    @Query("update `Transaction` set mailSent=:status where id=:id")
    suspend fun updateMailSentStatus(id:Int,status:Int)

    @Query("update `Transaction` set messageSent=:status where id=:id")
    suspend fun updateMessageSentStatus(id:Int,status:Int)

}
