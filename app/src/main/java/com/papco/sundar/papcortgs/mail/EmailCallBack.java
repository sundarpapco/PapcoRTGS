package com.papco.sundar.papcortgs.mail;

import com.papco.sundar.papcortgs.Transaction;

import java.util.List;

public interface EmailCallBack {

    public void onStartSending();
    public void onUpdate(List<Transaction> list);
    public void onComplete(List<Transaction>list);
}
