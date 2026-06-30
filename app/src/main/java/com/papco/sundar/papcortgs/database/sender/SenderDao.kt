package com.papco.sundar.papcortgs.database.sender

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Dao
interface SenderDao {
    @get:Query("select * from Sender order by name")
    val allSenders: Flow<List<Sender>>

    @get:Query("select * from Sender")
    val allSendersNonLive: List<Sender>

    @get:Query(("select displayName from Sender"))
    val allSenderNames: List<String>

    @Query("select * from Sender where id=:id")
    fun getSender(id: Int): Sender

    @get:Query("select * from Sender order by name limit 1")
    val firstSender: List<Sender>

    @Update
    fun updateSender(updatedSender: Sender): Int

    @Delete
    fun deleteSender(senderToDelete: Sender): Int

    @Query("delete from Sender where id=:id")
    fun deleteSenderById(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addSender(newSender: Sender): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addAllSenders(senders: List<Sender>)

    @Query("delete from Sender")
    fun deleteAllSenders()
}
