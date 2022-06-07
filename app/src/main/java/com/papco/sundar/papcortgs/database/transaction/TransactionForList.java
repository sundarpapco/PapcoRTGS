package com.papco.sundar.papcortgs.database.transaction;


import androidx.room.Ignore;

public class TransactionForList {

    public int id;
    public String sender;
    public String receiver;
    public String receiverMobile; //for sending sms
    public int amount;
    @Ignore
    public int smsStatus=-1;

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
