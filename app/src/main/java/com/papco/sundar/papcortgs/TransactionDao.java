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
public interface TransactionDao {

    @Query("select COUNT(*) from `Transaction` where groupId=:id and receiverId=:recId")
    public int getTransactionCountForReceiverInGroup(int id,int recId);

    @Query("select * from `Transaction` where groupId=:id")
    public List<Transaction> getTransactionsNonLive(int id);

    @Query("select * from `Transaction`")
    public List<Transaction> getAllTransactionsNonLive();

    @Query("select `Transaction`.id AS id, Sender.name AS sender, Receiver.name as receiver, Receiver.mobileNumber as receiverMobile, `Transaction`.amount as" +
            " amount from `Transaction` inner join Sender on `Transaction`.senderId=Sender.id " +
            "inner join Receiver on `Transaction`.receiverId=Receiver.id where `Transaction`.groupId=:id")
    public LiveData<List<TransactionForList>> getAllTransactionListItems(int id);

    @Query("select * from `Transaction` where id=:id")
    public Transaction getTransaction(int id);

    @Query("delete from `Transaction`where id=:id")
    public int deleteTransactionById(int id);

    @Update
    public int updateTransaction(Transaction updatedTransaction);

    @Delete
    public int deleteTransaction(Transaction TransactionToDelete);

    @Insert(onConflict = REPLACE)
    public long addTransaction(Transaction newTransaction);

    @Insert(onConflict = REPLACE)
    public void addAllTransactions(List<Transaction> transactions);

    @Query("delete from `Transaction`")
    public void deleteAllTransactions();

}
