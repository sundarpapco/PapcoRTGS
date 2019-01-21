package com.papco.sundar.papcortgs.screens.transaction.createTransaction;

import android.arch.lifecycle.LiveData;
import android.content.Context;

import com.papco.sundar.papcortgs.common.Observable;
import com.papco.sundar.papcortgs.database.common.MasterDatabase;
import com.papco.sundar.papcortgs.database.receiver.Receiver;
import java.util.List;
import com.papco.sundar.papcortgs.screens.transaction.createTransaction.ReceiverSelectionVM.*;

public class ReceiverSelectionVM extends Observable<ReceiverSelectionListener> {

    public interface ReceiverSelectionListener{

        void onReceiverSelected(Receiver receiver);
    }


    private MasterDatabase db;
    private LiveData<List<Receiver>> receivers;

    public ReceiverSelectionVM(Context context,int groupId){
        db=MasterDatabase.getInstance(context);
        receivers=db.getReceiverDao().getReceiversForSelection(groupId);
    }

    public LiveData<List<Receiver>> getReceivers() {
        return receivers;
    }

    public void selectReceiver(Receiver receiver){
        if(getCallback()!=null)
            getCallback().onReceiverSelected(receiver);
    }

}
