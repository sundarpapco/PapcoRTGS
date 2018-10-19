package com.papco.sundar.papcortgs;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;

public class ActivitySMSVM extends AndroidViewModel {

    private LiveData<List<TransactionForList>> transactions;
    int currentGroupId=-1;
    boolean sendingSms=false;
    MasterDatabase db;
    TransactionForList currentlySendingTransaction=null;

    public ActivitySMSVM(@NonNull Application application) {
        super(application);
        db=MasterDatabase.getInstance(getApplication());
    }

    public LiveData<List<TransactionForList>> getTransactions(){

        if(transactions==null){
            transactions=db.getTransactionDao().getAllTransactionListItems(currentGroupId);
        }
        return transactions;
    }

}
