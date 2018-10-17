package com.papco.sundar.papcortgs;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;

public class ReceiverActivityVM extends AndroidViewModel {

    LiveData<List<Receiver>> receivers;
    Receiver editingReceiver=null;
    MasterDatabase db;

    public ReceiverActivityVM(@NonNull Application application) {
        super(application);
        db=MasterDatabase.getInstance(getApplication());
        receivers=db.getReceiverDao().getAllReceivers();

    }

    public LiveData<List<Receiver>> getReceivers() {
        return receivers;
    }

    public void addReceiver(Receiver newReceiver){

        new ReceiverTableWorker(getApplication(),TableOperation.CREATE,null).execute(newReceiver);
    }

    public void updateReceiver(Receiver updateReceiver){
        new ReceiverTableWorker(getApplication(),TableOperation.UPDATE,null).execute(updateReceiver);
    }

    public void deleteReceiver(Receiver receiver){
        new ReceiverTableWorker(getApplication(),TableOperation.DELETE,null).execute(receiver);
    }

}