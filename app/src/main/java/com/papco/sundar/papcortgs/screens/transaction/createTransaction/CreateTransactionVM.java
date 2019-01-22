package com.papco.sundar.papcortgs.screens.transaction.createTransaction;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.util.Log;

import com.papco.sundar.papcortgs.database.common.MasterDatabase;
import com.papco.sundar.papcortgs.database.receiver.Receiver;
import com.papco.sundar.papcortgs.database.sender.Sender;
import com.papco.sundar.papcortgs.database.transaction.Transaction;

import java.util.List;

public class CreateTransactionVM {


    private MasterDatabase db;

    private int transactionId;
    private int groupId;
    private MutableLiveData<Sender> selectedSender;
    private MutableLiveData<Receiver> selectedReceiver;
    private MutableLiveData<Integer> amount;
    private MutableLiveData<String> remarks;

    public CreateTransactionVM(Context context, int groupId, int loadTransactionId){

        db=MasterDatabase.getInstance(context);

        transactionId=loadTransactionId;
        this.groupId=groupId;
        selectedSender=new MutableLiveData<>();
        selectedReceiver=new MutableLiveData<>();
        amount=new MutableLiveData<>();
        remarks=new MutableLiveData<>();

        initialize(transactionId);


    }


    // setters -------------------------------------------------

    public void setSender(Sender sender){
        selectedSender.setValue(sender);
    }

    public void setReceiver(Receiver receiver){
        selectedReceiver.setValue(receiver);
    }

    public void setAmount(int amount) {
        this.amount.setValue(amount);
    }

    public void setRemarks(String remarks) {
        this.remarks.setValue(remarks);
    }


    // getters ----------------------------------------------------

    public LiveData<Integer> getAmount() {
        return amount;
    }

    public MutableLiveData<String> getRemarks() {
        return remarks;
    }

    public MutableLiveData<Sender> getSelectedSender() {
        return selectedSender;
    }

    public MutableLiveData<Receiver> getSelectedReceiver() {
        return selectedReceiver;
    }


    // utility methods ---------------------------------------------------

    public void initialize(int transactionId){
        this.transactionId=transactionId;
        if(transactionId==-1) // -1 means we are not editing. But have to create a new Transaction
            createBlankTransaction(groupId);
        else
            loadTransaction(transactionId);

    }

    private void loadTransaction(final int transactionId){

        new Thread(new Runnable() {
            @Override
            public void run() {

                Transaction transaction=db.getTransactionDao().getTransaction(transactionId);
                Sender sender=db.getSenderDao().getSender(transaction.senderId);
                Receiver receiver=db.getReceiverDao().getReceiver(transaction.receiverId);

                CreateTransactionVM.this.transactionId=transaction.id;
                CreateTransactionVM.this.groupId=transaction.groupId;
                selectedSender.postValue(sender);
                selectedReceiver.postValue(receiver);
                amount.postValue(transaction.amount);
                remarks.postValue(transaction.remarks);

            }
        }).start();

    }

    private void createBlankTransaction(final int groupId){

        // This function will create a blank transaction.
        // This function has to select the first available sender and receiver for the initial screen to select
        // This function will set sender and receiver to null if no sender or receiver found in database

        transactionId=-1;
        new Thread(new Runnable() {
            @Override
            public void run() {

                List<Sender> firstSender=db.getSenderDao().getFirstSender();
                List<Receiver> firstReceiver=db.getReceiverDao().getFirstReceiverForSelection(groupId);
                if(firstSender.size()==0)
                    selectedSender.postValue(null);
                else
                    selectedSender.postValue(firstSender.get(0));

                if(firstReceiver.size()==0)
                    selectedReceiver.postValue(null);
                else
                    selectedReceiver.postValue(firstReceiver.get(0));

            }
        }).start();

    }

    public void saveNewTransaction(){

        final Transaction transaction=new Transaction();
        transaction.groupId=groupId;
        transaction.senderId=selectedSender.getValue().id;
        transaction.receiverId=selectedReceiver.getValue().id;
        transaction.amount=amount.getValue();
        transaction.remarks=remarks.getValue();
        new Thread(new Runnable() {
            @Override
            public void run() {
                db.getTransactionDao().addTransaction(transaction);
            }
        }).start();


    }

    public void updateTransaction(){

        final Transaction transaction=new Transaction();
        transaction.id=transactionId;
        transaction.groupId=groupId;
        transaction.senderId=selectedSender.getValue().id;
        transaction.receiverId=selectedReceiver.getValue().id;
        transaction.amount=amount.getValue();
        transaction.remarks=remarks.getValue();

        new Thread(new Runnable() {
            @Override
            public void run() {
                db.getTransactionDao().updateTransaction(transaction);
            }
        }).start();
    }
}
