package com.papco.sundar.papcortgs;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class Sender {

    @PrimaryKey(autoGenerate = true)
    int id;
    String accountType;
    String accountNumber;
    String name;
    String mobileNumber;
    String ifsc;
    String bank;

}
