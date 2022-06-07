package com.papco.sundar.papcortgs.database.sender;

import android.text.SpannableString;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class Sender {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public String accountType;
    public String accountNumber;
    public String name;
    public String mobileNumber;
    public String ifsc;
    public String bank;
    public String email;
    @Ignore
    public SpannableString highlightedName;

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}
