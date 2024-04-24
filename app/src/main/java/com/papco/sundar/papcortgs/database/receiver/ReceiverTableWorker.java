package com.papco.sundar.papcortgs.database.receiver;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.papco.sundar.papcortgs.database.common.MasterDatabase;
import com.papco.sundar.papcortgs.database.common.TableOperation;
import com.papco.sundar.papcortgs.database.common.TableWorkCallback;

import java.util.List;
import java.util.Locale;

public class ReceiverTableWorker extends AsyncTask<Object, Void, Object> {

    Context context = null;
    TableOperation operation = null;
    TableWorkCallback callback = null;
    MasterDatabase db;

    public ReceiverTableWorker(Context context, TableOperation operation, TableWorkCallback callback) {
        this.operation = operation;
        this.context = context;
        this.callback = callback;
        db = MasterDatabase.getInstance(context);
    }


    @Override
    protected Object doInBackground(Object... objects) {

        if (operation == null || operation == TableOperation.READALL)
            return null;

        switch (operation) {
            case CREATE:
                return createReceiver((Receiver) objects[0]);
            case READ:
                return readReceiver((int) objects[0]);
            case UPDATE:
                return updateReceiver((Receiver) objects[0]);
            case DELETE:
                return deleteReceiver((Receiver) objects[0]);
        }

        return null;
    }

    private long createReceiver(Receiver receiver) {

        if (isNameAlreadyExists(receiver.displayName))
            return -1L;
        else
            return db.getReceiverDao().addReceiver(receiver);

    }

    private Receiver readReceiver(int receiverId) {

        return db.getReceiverDao().getReceiver(receiverId);
    }


    private int updateReceiver(Receiver receiver) {

        if (isNameAlreadyExistsExceptCurrent(receiver))
            return -1;
        else
            return db.getReceiverDao().updateReceiver(receiver);
    }

    private int deleteReceiver(Receiver receiver) {

        return db.getReceiverDao().deleteReceiver(receiver);
    }

    private boolean isNameAlreadyExists(String givenName) {

        String newNameInLowerCase = givenName.toLowerCase();
        String nameInLowerCase;
        List<String> receivers = db.getReceiverDao().getAllReceiverDisplayNames();
        for (String receiver : receivers) {
            nameInLowerCase = receiver.toLowerCase();
            if (newNameInLowerCase.equals(nameInLowerCase))
                return true;

        }
        return false;

    }

    @Override
    protected void onPostExecute(Object result) {
        if (callback == null)
            return;

        switch (operation) {
            case CREATE:
                callback.onCreateComplete((long) result);
                break;
            case READ:
                callback.onReadComplete((Receiver) result);
                break;
            case UPDATE:
                callback.onUpdateComplete((int) result);
                break;
            case DELETE:
                callback.onDeleteComplete((int) result);
        }
    }

    private boolean isNameAlreadyExistsExceptCurrent(Receiver updatingReceiver) {

        String newNameInLowerCase = updatingReceiver.name.toLowerCase();
        String nameInLowerCase;
        List<Receiver> receivers = db.getReceiverDao().getAllReceiversNonLive();
        for (Receiver receiver : receivers) {

            if (receiver.id != updatingReceiver.id) {
                nameInLowerCase = receiver.name.toLowerCase();
                if (newNameInLowerCase.equals(nameInLowerCase))
                    return true;
            }

        }
        return false;

    }
}
