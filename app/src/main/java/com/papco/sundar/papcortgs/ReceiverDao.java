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
public interface ReceiverDao {

    @Query("select * from Receiver order by name")
    public LiveData<List<Receiver>> getAllReceivers();

    @Query("select * from Receiver where id not in (select receiverId from `Transaction` where groupId=:groupId) order by name")
    public LiveData<List<Receiver>> getReceiversForSelection(int groupId);

    @Query("select * from Receiver")
    public List<Receiver> getAllReceiversNonLive();

    @Query("select * from receiver order by name limit 1")
    public LiveData<Receiver> getFirstReceiver();

    @Query("select * from Receiver where id=:id")
    public Receiver getReceiver(int id);

    @Update
    public int updateReceiver(Receiver updatedReceiver);

    @Delete
    public int deleteReceiver(Receiver ReceiverToDelete);

    @Query("delete from Receiver")
    public void deleteAllReceivers();

    @Insert(onConflict = REPLACE)
    public long addReceiver(Receiver newReceiver);

    @Insert(onConflict = REPLACE)
    public void addAllReceivers(List<Receiver> receivers);


}
