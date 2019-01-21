package com.papco.sundar.papcortgs.database.receiver;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.text.SpannableString;

@Entity
public class Receiver {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public String accountType;
    public String accountNumber;
    public String name;
    public String mobileNumber;
    public String ifsc;
    public String bank;
    public String email="";
    @Ignore
    public SpannableString highlightedName;
}
