package com.papco.sundar.papcortgs.screens.transaction.createTransaction;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;

import com.papco.sundar.papcortgs.common.Observable;
import com.papco.sundar.papcortgs.database.common.MasterDatabase;
import com.papco.sundar.papcortgs.database.receiver.Receiver;

import java.util.List;

import com.papco.sundar.papcortgs.screens.transaction.createTransaction.ReceiverSelectionVM.*;

public class ReceiverSelectionVM extends Observable<ReceiverSelectionListener>
        implements FetchReceiversForGroupTask.ReceiverForSelectionCallBack {

    public interface ReceiverSelectionListener {

        void onReceiverSelected(Receiver receiver);
    }


    private MasterDatabase db;
    private MutableLiveData<List<Receiver>> receiversList= new MutableLiveData<List<Receiver>>();

    public ReceiverSelectionVM(Context context, int groupId) {
        db = MasterDatabase.getInstance(context);
        FetchReceiversForGroupTask task=new FetchReceiversForGroupTask(context,this);
        task.execute(groupId);
    }

    public LiveData<List<Receiver>> getReceivers() {
        return receiversList;
    }

    public void selectReceiver(Receiver receiver) {
        if (getCallback() != null)
            getCallback().onReceiverSelected(receiver);
    }

    @Override
    public void onReceiverForSelectionList(List<Receiver> receivers) {
        receiversList.setValue(receivers);
    }
}
