package com.papco.sundar.papcortgs;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

public class TransactionActivityVM extends AndroidViewModel implements TableWorkCallback {

    private LiveData<List<TransactionForList>> transactions;

    LiveData<List<Sender>> senders;
    private LiveData<List<Receiver>> getReceiversForSelection;

    TransactionGroup currentGroup;
    int editingTransactionId=-1;
    MutableLiveData<Transaction> editingTransaction=null;

    MutableLiveData<Sender> selectedSender=null;
    MutableLiveData<Receiver> selectedReceiver=null;

    MasterDatabase db;

    public TransactionActivityVM(@NonNull Application application) {
        super(application);

        Log.d("SUNDAR", "TransactionActivityVM is creating");

        db=MasterDatabase.getInstance(getApplication());
        senders=db.getSenderDao().getAllSenders();
        selectedSender=new MutableLiveData<>();
        selectedReceiver=new MutableLiveData<>();
        editingTransaction=new MutableLiveData<>();

    }




    public LiveData<List<Receiver>> getGetReceiversForSelection(int groupId) {

        if(getReceiversForSelection==null)
            getReceiversForSelection=db.getReceiverDao().getReceiversForSelection(groupId);

        return getReceiversForSelection;
    }

    public LiveData<List<TransactionForList>> getTransactions(){

        if(transactions==null)
            transactions=db.getTransactionDao().getAllTransactionListItems(currentGroup.id);

        return transactions;
    }


    public void addTransaction(Transaction newTransaction){

        new TransactionTableWorker(getApplication(),TableOperation.CREATE,this).execute(newTransaction);
    }

    public void updateTransaction(Transaction updateTransaction){
        new TransactionTableWorker(getApplication(),TableOperation.UPDATE,null).execute(updateTransaction);
    }

    public void deleteTransaction(int id){
        new TransactionTableWorker(getApplication(),TableOperation.DELETE,null).execute(id);
    }

    public void loadTransaction(int id){

        new TransactionTableWorker(getApplication(),TableOperation.READ,this).execute(id);
    }


    public void exportFile() {

        new FileExporter(getApplication(),null).execute(currentGroup);
    }

    @Override
    public void onCreateComplete(long result) {

        if(result==-1){
            //duplicate transaction for the same receiver
            Toast.makeText(getApplication(),"Already this receiver has a transaction.",Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onReadComplete(Object result) {

        if(result instanceof Transaction)
            editingTransaction.setValue((Transaction)result);

    }

    @Override
    public void onReadAllComplete(Object result) {

    }

    @Override
    public void onUpdateComplete(int result) {

    }

    @Override
    public void onDeleteComplete(int result) {

    }

}
