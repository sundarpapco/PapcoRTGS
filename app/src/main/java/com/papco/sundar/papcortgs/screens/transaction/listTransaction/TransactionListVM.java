package com.papco.sundar.papcortgs.screens.transaction.listTransaction;

import android.arch.lifecycle.LiveData;
import android.content.Context;

import com.papco.sundar.papcortgs.database.common.MasterDatabase;
import com.papco.sundar.papcortgs.database.transaction.Transaction;
import com.papco.sundar.papcortgs.database.transaction.TransactionForList;

import java.util.List;

public class TransactionListVM {

    private int groupId;
    private MasterDatabase db;
    private LiveData<List<TransactionForList>> transactions;

    public TransactionListVM(Context context,int groupId){
        db=MasterDatabase.getInstance(context);
        this.groupId=groupId;
    }

    public LiveData<List<TransactionForList>> getTransactions() {

        if(transactions==null)
            transactions=db.getTransactionDao().getAllTransactionListItems(groupId);

        return transactions;
    }

    public void deleteTransaction(final int transactionId){
        new Thread(new Runnable() {
            @Override
            public void run() {
                db.getTransactionDao().deleteTransactionById(transactionId);
            }
        }).start();
    }
}
