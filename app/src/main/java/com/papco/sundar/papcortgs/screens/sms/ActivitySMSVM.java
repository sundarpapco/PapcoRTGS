package com.papco.sundar.papcortgs.screens.sms;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.annotation.NonNull;

import androidx.lifecycle.MutableLiveData;

import com.papco.sundar.papcortgs.database.common.MasterDatabase;
import com.papco.sundar.papcortgs.database.common.TableOperation;
import com.papco.sundar.papcortgs.database.common.TableWorkCallback;
import com.papco.sundar.papcortgs.database.transaction.Transaction;
import com.papco.sundar.papcortgs.database.transaction.TransactionTableWorker;

import java.util.List;

public class ActivitySMSVM extends AndroidViewModel implements TableWorkCallback {

    private MutableLiveData<List<Transaction>> smsList;
    int currentGroupId=-1;
    String currentGroupName=null;
    MasterDatabase db;

    public ActivitySMSVM(@NonNull Application application) {
        super(application);
        db= MasterDatabase.getInstance(getApplication());
    }

    public MutableLiveData<List<Transaction>> getSmsList() {
        if(smsList==null){
            smsList=new MutableLiveData<>();
            if(!SmsService.IS_SERVICE_RUNNING) //if the service is running and activity is binding to it, then the list can be obtained from the service while binding
                new TransactionTableWorker(getApplication(), TableOperation.READALL,this).execute(currentGroupId);
        }

        return smsList;
    }

    public void setSmsList(List<Transaction> smsList) {
        this.smsList.setValue(smsList);
    }

    @Override
    public void onCreateComplete(long result) {

    }

    @Override
    public void onReadComplete(Object result) {

    }

    @Override
    @SuppressWarnings("unchecked")
    public void onReadAllComplete(Object result) {
        smsList.setValue((List<Transaction>)result);
    }

    @Override
    public void onUpdateComplete(int result) {

    }

    @Override
    public void onDeleteComplete(int result) {

    }
}
