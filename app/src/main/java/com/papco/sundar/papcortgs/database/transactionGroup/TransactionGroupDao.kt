package com.papco.sundar.papcortgs.database.transactionGroup

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionGroupDao {

    @Transaction
    @Query("select * from TransactionGroup order by id DESC")
    fun allTransactionGroupsForList(): Flow<List<TransactionGroupListItem>>

    @Transaction
    @Query("select * from TransactionGroup where id=:groupId")
    fun getTransactionGroupListItem(groupId: Int): TransactionGroupListItem

    @Query("select * from TransactionGroup")
    fun allGroupsNonLive(): List<TransactionGroup>

    @Query("select * from TransactionGroup where id=:id")
    fun getTransactionGroup(id: Int): TransactionGroup

    @Query("delete from TransactionGroup where id=:id")
    fun deleteTransactionGroup(id: Int): Int

    @Update
    fun updateTransactionGroup(updatedGroup: TransactionGroup): Int

    @Delete
    fun deleteTransactionGroup(transactionToDelete: TransactionGroup): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addTransactionGroup(newGroup: TransactionGroup): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addAllTransactionGroups(groups: List<TransactionGroup>)

    @Query("delete from TransactionGroup")
    suspend fun deleteAllTransactionGroups()
}
