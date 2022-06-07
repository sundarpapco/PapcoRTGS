package com.papco.sundar.papcortgs.database.transaction;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface TransactionDao {

    @Query("select * from `Transaction` where groupId=:id")
    //used by fileExporter to export a group to excel file and mail
    List<Transaction> getTransactionsNonLive(int id);

    @Query("select receiverId from `Transaction` where groupId=:groupId")
    List<Integer> getReceiverIdsOfGroup(int groupId);

    @Query("select * from `Transaction`") //used while creating backup to dropbox
    List<Transaction> getAllTransactionsNonLive();

    @Query("select `Transaction`.id AS id, Sender.name AS sender, Receiver.name as receiver, Receiver.mobileNumber as receiverMobile, `Transaction`.amount as" +
            " amount from `Transaction` inner join Sender on `Transaction`.senderId=Sender.id " +
            "inner join Receiver on `Transaction`.receiverId=Receiver.id where `Transaction`.groupId=:id")
    LiveData<List<TransactionForList>> getAllTransactionListItems(int id);

    @Query("select * from `Transaction` where id=:id")
    Transaction getTransaction(int id);

    @Query("delete from `Transaction`where id=:id")
    int deleteTransactionById(int id);

    @Update
    int updateTransaction(Transaction updatedTransaction);

    @Insert(onConflict = REPLACE)
    long addTransaction(Transaction newTransaction);

    @Insert(onConflict = REPLACE)
    void addAllTransactions(List<Transaction> transactions);

    @Query("delete from `Transaction`")
    void deleteAllTransactions();

}
