package com.papco.sundar.papcortgs;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.UploadBuilder;
import com.dropbox.core.v2.files.WriteMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Alignment;
import jxl.format.VerticalAlignment;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class BackupTask extends AsyncTask<Void, String, Boolean> {

    private String PATH_RECEIVER_XL_FILE;
    private String PATH_SENDER_XL_FILE;
    private String PATH_GROUP_XL_FILE;
    private String PATH_TRANSACTION_XL_FILE;

    private String PATH_DBX_RECEIVER_FILE="/apps/papcortgs/receivers.xls";
    private String PATH_DBX_SENDER_FILE="/apps/papcortgs/senders.xls";
    private String PATH_DBX_GROUP_XL_FILE="/apps/papcortgs/groups.xls";
    private String PATH_DBX_TRANSACTION_FILE="/apps/papcortgs/transactions.xls";

    BackupOperation operation;
    DbxClientV2 client;
    BackupCallBack callBack = null;
    MasterDatabase db;

    public BackupTask(Context context,DbxClientV2 client, BackupOperation opertation, BackupCallBack callBack) {

        this.operation = opertation;
        this.client = client;
        this.callBack = callBack;
        db=MasterDatabase.getInstance(context);

    }

    @Override
    protected void onPreExecute() {

        String dirPath=Environment.getExternalStorageDirectory().getAbsolutePath();
        dirPath=dirPath+"/papcoRTGS/Temp";

        File sdDir=new File(dirPath);

        if(!sdDir.isDirectory())
            sdDir.mkdirs();

        PATH_RECEIVER_XL_FILE=dirPath+"/receivers.xls";
        PATH_SENDER_XL_FILE=dirPath+"/senders.xls";
        PATH_GROUP_XL_FILE=dirPath+"/groups.xls";
        PATH_TRANSACTION_XL_FILE=dirPath+"/transactions.xls";

    }

    @Override
    protected Boolean doInBackground(Void... strings) {

        try {

            if(operation==BackupOperation.BACKUP)
                return backupFile();
            else
                return restoreFile();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    private boolean backupFile() throws Exception {

        if(!createReceiverXLFile())
            return false;

        if(!createSenderXLFile())
            return false;

        if(!createGroupXLFile())
            return false;

        if(!createTransactionXLFile())
            return false;


        publishProgress("Backing up receivers...");
        if(!backupReceiverFile())
            return false;

        publishProgress("Backing up senders...");
        if(!backupSenderFile())
            return false;

        publishProgress("Backing up XL sheets...");
        if(!backupGroupFile())
            return false;

        publishProgress("Backing up Transactions...");
        if(!backupTransactionFile())
            return false;

        publishProgress("Clearing up temp files");
        if(!deleteTempFiles())
            return false;


        return true;

    }

    private boolean restoreFile() throws Exception {

        publishProgress("Downloading receivers...");
        if(!downloadReceiversFile())
            return false;

        publishProgress("Downloading senders...");
        if(!downloadSendersFile())
            return false;

        publishProgress("Downloading XL Sheets...");
        if(!downloadGroupsFile())
            return false;

        publishProgress("Downloading transactions...");
        if(!downloadTransactionsFile())
            return false;

        clearAllTables();

        publishProgress("Restoring receivers...");
        if(!restoreReceivers())
            return false;

        publishProgress("Restoring senders...");
        if(!restoreSenders())
            return false;

        publishProgress("Restoring XL Sheets...");
        if(!restoreGroups())
            return false;

        publishProgress("Restoring Transactions...");
        if(!restoreTransactions())
            return false;

        publishProgress("Clearing temp files");
        if(!deleteTempFiles())
            return false;

        return true;
    }


    private void clearAllTables() throws Exception{

        Log.d("CLEARING","CLEARING ALL TABLES NOW");
        db.getReceiverDao().deleteAllReceivers();
        db.getSenderDao().deleteAllSenders();
        db.getTransactionGroupDao().deleteAllTransactionGroups();
        db.getTransactionDao().deleteAllTransactions();

    }

    private boolean createReceiverXLFile() throws Exception{

        //prepare the list of receivers to backup
        List<Receiver> receivers=db.getReceiverDao().getAllReceiversNonLive();

        //prepare and create the xl file to write receivers
        File receiversFile=new File(PATH_RECEIVER_XL_FILE);

        //prepare and create the workbook and writable sheet
        WritableWorkbook workbook = null;
        WorkbookSettings wbSettings = new WorkbookSettings();
        wbSettings.setLocale(new Locale("en", "EN"));
        workbook = Workbook.createWorkbook(receiversFile, wbSettings);
        WritableSheet sheet = workbook.createSheet("receivers", 0);

        //prepare the cellformat for writing
        WritableFont contentFont = new WritableFont(WritableFont.ARIAL, 10, WritableFont.NO_BOLD);
        WritableCellFormat contentformat = new WritableCellFormat(contentFont);
        contentformat.setAlignment(Alignment.CENTRE);
        contentformat.setVerticalAlignment(VerticalAlignment.BOTTOM);
        contentformat.setWrap(false);

        //write the receivers to the file
        int row=0;
        for(Receiver receiver:receivers){

            sheet.addCell(new Number(0,row,receiver.id,contentformat));
            sheet.addCell(new Label(1,row,receiver.accountType,contentformat));
            sheet.addCell(new Label(2,row,receiver.accountNumber,contentformat));
            sheet.addCell(new Label(3,row,receiver.name,contentformat));
            sheet.addCell(new Label(4,row,receiver.mobileNumber,contentformat));
            sheet.addCell(new Label(5,row,receiver.ifsc,contentformat));
            sheet.addCell(new Label(6,row,receiver.bank,contentformat));
            row++;

        }
        sheet.addCell(new Label(0,row,"--end--",contentformat));

        //write the workbook and close it
        workbook.write();
        workbook.close();

        return true;
    }

    private boolean createSenderXLFile() throws Exception{

        //prepare the list of senders to backup
        List<Sender> senders=db.getSenderDao().getAllSendersNonLive();

        //prepare and create the xl file to write senders
        File sendersFile=new File(PATH_SENDER_XL_FILE);

        //prepare and create the workbook and writable sheet
        WritableWorkbook workbook = null;
        WorkbookSettings wbSettings = new WorkbookSettings();
        wbSettings.setLocale(new Locale("en", "EN"));
        workbook = Workbook.createWorkbook(sendersFile, wbSettings);
        WritableSheet sheet = workbook.createSheet("senders", 0);

        //prepare the cellformat for writing
        WritableFont contentFont = new WritableFont(WritableFont.ARIAL, 10, WritableFont.NO_BOLD);
        WritableCellFormat contentformat = new WritableCellFormat(contentFont);
        contentformat.setAlignment(Alignment.CENTRE);
        contentformat.setVerticalAlignment(VerticalAlignment.BOTTOM);
        contentformat.setWrap(false);

        //write the senders to the file
        int row=0;
        for(Sender sender:senders){

            sheet.addCell(new Number(0,row,sender.id,contentformat));
            sheet.addCell(new Label(1,row,sender.accountType,contentformat));
            sheet.addCell(new Label(2,row,sender.accountNumber,contentformat));
            sheet.addCell(new Label(3,row,sender.name,contentformat));
            sheet.addCell(new Label(4,row,sender.mobileNumber,contentformat));
            sheet.addCell(new Label(5,row,sender.ifsc,contentformat));
            sheet.addCell(new Label(6,row,sender.bank,contentformat));
            row++;

        }
        sheet.addCell(new Label(0,row,"--end--",contentformat));

        //write the workbook and close it
        workbook.write();
        workbook.close();

        return true;

    }

    private boolean createTransactionXLFile() throws Exception{

        //prepare the list of transactions to backup
        List<Transaction> transactions=db.getTransactionDao().getAllTransactionsNonLive();

        //prepare and create the xl file to write transactions
        File transFile=new File(PATH_TRANSACTION_XL_FILE);

        //prepare and create the workbook and writable sheet
        WritableWorkbook workbook;
        WorkbookSettings wbSettings = new WorkbookSettings();
        wbSettings.setLocale(new Locale("en", "EN"));
        workbook = Workbook.createWorkbook(transFile, wbSettings);
        WritableSheet sheet = workbook.createSheet("transactions", 0);

        //prepare the cellformat for writing
        WritableFont contentFont = new WritableFont(WritableFont.ARIAL, 10, WritableFont.NO_BOLD);
        WritableCellFormat contentformat = new WritableCellFormat(contentFont);
        contentformat.setAlignment(Alignment.CENTRE);
        contentformat.setVerticalAlignment(VerticalAlignment.BOTTOM);
        contentformat.setWrap(false);

        //write the transactions to the file
        int row=0;
        for(Transaction trans:transactions){

            sheet.addCell(new Number(0,row,trans.id,contentformat));
            sheet.addCell(new Number(1,row,trans.groupId,contentformat));
            sheet.addCell(new Number(2,row,trans.senderId,contentformat));
            sheet.addCell(new Number(3,row,trans.receiverId,contentformat));
            sheet.addCell(new Number(4,row,trans.amount,contentformat));
            sheet.addCell(new Label(5,row,trans.remarks,contentformat));
            row++;

        }
        sheet.addCell(new Label(0,row,"--end--",contentformat));

        //write the workbook and close it
        workbook.write();
        workbook.close();

        return true;
    }

    private boolean createGroupXLFile() throws Exception{

        //prepare the list of groups to backup
        List<TransactionGroup> groups=db.getTransactionGroupDao().getAllGroupsNonLive();

        //prepare and create the xl file to write groups
        File groupsFile=new File(PATH_GROUP_XL_FILE);

        //prepare and create the workbook and writable sheet
        WritableWorkbook workbook = null;
        WorkbookSettings wbSettings = new WorkbookSettings();
        wbSettings.setLocale(new Locale("en", "EN"));
        workbook = Workbook.createWorkbook(groupsFile, wbSettings);
        WritableSheet sheet = workbook.createSheet("groups", 0);

        //prepare the cellformat for writing
        WritableFont contentFont = new WritableFont(WritableFont.ARIAL, 10, WritableFont.NO_BOLD);
        WritableCellFormat contentformat = new WritableCellFormat(contentFont);
        contentformat.setAlignment(Alignment.CENTRE);
        contentformat.setVerticalAlignment(VerticalAlignment.BOTTOM);
        contentformat.setWrap(false);

        //write the groups to the file
        int row=0;
        for(TransactionGroup group:groups){

            sheet.addCell(new Number(0,row,group.id,contentformat));
            sheet.addCell(new Label(1,row,group.name,contentformat));
            row++;

        }
        sheet.addCell(new Label(0,row,"--end--",contentformat));

        //write the workbook and close it
        workbook.write();
        workbook.close();

        return true;
    }

    private boolean backupReceiverFile() throws Exception{

        File file=new File(PATH_RECEIVER_XL_FILE);
        if(!file.exists())
            return false;
        InputStream in=new FileInputStream(file);

        UploadBuilder builder=client.files().uploadBuilder(PATH_DBX_RECEIVER_FILE);
        builder.withMode(WriteMode.OVERWRITE);
        builder.uploadAndFinish(in);

        return true;
    }

    private boolean backupSenderFile() throws Exception{

        File file=new File(PATH_SENDER_XL_FILE);
        if(!file.exists())
            return false;
        InputStream in=new FileInputStream(file);

        UploadBuilder builder=client.files().uploadBuilder(PATH_DBX_SENDER_FILE);
        builder.withMode(WriteMode.OVERWRITE);
        builder.uploadAndFinish(in);

        return true;

    }

    private boolean backupGroupFile() throws Exception{

        File file=new File(PATH_GROUP_XL_FILE);
        if(!file.exists())
            return false;
        InputStream in=new FileInputStream(file);

        UploadBuilder builder=client.files().uploadBuilder(PATH_DBX_GROUP_XL_FILE);
        builder.withMode(WriteMode.OVERWRITE);
        builder.uploadAndFinish(in);

        return true;
    }

    private boolean backupTransactionFile() throws Exception{

        File file=new File(PATH_TRANSACTION_XL_FILE);
        if(!file.exists())
            return false;
        InputStream in=new FileInputStream(file);

        UploadBuilder builder=client.files().uploadBuilder(PATH_DBX_TRANSACTION_FILE);
        builder.withMode(WriteMode.OVERWRITE);
        builder.uploadAndFinish(in);

        return true;

    }


    private boolean downloadReceiversFile()throws Exception{

        //create the local file for downloading
        File file=new File(PATH_RECEIVER_XL_FILE);
        OutputStream out=new FileOutputStream(file);

        client.files().download(PATH_DBX_RECEIVER_FILE).download(out);

        return true;
    }

    private boolean downloadSendersFile() throws Exception{

        //create the local file for downloading
        File file=new File(PATH_SENDER_XL_FILE);
        OutputStream out=new FileOutputStream(file);

        client.files().download(PATH_DBX_SENDER_FILE).download(out);

        return true;

    }

    private boolean downloadGroupsFile() throws Exception{

        //create the local file for downloading
        File file=new File(PATH_GROUP_XL_FILE);
        OutputStream out=new FileOutputStream(file);

        client.files().download(PATH_DBX_GROUP_XL_FILE).download(out);

        return true;
    }

    private boolean downloadTransactionsFile() throws Exception{

        //create the local file for downloading
        File file=new File(PATH_TRANSACTION_XL_FILE);
        OutputStream out=new FileOutputStream(file);

        client.files().download(PATH_DBX_TRANSACTION_FILE).download(out);

        return true;
    }

    private boolean restoreReceivers() throws Exception{

        List<Receiver> receivers =new ArrayList<>();
        boolean notReachedEnd;
        Cell currentCell;
        Receiver currentReceiver;

        Workbook workbook=Workbook.getWorkbook(new File(PATH_RECEIVER_XL_FILE));
        Sheet sheet=workbook.getSheet(0);

        notReachedEnd=true;
        int row=0;
        while(notReachedEnd){

            currentCell=sheet.getCell(0,row);
            if(currentCell.getContents().equals("--end--")){
                notReachedEnd=false;
                break;
            }

            currentReceiver=new Receiver();
            currentReceiver.id=Integer.parseInt(sheet.getCell(0,row).getContents());
            currentReceiver.accountType=sheet.getCell(1,row).getContents();
            currentReceiver.accountNumber=sheet.getCell(2,row).getContents();
            currentReceiver.name=sheet.getCell(3,row).getContents();
            currentReceiver.mobileNumber=sheet.getCell(4,row).getContents();
            currentReceiver.ifsc=sheet.getCell(5,row).getContents();
            currentReceiver.bank=sheet.getCell(6,row).getContents();

            receivers.add(currentReceiver);
            row++;

        }

        db.getReceiverDao().addAllReceivers(receivers);


        return true;
    }

    private boolean restoreSenders() throws Exception{

        List<Sender> senders =new ArrayList<>();
        boolean notReachedEnd;
        Cell currentCell;
        Sender currentSender;

        Workbook workbook=Workbook.getWorkbook(new File(PATH_SENDER_XL_FILE));
        Sheet sheet=workbook.getSheet(0);

        notReachedEnd=true;
        int row=0;
        while(notReachedEnd){

            currentCell=sheet.getCell(0,row);
            if(currentCell.getContents().equals("--end--")){
                notReachedEnd=false;
                break;
            }

            currentSender=new Sender();
            currentSender.id=Integer.parseInt(sheet.getCell(0,row).getContents());
            currentSender.accountType=sheet.getCell(1,row).getContents();
            currentSender.accountNumber=sheet.getCell(2,row).getContents();
            currentSender.name=sheet.getCell(3,row).getContents();
            currentSender.mobileNumber=sheet.getCell(4,row).getContents();
            currentSender.ifsc=sheet.getCell(5,row).getContents();
            currentSender.bank=sheet.getCell(6,row).getContents();

            senders.add(currentSender);
            row++;

        }

        db.getSenderDao().addAllSenders(senders);
        return true;

    }

    private boolean restoreGroups() throws Exception{

        List<TransactionGroup> groups =new ArrayList<>();
        boolean notReachedEnd;
        Cell currentCell;
        TransactionGroup currentGroup;

        Workbook workbook=Workbook.getWorkbook(new File(PATH_GROUP_XL_FILE));
        Sheet sheet=workbook.getSheet(0);

        notReachedEnd=true;
        int row=0;
        while(notReachedEnd){

            currentCell=sheet.getCell(0,row);
            if(currentCell.getContents().equals("--end--")){
                notReachedEnd=false;
                break;
            }

            currentGroup=new TransactionGroup();
            currentGroup.id=Integer.parseInt(sheet.getCell(0,row).getContents());
            currentGroup.name=sheet.getCell(1,row).getContents();

            groups.add(currentGroup);
            row++;

        }

        db.getTransactionGroupDao().addAllTransactionGroups(groups);
        return true;
    }

    private boolean restoreTransactions()throws Exception{

        List<Transaction> transactions =new ArrayList<>();
        boolean notReachedEnd;
        Cell currentCell;
        Transaction currentTrans;

        Workbook workbook=Workbook.getWorkbook(new File(PATH_TRANSACTION_XL_FILE));
        Sheet sheet=workbook.getSheet(0);

        notReachedEnd=true;
        int row=0;
        while(notReachedEnd) {

            currentCell = sheet.getCell(0, row);
            if (currentCell.getContents().equals("--end--")) {
                notReachedEnd = false;
                break;
            }

            currentTrans = new Transaction();
            currentTrans.id = Integer.parseInt(sheet.getCell(0, row).getContents());
            currentTrans.groupId = Integer.parseInt(sheet.getCell(1, row).getContents());
            currentTrans.senderId = Integer.parseInt(sheet.getCell(2, row).getContents());
            currentTrans.receiverId = Integer.parseInt(sheet.getCell(3, row).getContents());
            currentTrans.amount = Integer.parseInt(sheet.getCell(4, row).getContents());
            currentTrans.remarks = sheet.getCell(5, row).getContents();

            transactions.add(currentTrans);
            row++;
        }

        db.getTransactionDao().addAllTransactions(transactions);
        return true;
    }

    private boolean deleteTempFiles() throws Exception{

        File file;
        file=new File(PATH_RECEIVER_XL_FILE);
        if(file.exists())
            file.delete();

        file=new File(PATH_SENDER_XL_FILE);
        if(file.exists())
            file.delete();

        file=new File(PATH_GROUP_XL_FILE);
        if(file.exists())
            file.delete();

        file=new File(PATH_TRANSACTION_XL_FILE);
        if(file.exists())
            file.delete();

        return true;

    }



    @Override
    protected void onProgressUpdate(String... values) {
        if(callBack==null)
            return;

        callBack.onProgessChanged(values[0]);
    }

    @Override
    protected void onPostExecute(Boolean result) {

        if (callBack == null)
            return;

        if(operation==BackupOperation.BACKUP)
            callBack.onBackupComplete(result);
        else
            callBack.onRestoreComplete(result);

    }


    public static interface BackupCallBack {

        public void onBackupComplete(boolean success);
        public void onProgessChanged(String progress);
        public void onRestoreComplete(boolean success);
    }
}
