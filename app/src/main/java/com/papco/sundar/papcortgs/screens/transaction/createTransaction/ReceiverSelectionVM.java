package com.papco.sundar.papcortgs.screens.transaction.createTransaction;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.papco.sundar.papcortgs.database.receiver.Receiver;
import java.util.List;


public class ReceiverSelectionVM extends AndroidViewModel
        implements FetchReceiversForGroupTask.ReceiverForSelectionCallBack {


    private MutableLiveData<List<Receiver>> receiversList = new MutableLiveData<>();

    public ReceiverSelectionVM(Application application) {
        super(application);
    }

    public void loadReceivers(int groupId) {
        FetchReceiversForGroupTask task = new FetchReceiversForGroupTask(getApplication(), this);
        task.execute(groupId);
    }

    public LiveData<List<Receiver>> getReceivers() {
        return receiversList;
    }


    @Override
    public void onReceiverForSelectionList(List<Receiver> receivers) {
        receiversList.setValue(receivers);
    }
}
