package com.papco.sundar.papcortgs.database.transactionGroup;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class TransactionGroup {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public int defaultSenderId=0;
}
