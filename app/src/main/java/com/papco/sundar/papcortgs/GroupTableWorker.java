package com.papco.sundar.papcortgs;

import android.content.Context;
import android.os.AsyncTask;

public class GroupTableWorker extends AsyncTask<Object,Void,Object> {

    Context context=null;
    TableOperation operation=null;
    TableWorkCallback callback=null;
    MasterDatabase db;

    GroupTableWorker(Context context, TableOperation operation, TableWorkCallback callback){
        this.operation=operation;
        this.context=context;
        this.callback=callback;
        db=MasterDatabase.getInstance(context);
    }


    @Override
    protected Object doInBackground(Object... objects) {

        if (operation == null || operation==TableOperation.READALL)
            return null;

        switch (operation){
            case CREATE:
                return createTransactionGroup((TransactionGroup)objects[0]);
            case READ:
                return readTransactionGroup((int)objects[0]);
            case UPDATE:
                return updateTransactionGroup((TransactionGroup)objects[0]);
            case DELETE:
                return deleteTransactionGroup((TransactionGroup)objects[0]);
        }

        return null;
    }

    private long createTransactionGroup(TransactionGroup transactionGroup){

        return db.getTransactionGroupDao().addTransactionGroup(transactionGroup);
    }

    private TransactionGroup readTransactionGroup(int transactionGroupId){

        return db.getTransactionGroupDao().getTransactionGroup(transactionGroupId);
    }


    private int updateTransactionGroup(TransactionGroup transactionGroup){

        return db.getTransactionGroupDao().updateTransactionGroup(transactionGroup);
    }

    private int deleteTransactionGroup(TransactionGroup transactionGroup){

        return db.getTransactionGroupDao().deleteTransactionGroup(transactionGroup);
    }

    @Override
    protected void onPostExecute(Object result) {
        if(callback==null)
            return;

        switch (operation){
            case CREATE:
                callback.onCreateComplete((int)result);
                break;
            case READ:
                callback.onReadComplete((TransactionGroup)result);
                break;
            case UPDATE:
                callback.onUpdateComplete((int)result);
                break;
            case DELETE:
                callback.onDeleteComplete((int)result);
        }
    }


}
