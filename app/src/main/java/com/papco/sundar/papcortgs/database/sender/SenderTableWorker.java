package com.papco.sundar.papcortgs.database.sender;

import android.content.Context;
import android.os.AsyncTask;

import com.papco.sundar.papcortgs.database.common.MasterDatabase;
import com.papco.sundar.papcortgs.database.common.TableOperation;
import com.papco.sundar.papcortgs.database.common.TableWorkCallback;

public class SenderTableWorker extends AsyncTask<Object,Void,Object> {

    Context context=null;
    TableOperation operation=null;
    TableWorkCallback callback=null;
    MasterDatabase db;

    public SenderTableWorker(Context context, TableOperation operation, TableWorkCallback callback){
        this.operation=operation;
        this.context=context;
        this.callback=callback;
        db= MasterDatabase.getInstance(context);
    }


    @Override
    protected Object doInBackground(Object... objects) {

        if (operation == null || operation==TableOperation.READALL)
            return null;

        switch (operation){
            case CREATE:
                return createSender((Sender)objects[0]);
            case READ:
                return readSender((int)objects[0]);
            case UPDATE:
                return updateSender((Sender)objects[0]);
            case DELETE:
                return deleteSender((Sender)objects[0]);
        }

        return null;
    }

    private long createSender(Sender sender){

        return db.getSenderDao().addSender(sender);
    }

    private Sender readSender(int SenderId){

        return db.getSenderDao().getSender(SenderId);
    }


    private int updateSender(Sender sender){

        return db.getSenderDao().updateSender(sender);
    }

    private int deleteSender(Sender sender){

        return db.getSenderDao().deleteSender(sender);
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
                callback.onReadComplete((Sender)result);
                break;
            case UPDATE:
                callback.onUpdateComplete((int)result);
                break;
            case DELETE:
                callback.onDeleteComplete((int)result);
        }
    }
}
