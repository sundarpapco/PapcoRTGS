package com.papco.sundar.papcortgs.screens.transaction.createTransaction;

import android.content.Context;
import android.os.AsyncTask;

import com.papco.sundar.papcortgs.database.common.MasterDatabase;
import com.papco.sundar.papcortgs.database.receiver.Receiver;

import java.util.List;

public class FetchReceiversForGroupTask extends AsyncTask<Integer,Void, List<Receiver>> {

    MasterDatabase db;
    ReceiverForSelectionCallBack callBack;

    public FetchReceiversForGroupTask(Context context,ReceiverForSelectionCallBack callBack){
        db=MasterDatabase.getInstance(context);
        this.callBack=callBack;
    }

    @Override
    protected List<Receiver> doInBackground(Integer... ids) {
        return filterReceivers(
                getAllReceivers(),
                getMembersOfTheGroup(ids[0])
        );
    }

    @Override
    protected void onPostExecute(List<Receiver> receivers) {
        if(callBack!=null)
            callBack.onReceiverForSelectionList(receivers);
    }

    private List<Receiver> filterReceivers(List<Receiver> receivers, List<Integer> membersOfGroup){
        for(Receiver receiver:receivers){
            for(int id:membersOfGroup){
                if(receiver.id==id){
                    receiver.accountNumber="-1";
                    break;
                }
            }
        }
        return receivers;
    }

    private List<Receiver> getAllReceivers(){
        return db.getReceiverDao().getAllReceiversNonLive();
    }

    private List<Integer> getMembersOfTheGroup(Integer groupId){
        return db.getTransactionDao().getReceiverIdsOfGroup(groupId);
    }

    interface ReceiverForSelectionCallBack{
        void onReceiverForSelectionList(List<Receiver> receivers);
    }

}
