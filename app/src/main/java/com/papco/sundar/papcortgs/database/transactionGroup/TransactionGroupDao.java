package com.papco.sundar.papcortgs.database.transactionGroup;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

import kotlinx.coroutines.flow.Flow;

@Dao
public interface TransactionGroupDao {

    @Query("select * from TransactionGroup order by id DESC")
    LiveData<List<TransactionGroup>> getAllTransactionGroups();

    @Transaction
    @Query("select * from TransactionGroup order by id DESC")
    Flow<List<TransactionGroupListItem>> getAllTransactionGroupsForList();

    @Transaction
    @Query("select * from TransactionGroup where id=:groupId")
    TransactionGroupListItem getTransactionGroupListItem(int groupId);

    @Query("select * from TransactionGroup")
    List<TransactionGroup> getAllGroupsNonLive();

    @Query("select * from TransactionGroup where id=:id")
    TransactionGroup getTransactionGroup(int id);

    @Query("delete from TransactionGroup where id=:id")
    int deleteTransactionGroup(int id);

    @Update
    int updateTransactionGroup(TransactionGroup updatedGroup);

    @Delete
    int deleteTransactionGroup(TransactionGroup TransactionToDelete);

    @Insert(onConflict = REPLACE)
    long addTransactionGroup(TransactionGroup newGroup);

    @Insert(onConflict = REPLACE)
    void addAllTransactionGroups(List<TransactionGroup> groups);

    @Query("delete from TransactionGroup")
    void deleteAllTransactionGroups();
}
