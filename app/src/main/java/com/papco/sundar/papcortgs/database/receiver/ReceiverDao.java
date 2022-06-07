package com.papco.sundar.papcortgs.database.receiver;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import static androidx.room.OnConflictStrategy.REPLACE;


@Dao
public interface ReceiverDao {

    @Query("select * from Receiver order by name")
    LiveData<List<Receiver>> getAllReceivers();


    @Query("select * from Receiver order by name")
    List<Receiver> getAllReceiversNonLive();

    @Query(("select name from Receiver"))
    List<String> getAllReceiverNames();

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
