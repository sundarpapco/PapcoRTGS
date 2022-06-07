package com.papco.sundar.papcortgs.screens.transaction.createTransaction;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.papco.sundar.papcortgs.database.common.MasterDatabase;
import com.papco.sundar.papcortgs.database.receiver.Receiver;
import com.papco.sundar.papcortgs.database.sender.Sender;
import com.papco.sundar.papcortgs.database.transaction.Transaction;

import java.util.List;
import java.util.Objects;

public class CreateTransactionVM extends AndroidViewModel {


    private MasterDatabase db;

    private MutableLiveData<Sender> selectedSender;
    private MutableLiveData<Receiver> selectedReceiver;
    private MutableLiveData<Integer> amount;
    private MutableLiveData<String> remarks;
    private Boolean isAlreadyLoaded = false;

    public CreateTransactionVM(Application application) {
        super(application);

        db = MasterDatabase.getInstance(application);

        selectedSender = new MutableLiveData<>();
        selectedReceiver = new MutableLiveData<>();
        amount = new MutableLiveData<>();
        remarks = new MutableLiveData<>();

    }


    // setters -------------------------------------------------

    public void setSender(Sender sender) {
        selectedSender.setValue(sender);
    }

    public void setReceiver(Receiver receiver) {
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


    public void loadTransaction(final int transactionId) {

        if (isAlreadyLoaded)
            return;
        else
            isAlreadyLoaded = true;

        new Thread(new Runnable() {
            @Override
            public void run() {

                Transaction transaction = db.getTransactionDao().getTransaction(transactionId);
                Sender sender = db.getSenderDao().getSender(transaction.senderId);
                Receiver receiver = db.getReceiverDao().getReceiver(transaction.receiverId);

                selectedSender.postValue(sender);
                selectedReceiver.postValue(receiver);
                amount.postValue(transaction.amount);
                remarks.postValue(transaction.remarks);

            }
        }).start();

    }

    public void createBlankTransaction(final int groupId, final int defaultSenderId) {

        // This function will create a blank transaction.
        // This function has to select the first available sender and receiver for the initial screen to select
        // This function will set sender and receiver to null if no sender or receiver found in database
        // Also set Amount and remarks to initial values

        if (isAlreadyLoaded)
            return;
        else
            isAlreadyLoaded = true;

        new Thread(new Runnable() {
            @Override
            public void run() {

                List<Sender> firstSender = db.getSenderDao().getFirstSender();
                Sender defaultSender = db.getSenderDao().getSender(defaultSenderId);
                List<Receiver> firstReceiver = db.getReceiverDao().getFirstReceiverForSelection(groupId);

                if (firstSender.size() == 0)
                    selectedSender.postValue(null);
                else if (defaultSender == null)
                    selectedSender.postValue(firstSender.get(0));
                else
                    selectedSender.postValue(defaultSender);

                if (firstReceiver.size() == 0)
                    selectedReceiver.postValue(null);
                else
                    selectedReceiver.postValue(firstReceiver.get(0));

                amount.postValue(0);
                remarks.postValue("ON ACCOUNT");

            }
        }).start();

    }

    public void selectReceiver(final int receiverId) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Receiver receiver = db.getReceiverDao().getReceiver(receiverId);
                selectedReceiver.postValue(receiver);
            }
        }).start();

    }

    public void selectSender(final int senderId) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Sender sender = db.getSenderDao().getSender(senderId);
                selectedSender.postValue(sender);
            }
        }).start();

    }

    public void saveAmount(int amountToSave){
        amount.setValue(amountToSave);
    }

    public void saveRemarks(String remarksToSave){
        remarks.setValue(remarksToSave);
    }

    public void saveNewTransaction(int groupId) {

        final Transaction transaction = new Transaction();
        transaction.groupId = groupId;
        transaction.senderId = Objects.requireNonNull(selectedSender.getValue()).id;
        transaction.receiverId = Objects.requireNonNull(selectedReceiver.getValue()).id;
        Objects.requireNonNull(amount.getValue());
        transaction.amount = amount.getValue();

        transaction.remarks = remarks.getValue();
        new Thread(new Runnable() {
            @Override
            public void run() {
                db.getTransactionDao().addTransaction(transaction);
            }
        }).start();


    }

    public void updateTransaction(int groupId, int transactionId) {

        final Transaction transaction = new Transaction();
        transaction.id = transactionId;
        transaction.groupId = groupId;
        transaction.senderId = Objects.requireNonNull(selectedSender.getValue()).id;
        transaction.receiverId = Objects.requireNonNull(selectedReceiver.getValue()).id;
        Objects.requireNonNull(amount.getValue());
        transaction.amount = amount.getValue();
        transaction.remarks = remarks.getValue();

        new Thread(new Runnable() {
            @Override
            public void run() {
                db.getTransactionDao().updateTransaction(transaction);
            }
        }).start();
    }
}
