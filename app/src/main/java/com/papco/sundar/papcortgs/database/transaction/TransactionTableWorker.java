package com.papco.sundar.papcortgs.database.transaction;

import android.content.Context;
import android.os.AsyncTask;

import com.papco.sundar.papcortgs.database.common.MasterDatabase;
import com.papco.sundar.papcortgs.database.common.TableOperation;
import com.papco.sundar.papcortgs.database.common.TableWorkCallback;

import java.util.List;

public class TransactionTableWorker extends AsyncTask<Object,Void,Object> {

    Context context=null;
    TableOperation operation=null;
    TableWorkCallback callback=null;
    MasterDatabase db;

    public TransactionTableWorker(Context context, TableOperation operation, TableWorkCallback callback){
        this.operation=operation;
        this.context=context;
        this.callback=callback;
        db=MasterDatabase.getInstance(context);
    }


    @Override
    protected Object doInBackground(Object... objects) {

        if (operation == null)
            return null;

        switch (operation){
            case CREATE:
                return createTransaction((Transaction)objects[0]);
            case READ:
                return readTransaction((int)objects[0]);
            case READALL:
                return readAllTransactionsOfGroup((int)objects[0]);
            case UPDATE:
                return updateTransaction((Transaction)objects[0]);
            case DELETE:
                return deleteTransaction((Integer) objects[0]);
        }

        return null;
    }

    private long createTransaction(Transaction transaction){

        return db.getTransactionDao().addTransaction(transaction);
    }

    private Transaction readTransaction(int transactionId){

        Transaction trans=db.getTransactionDao().getTransaction(transactionId);
        trans.sender=db.getSenderDao().getSender(trans.senderId);
        trans.receiver=db.getReceiverDao().getReceiver(trans.receiverId);
        return trans;
    }

    private List<Transaction> readAllTransactionsOfGroup(int groupId){

        List<Transaction> transactions=db.getTransactionDao().getTransactionsNonLive(groupId);
        for(Transaction trans:transactions){

            trans.sender=db.getSenderDao().getSender(trans.senderId);
            trans.receiver=db.getReceiverDao().getReceiver(trans.receiverId);

        }
        return transactions;

    }


    private int updateTransaction(Transaction sender){

        return db.getTransactionDao().updateTransaction(sender);
    }

    private int deleteTransaction(int id){

        return db.getTransactionDao().deleteTransactionById(id);
    }

    @Override
    protected void onPostExecute(Object result) {
        if(callback==null)
            return;

        switch (operation){
            case CREATE:
                callback.onCreateComplete(((Long)result).intValue());
                break;
            case READ:
                callback.onReadComplete((Transaction)result);
                break;
            case READALL:
                callback.onReadAllComplete((List<Transaction>)result);
                break;
            case UPDATE:
                callback.onUpdateComplete((int)result);
                break;
            case DELETE:
                callback.onDeleteComplete((int)result);
        }
    }
}
