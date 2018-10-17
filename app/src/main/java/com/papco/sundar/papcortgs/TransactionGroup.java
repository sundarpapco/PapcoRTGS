package com.papco.sundar.papcortgs;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class TransactionGroup {

    @PrimaryKey(autoGenerate = true)
    int id;
    String name;
}
