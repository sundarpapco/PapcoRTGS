package com.papco.sundar.papcortgs.screens.mail;

import com.papco.sundar.papcortgs.database.transaction.Transaction;

import java.util.List;

public interface EmailCallBack {

    //Will be called immediately when a observer registers this callback
    //use this callback for initial binding
    void onObserverAttached(List<Transaction> currentList);

    //Will be called when sending mail starts
    void onStartSending();

    void onUpdate(int updatePosition);

    void onComplete(List<Transaction> list);
}
