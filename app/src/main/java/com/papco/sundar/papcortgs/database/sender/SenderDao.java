package com.papco.sundar.papcortgs.database.sender;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface SenderDao {

    @Query("select * from Sender order by name")
    LiveData<List<Sender>> getAllSenders();

    @Query("select * from Sender")
    List<Sender> getAllSendersNonLive();

    @Query("select * from Sender where id=:id")
    Sender getSender(int id);

    @Query("select * from Sender order by name limit 1")
    List<Sender> getFirstSender();

    @Update
    int updateSender(Sender updatedSender);

    @Delete
    int deleteSender(Sender senderToDelete);

    @Insert(onConflict = REPLACE)
    long addSender(Sender newSender);

    @Insert(onConflict = REPLACE)
    void addAllSenders(List<Sender> senders);

    @Query("delete from Sender")
    void deleteAllSenders();
}
