package com.papco.sundar.papcortgs.database.receiver;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface ReceiverDao {

    @Query("select * from Receiver order by name")
    LiveData<List<Receiver>> getAllReceivers();

    @Query("select * from Receiver where id not in (select receiverId from `Transaction` where groupId=:groupId) order by name")
    LiveData<List<Receiver>> getReceiversForSelection(int groupId);

    @Query("select * from Receiver")
    List<Receiver> getAllReceiversNonLive();

    @Query("select * from Receiver where id not in (select receiverId from `Transaction` where groupId=:groupId) order by name limit 1")
    List<Receiver> getFirstReceiverForSelection(int groupId);

    @Query("select * from Receiver where id=:id")
    Receiver getReceiver(int id);

    @Update
    int updateReceiver(Receiver updatedReceiver);

    @Delete
    int deleteReceiver(Receiver ReceiverToDelete);

    @Query("delete from Receiver")
    void deleteAllReceivers();

    @Insert(onConflict = REPLACE)
    long addReceiver(Receiver newReceiver);

    @Insert(onConflict = REPLACE)
    void addAllReceivers(List<Receiver> receivers);


}
