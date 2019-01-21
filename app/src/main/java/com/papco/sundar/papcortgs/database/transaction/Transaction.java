package com.papco.sundar.papcortgs.database.transaction;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import com.papco.sundar.papcortgs.database.transactionGroup.TransactionGroup;
import com.papco.sundar.papcortgs.database.receiver.Receiver;
import com.papco.sundar.papcortgs.database.sender.Sender;
import com.papco.sundar.papcortgs.screens.mail.EmailService;

@Entity(foreignKeys={@ForeignKey(entity = Sender.class,parentColumns = "id",childColumns = "senderId",onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = Receiver.class,parentColumns = "id",childColumns = "receiverId",onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = TransactionGroup.class,parentColumns = "id",childColumns = "groupId",onDelete = ForeignKey.CASCADE)})
public class Transaction {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public int groupId;
    public int senderId;
    public int receiverId;
    public int amount;
    public String remarks;
    @Ignore
    public Sender sender=null;
    @Ignore
    public Receiver receiver=null;
    @Ignore
    public int smsStatus=-1;
    @Ignore
    public int emailStatus=EmailService.STATUS_DEFAULT;

    public String getAmountAsString(){

        String value=Integer.toString(amount);
        char lastDigit=value.charAt(value.length()-1);
        String result = "";
        int len = value.length()-1;
        int nDigits = 0;

        for (int i = len - 1; i >= 0; i--)
        {
            result = value.charAt(i) + result;
            nDigits++;
            if (((nDigits % 2) == 0) && (i > 0))
            {
                result = "," + result;
            }
        }
        return ("\u20B9 "+result+lastDigit);
    }

    public static String formatAmountAsString(int amount){

        String value=Integer.toString(amount);
        char lastDigit=value.charAt(value.length()-1);
        String result = "";
        int len = value.length()-1;
        int nDigits = 0;

        for (int i = len - 1; i >= 0; i--)
        {
            result = value.charAt(i) + result;
            nDigits++;
            if (((nDigits % 2) == 0) && (i > 0))
            {
                result = "," + result;
            }
        }
        return ("\u20B9 "+result+lastDigit);
    }

    public static String amountAsString(int amount){

        String value=Integer.toString(amount);
        char lastDigit=value.charAt(value.length()-1);
        String result = "";
        int len = value.length()-1;
        int nDigits = 0;

        for (int i = len - 1; i >= 0; i--)
        {
            result = value.charAt(i) + result;
            nDigits++;
            if (((nDigits % 2) == 0) && (i > 0))
            {
                result = "," + result;
            }
        }
        return ("Rs."+result+lastDigit);
    }

}
