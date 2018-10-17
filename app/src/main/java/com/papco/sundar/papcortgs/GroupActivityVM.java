package com.papco.sundar.papcortgs;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import java.util.List;

public class GroupActivityVM extends AndroidViewModel {

    LiveData<List<TransactionGroup>> groups;
    MasterDatabase db;
    TransactionGroup editingGroup=null;

    public GroupActivityVM(@NonNull Application application) {
        super(application);
        db=MasterDatabase.getInstance(getApplication());
        groups=db.getTransactionGroupDao().getAllTransactionGroups();
    }

    public void addTransactionGroup(TransactionGroup newGroup){
        new GroupTableWorker(getApplication(),TableOperation.CREATE,null).execute(newGroup);
    }

    public void updateTransactionGroup(TransactionGroup updated){
        new GroupTableWorker(getApplication(),TableOperation.UPDATE,null).execute(updated);
    }

    public void deleteTransactionGroup(TransactionGroup toDel){
        new GroupTableWorker(getApplication(),TableOperation.DELETE,null).execute(toDel);
    }
}
