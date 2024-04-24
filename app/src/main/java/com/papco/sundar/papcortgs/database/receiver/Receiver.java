package com.papco.sundar.papcortgs.database.receiver;

import android.text.SpannableString;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class Receiver {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public String accountType;
    public String accountNumber;
    public String name;
    public String displayName;
    public String mobileNumber;
    public String ifsc;
    public String bank;
    public String email="";
    @Ignore
    public SpannableString highlightedName;
}
