package com.papco.sundar.papcortgs.common;

import android.os.AsyncTask;

import com.papco.sundar.papcortgs.database.common.MasterDatabase;
import com.papco.sundar.papcortgs.database.transactionGroup.TransactionGroup;

public class ManualFileExporter extends AsyncTask<TransactionGroup,Void,String> {

    WriteFileListener listener;
    MasterDatabase db;
    String chequeNumber;

    public ManualFileExporter(MasterDatabase db, WriteFileListener listener,String chequeNumber){
        this.listener=listener;
        this.db=db;
        this.chequeNumber=chequeNumber;
    }

    @Override
    protected String doInBackground(TransactionGroup... transactionGroups) {

        String result;

        try{
            ManualRTGSReport report=new ManualRTGSReport(db,chequeNumber);
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
