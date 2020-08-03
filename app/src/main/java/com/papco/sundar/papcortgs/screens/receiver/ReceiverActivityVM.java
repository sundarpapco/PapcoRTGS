package com.papco.sundar.papcortgs.screens.receiver;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.papco.sundar.papcortgs.common.Event;
import com.papco.sundar.papcortgs.database.common.TableWorkCallback;
import com.papco.sundar.papcortgs.database.receiver.ReceiverTableWorker;
import com.papco.sundar.papcortgs.database.common.MasterDatabase;
import com.papco.sundar.papcortgs.database.common.TableOperation;
import com.papco.sundar.papcortgs.database.receiver.Receiver;

import java.util.List;

public class ReceiverActivityVM extends AndroidViewModel implements TableWorkCallback {

    LiveData<List<Receiver>> receivers;
    Receiver editingReceiver=null;
    MasterDatabase db;

    public ReceiverActivityVM(@NonNull Application application) {
        super(application);
        db= MasterDatabase.getInstance(getApplication());
        receivers=db.getReceiverDao().getAllReceivers();

    }

    public LiveData<List<Receiver>> getReceivers() {
        return receivers;
    }
    public MutableLiveData<Event<Boolean>> shouldPopUpBackStack = new MutableLiveData<>();

    public void addReceiver(Receiver newReceiver){
        new ReceiverTableWorker(getApplication(), TableOperation.CREATE,this).execute(newReceiver);
    }

    public void updateReceiver(Receiver updateReceiver){
        new ReceiverTableWorker(getApplication(),TableOperation.UPDATE,this).execute(updateReceiver);
    }

    public void deleteReceiver(Receiver receiver){
        new ReceiverTableWorker(getApplication(),TableOperation.DELETE,null).execute(receiver);
    }


    @Override
    public void onCreateComplete(long result) {
        if(result==-1L) {
            Toast.makeText(getApplication(),"This beneficiary name already exists",Toast.LENGTH_SHORT).show();
            shouldPopUpBackStack.setValue(new Event<>(false));
        }else
            shouldPopUpBackStack.setValue(new Event<>(true));

    }

    @Override
    public void onReadComplete(Object result) {

    }

    @Override
    public void onReadAllComplete(Object result) {

    }

    @Override
    public void onUpdateComplete(int result) {
        if(result==-1) {
            Toast.makeText(getApplication(),"This beneficiary name already exists",Toast.LENGTH_SHORT).show();
            shouldPopUpBackStack.setValue(new Event<>(false));
        }else
            shouldPopUpBackStack.setValue(new Event<>(true));
    }

    @Override
    public void onDeleteComplete(int result) {

    }
}
