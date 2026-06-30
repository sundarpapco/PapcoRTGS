package com.papco.sundar.papcortgs.database.receiver

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceiverDao {
    @get:Query("select * from Receiver order by name")
    val allReceivers: Flow<List<Receiver>>


    @get:Query("select * from Receiver order by name")
    val allReceiversNonLive: List<Receiver>

    @get:Query(("select displayName from Receiver"))
    val allReceiverDisplayNames: List<String>

    @Query("select * from Receiver where id not in (select receiverId from `Transaction` where groupId=:groupId) order by name limit 1")
    fun getFirstReceiverForSelection(groupId: Int): List<Receiver>

    @Query("select * from Receiver where id=:id")
    fun getReceiver(id: Int): Receiver

    @Update
    fun updateReceiver(updatedReceiver: Receiver): Int

    @Query("delete from Receiver where id =:id")
    fun deleteReceiverById(id: Int)

    @Query("delete from Receiver")
    fun deleteAllReceivers()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addReceiver(newReceiver: Receiver): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addAllReceivers(receivers: List<Receiver>)
}
