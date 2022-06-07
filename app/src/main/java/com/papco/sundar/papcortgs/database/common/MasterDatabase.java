package com.papco.sundar.papcortgs.database.common;

import android.content.Context;
import androidx.annotation.NonNull;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.papco.sundar.papcortgs.database.transactionGroup.TransactionGroup;
import com.papco.sundar.papcortgs.database.transactionGroup.TransactionGroupDao;
import com.papco.sundar.papcortgs.database.receiver.Receiver;
import com.papco.sundar.papcortgs.database.receiver.ReceiverDao;
import com.papco.sundar.papcortgs.database.sender.Sender;
import com.papco.sundar.papcortgs.database.sender.SenderDao;
import com.papco.sundar.papcortgs.database.transaction.Transaction;
import com.papco.sundar.papcortgs.database.transaction.TransactionDao;

@Database(entities = {Sender.class, Receiver.class, Transaction.class, TransactionGroup.class},version = 3)
public abstract class MasterDatabase extends RoomDatabase {

    static MasterDatabase db;
    private static final Migration MIGRATION_1_2 =new Migration(1,2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Receiver ADD COLUMN email TEXT");
            database.execSQL("ALTER TABLE Sender ADD COLUMN email TEXT");
        }
    };

    private static final Migration MIGRATION_2_3=new Migration(2,3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE TransactionGroup ADD COLUMN defaultSenderId INTEGER NOT NULL DEFAULT 0");
        }
    };

    public static MasterDatabase getInstance(Context context){

        if(db==null)
            db= Room.databaseBuilder(context,MasterDatabase.class,"master_database")
                    .addMigrations(MIGRATION_1_2)
                    .addMigrations(MIGRATION_2_3)
                    .build();

        return db;
    }

    public abstract SenderDao getSenderDao();
    public abstract ReceiverDao getReceiverDao();
    public abstract TransactionDao getTransactionDao();
    public abstract TransactionGroupDao getTransactionGroupDao();

}
