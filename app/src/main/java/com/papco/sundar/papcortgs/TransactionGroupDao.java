package com.papco.sundar.papcortgs;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface TransactionGroupDao {

    @Query("select * from TransactionGroup order by id DESC")
    public LiveData<List<TransactionGroup>> getAllTransactionGroups();

    @Query("select * from TransactionGroup")
    public List<TransactionGroup> getAllGroupsNonLive();

    @Query("select * from TransactionGroup where id=:id")
    public TransactionGroup getTransactionGroup(int id);

    @Update
    public int updateTransactionGroup(TransactionGroup updatedGroup);

    @Delete
    public int deleteTransactionGroup(TransactionGroup TransactionToDelete);

    @Insert(onConflict = REPLACE)
    public long addTransactionGroup(TransactionGroup newGroup);

    @Insert(onConflict = REPLACE)
    public void addAllTransactionGroups(List<TransactionGroup> groups);

    @Query("delete from TransactionGroup")
    public void deleteAllTransactionGroups();
}
