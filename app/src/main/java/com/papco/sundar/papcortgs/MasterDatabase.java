package com.papco.sundar.papcortgs;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {Sender.class,Receiver.class,Transaction.class,TransactionGroup.class},version = 1)
public abstract class MasterDatabase extends RoomDatabase {

    static MasterDatabase db;

    public static MasterDatabase getInstance(Context context){

        if(db==null)
            db= Room.databaseBuilder(context,MasterDatabase.class,"master_database").build();

        return db;
    }

    public abstract SenderDao getSenderDao();
    public abstract ReceiverDao getReceiverDao();
    public abstract TransactionDao getTransactionDao();
    public abstract TransactionGroupDao getTransactionGroupDao();

}
