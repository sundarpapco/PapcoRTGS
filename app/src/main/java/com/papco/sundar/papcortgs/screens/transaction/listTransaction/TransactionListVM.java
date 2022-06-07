package com.papco.sundar.papcortgs.screens.transaction.listTransaction;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.papco.sundar.papcortgs.database.common.MasterDatabase;
import com.papco.sundar.papcortgs.database.transaction.TransactionForList;

import java.util.List;

public class TransactionListVM extends AndroidViewModel {

    private MasterDatabase db;
    private LiveData<List<TransactionForList>> transactions;

    public TransactionListVM(Application application) {
        super(application);
        db = MasterDatabase.getInstance(application);
    }


    public LiveData<List<TransactionForList>> getTransactions(int groupId) {

        if (transactions == null)
            transactions = db.getTransactionDao().getAllTransactionListItems(groupId);

        return transactions;
    }

    public void deleteTransaction(final int transactionId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                db.getTransactionDao().deleteTransactionById(transactionId);
            }
        }).start();
    }
}
