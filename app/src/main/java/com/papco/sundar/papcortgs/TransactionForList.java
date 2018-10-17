package com.papco.sundar.papcortgs;

import android.arch.persistence.room.Ignore;

public class TransactionForList {

    int id;
    String sender;
    String receiver;
    String receiverMobile; //for sending sms
    int amount;
    @Ignore
    int smsStatus=-1;

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
}
