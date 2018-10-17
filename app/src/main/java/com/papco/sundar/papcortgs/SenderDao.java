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
public interface SenderDao {

    @Query("select * from Sender order by name")
    public LiveData<List<Sender>> getAllSenders();

    @Query("select * from Sender")
    public List<Sender> getAllSendersNonLive();

    @Query("select * from Sender where id=:id")
    public Sender getSender(int id);

    @Query("select * from Sender order by name limit 1")
    public LiveData<Sender> getFirstSender();

    @Update
    public int updateSender(Sender updatedSender);

    @Delete
    public int deleteSender(Sender senderToDelete);

    @Insert(onConflict = REPLACE)
    public long addSender(Sender newSender);

    @Insert(onConflict = REPLACE)
    public void addAllSenders(List<Sender> senders);

    @Query("delete from Sender")
    public void deleteAllSenders();
}
