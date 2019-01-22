package com.papco.sundar.papcortgs.screens.mail;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

import com.papco.sundar.papcortgs.database.common.TableOperation;
import com.papco.sundar.papcortgs.database.common.TableWorkCallback;
import com.papco.sundar.papcortgs.database.transaction.TransactionTableWorker;
import com.papco.sundar.papcortgs.database.common.MasterDatabase;
import com.papco.sundar.papcortgs.database.transaction.Transaction;

import java.util.List;

public class ActivityEmailVM extends AndroidViewModel implements TableWorkCallback {

    private MutableLiveData<List<Transaction>> emailList;
    int currentGroupId=-1;
    String currentGroupName=null;
    MasterDatabase db;

    public ActivityEmailVM(@NonNull Application application) {
        super(application);
        db=MasterDatabase.getInstance(getApplication());
    }

    public MutableLiveData<List<Transaction>> getEmailList() {
        if(emailList==null){
            emailList=new MutableLiveData<>();
            if(!EmailService.isRunning()) //if the service is running and activity is binding to it, then the list can be obtained from the service while binding
                new TransactionTableWorker(getApplication(), TableOperation.READALL,this).execute(currentGroupId);
        }

        return emailList;
    }

    public void clearEmailList() {
        this.emailList=null;
    }

    @Override
    public void onCreateComplete(long result) {

    }

    @Override
    public void onReadComplete(Object result) {

    }

    @Override
    public void onReadAllComplete(Object result) {
        emailList.setValue((List<Transaction>)result);
    }

    @Override
    public void onUpdateComplete(int result) {

    }

    @Override
    public void onDeleteComplete(int result) {

    }


}
