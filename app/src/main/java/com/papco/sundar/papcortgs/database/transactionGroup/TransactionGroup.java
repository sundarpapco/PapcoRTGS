package com.papco.sundar.papcortgs.database.transactionGroup;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class TransactionGroup {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
}
