package com.papco.sundar.papcortgs.database.sender;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;

import kotlinx.coroutines.flow.Flow;

@Dao
public interface SenderDao {

    @Query("select * from Sender order by name")
    Flow<List<Sender>> getAllSenders();

    @Query("select * from Sender")
    List<Sender> getAllSendersNonLive();

    @Query(("select displayName from Sender"))
    List<String> getAllSenderNames();

    @Query("select * from Sender where id=:id")
    Sender getSender(int id);

    @Query("select * from Sender order by name limit 1")
    List<Sender> getFirstSender();

    @Update
    int updateSender(Sender updatedSender);

    @Delete
    int deleteSender(Sender senderToDelete);

    @Query("delete from Sender where id=:id")
    void deleteSenderById(int id);

    @Insert(onConflict = REPLACE)
    long addSender(Sender newSender);

    @Insert(onConflict = REPLACE)
    void addAllSenders(List<Sender> senders);

    @Query("delete from Sender")
    void deleteAllSenders();
}
