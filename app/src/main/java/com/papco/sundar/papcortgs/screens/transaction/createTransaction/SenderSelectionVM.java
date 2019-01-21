package com.papco.sundar.papcortgs.screens.transaction.createTransaction;

import android.arch.lifecycle.LiveData;
import android.content.Context;

import com.papco.sundar.papcortgs.common.Observable;
import com.papco.sundar.papcortgs.database.common.MasterDatabase;
import java.util.List;

import com.papco.sundar.papcortgs.database.sender.Sender;

public class SenderSelectionVM extends Observable<SenderSelectionVM.SenderSelectionListener> {

    public interface SenderSelectionListener{

        void onSenderSelected(Sender receiver);
    }

    private MasterDatabase db;
    private LiveData<List<Sender>> senders;

    public SenderSelectionVM(Context context){
        db=MasterDatabase.getInstance(context);
        senders=db.getSenderDao().getAllSenders();
    }

    public LiveData<List<Sender>> getSenders() {
        return senders;
    }

    public void selectSender(Sender sender){
        if(getCallback()!=null)
            getCallback().onSenderSelected(sender);
    }

}
