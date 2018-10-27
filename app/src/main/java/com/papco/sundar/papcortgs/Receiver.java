package com.papco.sundar.papcortgs;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.text.SpannableString;

@Entity
public class Receiver {

    @PrimaryKey(autoGenerate = true)
    int id;
    String accountType;
    String accountNumber;
    String name;
    String mobileNumber;
    String ifsc;
    String bank;
    @Ignore
    SpannableString highlightedName;
}
