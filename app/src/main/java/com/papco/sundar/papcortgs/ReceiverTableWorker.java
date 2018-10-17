package com.papco.sundar.papcortgs;

import android.content.Context;
import android.os.AsyncTask;

public class ReceiverTableWorker extends AsyncTask<Object,Void,Object> {

    Context context=null;
    TableOperation operation=null;
    TableWorkCallback callback=null;
    MasterDatabase db;

    ReceiverTableWorker(Context context, TableOperation operation, TableWorkCallback callback){
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
                return createReceiver((Receiver)objects[0]);
            case READ:
                return readReceiver((int)objects[0]);
            case UPDATE:
                return updateReceiver((Receiver)objects[0]);
            case DELETE:
                return deleteReceiver((Receiver)objects[0]);
        }

        return null;
    }

    private long createReceiver(Receiver receiver){

        return db.getReceiverDao().addReceiver(receiver);
    }

    private Receiver readReceiver(int receiverId){

        return db.getReceiverDao().getReceiver(receiverId);
    }


    private int updateReceiver(Receiver receiver){

        return db.getReceiverDao().updateReceiver(receiver);
    }

    private int deleteReceiver(Receiver receiver){

        return db.getReceiverDao().deleteReceiver(receiver);
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
                callback.onReadComplete((Receiver)result);
                break;
            case UPDATE:
                callback.onUpdateComplete((int)result);
                break;
            case DELETE:
                callback.onDeleteComplete((int)result);
        }
    }
}
