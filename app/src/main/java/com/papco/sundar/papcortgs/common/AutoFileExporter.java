package com.papco.sundar.papcortgs.common;

import android.content.Context;
import android.os.AsyncTask;

import com.papco.sundar.papcortgs.database.common.MasterDatabase;
import com.papco.sundar.papcortgs.database.transactionGroup.TransactionGroup;

public class AutoFileExporter extends AsyncTask<TransactionGroup,Void,String> {

    Context context;
    WriteFileListener listener;
    MasterDatabase db;
    long time;

    public AutoFileExporter(
            Context context,
            MasterDatabase db,
            WriteFileListener listener,
            long time
    ){
        this.context=context;
        this.listener=listener;
        this.db=db;
        this.time=time;
    }

    @Override
    protected String doInBackground(TransactionGroup... transactionGroups) {

        String result;

        try{
            AutoRTGSReport report=new AutoRTGSReport(context,db,time);
            result=report.createReport(transactionGroups[0]);
        }catch (Exception e){
            e.printStackTrace();
            result="";
        }

        return result;
    }

    @Override
    protected void onPostExecute(String s) {
        if(listener!=null)
            listener.onWriteFileComplete(s);
    }
}
