package com.papco.sundar.papcortgs;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.NonNull;

@Database(entities = {Sender.class,Receiver.class,Transaction.class,TransactionGroup.class},version = 2)
public abstract class MasterDatabase extends RoomDatabase {

    static MasterDatabase db;
    private static final Migration MISGRATION_1_2=new Migration(1,2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Receiver ADD COLUMN email TEXT");
            database.execSQL("ALTER TABLE Sender ADD COLUMN email TEXT");
        }
    };

    public static MasterDatabase getInstance(Context context){

        if(db==null)
            db= Room.databaseBuilder(context,MasterDatabase.class,"master_database")
                    .addMigrations(MISGRATION_1_2).build();

        return db;
    }

    public abstract SenderDao getSenderDao();
    public abstract ReceiverDao getReceiverDao();
    public abstract TransactionDao getTransactionDao();
    public abstract TransactionGroupDao getTransactionGroupDao();

}
