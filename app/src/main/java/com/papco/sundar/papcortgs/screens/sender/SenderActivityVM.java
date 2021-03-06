package com.papco.sundar.papcortgs.screens.sender;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.papco.sundar.papcortgs.database.sender.SenderTableWorker;
import com.papco.sundar.papcortgs.database.common.MasterDatabase;
import com.papco.sundar.papcortgs.database.common.TableOperation;
import com.papco.sundar.papcortgs.database.sender.Sender;

import java.util.List;

public class SenderActivityVM extends AndroidViewModel {

    LiveData<List<Sender>> senders;
    Sender editingSender=null;
    MasterDatabase db;

    public SenderActivityVM(@NonNull Application application) {
        super(application);
        db= MasterDatabase.getInstance(getApplication());
        senders=db.getSenderDao().getAllSenders();

    }

    public LiveData<List<Sender>> getSenders() {
        return senders;
    }

    public void addSender(Sender newSender){

        new SenderTableWorker(getApplication(), TableOperation.CREATE,null).execute(newSender);
    }

    public void updateSender(Sender updateSender){
        new SenderTableWorker(getApplication(),TableOperation.UPDATE,null).execute(updateSender);
    }

    public void deleteSender(Sender sender){
        new SenderTableWorker(getApplication(),TableOperation.DELETE,null).execute(sender);
    }


}
