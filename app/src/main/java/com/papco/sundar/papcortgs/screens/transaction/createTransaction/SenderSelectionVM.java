package com.papco.sundar.papcortgs.screens.transaction.createTransaction;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.papco.sundar.papcortgs.database.common.MasterDatabase;
import com.papco.sundar.papcortgs.database.sender.Sender;

import java.util.List;

public class SenderSelectionVM extends AndroidViewModel {


    private LiveData<List<Sender>> senders;

    public SenderSelectionVM(Application application){
        super(application);
        MasterDatabase db = MasterDatabase.getInstance(application);
        senders= db.getSenderDao().getAllSenders();
    }

    public LiveData<List<Sender>> getSenders() {
        return senders;
    }

}
