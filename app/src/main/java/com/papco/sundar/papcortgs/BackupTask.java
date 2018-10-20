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

    private String PATH_BACKUP_XL_FILE;
    private String PATH_DBX_BACKUP_XL_FILE="/apps/papcortgs/papcortgsbackup.xls";

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

        PATH_BACKUP_XL_FILE=dirPath+"/papcortgsbackup.xls";

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

        //Create the backup file in local drive
        File receiversFile=new File(PATH_BACKUP_XL_FILE);

        //prepare and create the workbook and writable sheet
        WritableWorkbook workbook;
        WorkbookSettings wbSettings = new WorkbookSettings();
        wbSettings.setLocale(new Locale("en", "EN"));
        workbook = Workbook.createWorkbook(receiversFile, wbSettings);

        publishProgress("Backing up Receivers...");
        writeReceiverToWorkbook(workbook);

        publishProgress("Backing up Senders...");
        writeSendersToWorkbook(workbook);

        publishProgress("Backing up XL Sheets");
        writeGroupsToWorkBook(workbook);

        publishProgress("Backing up Transactions");
        writeTransactionsToWorkBook(workbook);

        workbook.write();
        workbook.close();

        publishProgress("Backing up to dropbox...");
        backupToDropBox();

        publishProgress("Clearing up temp files");
        deleteTempFiles();

        return true;

    }

    private boolean restoreFile() throws Exception {

        publishProgress("Downloading the backup file...");
        downloadFromDropBox();

        clearAllTables();

        Workbook workbook=Workbook.getWorkbook(new File(PATH_BACKUP_XL_FILE));

        publishProgress("Restoring Receivers...");
        restoreReceivers(workbook);

        publishProgress("Restoring Senders...");
        restoreSenders(workbook);

        publishProgress("Restoring Groups...");
        restoreGroups(workbook);

        publishProgress("Restoring Transactions");
        restoreTransactions(workbook);

        publishProgress("Clearing up temp files");
        deleteTempFiles();

        return true;
    }


    private void clearAllTables() throws Exception{

        Log.d("CLEARING","CLEARING ALL TABLES NOW");
        db.getReceiverDao().deleteAllReceivers();
        db.getSenderDao().deleteAllSenders();
        db.getTransactionGroupDao().deleteAllTransactionGroups();
        db.getTransactionDao().deleteAllTransactions();

    }

    private void writeReceiverToWorkbook(WritableWorkbook workbook) throws Exception{

        //prepare the list of receivers to backup
        List<Receiver> receivers=db.getReceiverDao().getAllReceiversNonLive();

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

        //write to the workbook
        //workbook.write();
        //workbook.close();

    }

    private void writeSendersToWorkbook(WritableWorkbook workbook) throws Exception{

        //prepare the list of senders to backup
        List<Sender> senders=db.getSenderDao().getAllSendersNonLive();

        WritableSheet sheet = workbook.createSheet("senders", 1);

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

        //write to the workbook
        //workbook.write();
        //workbook.close();


    }

    private void writeTransactionsToWorkBook(WritableWorkbook workbook) throws Exception{

        //prepare the list of transactions to backup
        List<Transaction> transactions=db.getTransactionDao().getAllTransactionsNonLive();

        WritableSheet sheet = workbook.createSheet("transactions", 3);

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
        //workbook.write();
        //workbook.close();

    }

    private void writeGroupsToWorkBook(WritableWorkbook workbook) throws Exception{

        //prepare the list of groups to backup
        List<TransactionGroup> groups=db.getTransactionGroupDao().getAllGroupsNonLive();

        WritableSheet sheet = workbook.createSheet("groups", 2);

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
        //workbook.write();
        //workbook.close();

    }

    private boolean backupToDropBox() throws Exception{

        File file=new File(PATH_BACKUP_XL_FILE);
        if(!file.exists())
            return false;
        InputStream in=new FileInputStream(file);

        UploadBuilder builder=client.files().uploadBuilder(PATH_DBX_BACKUP_XL_FILE);
        builder.withMode(WriteMode.OVERWRITE);
        builder.uploadAndFinish(in);

        return true;
    }



    private void downloadFromDropBox()throws Exception{

        //create the local file for downloading
        File file=new File(PATH_BACKUP_XL_FILE);
        OutputStream out=new FileOutputStream(file);

        client.files().download(PATH_DBX_BACKUP_XL_FILE).download(out);

    }


    private void restoreReceivers(Workbook workbook) throws Exception{

        List<Receiver> receivers =new ArrayList<>();
        boolean notReachedEnd;
        Cell currentCell;
        Receiver currentReceiver;

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

    }

    private void restoreSenders(Workbook workbook) throws Exception{

        List<Sender> senders =new ArrayList<>();
        boolean notReachedEnd;
        Cell currentCell;
        Sender currentSender;

        Sheet sheet=workbook.getSheet(1);

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

    }

    private void restoreGroups(Workbook workbook) throws Exception{

        List<TransactionGroup> groups =new ArrayList<>();
        boolean notReachedEnd;
        Cell currentCell;
        TransactionGroup currentGroup;

        Sheet sheet=workbook.getSheet(2);

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
    }

    private void restoreTransactions(Workbook workbook)throws Exception{

        List<Transaction> transactions =new ArrayList<>();
        boolean notReachedEnd;
        Cell currentCell;
        Transaction currentTrans;

        Sheet sheet=workbook.getSheet(3);

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
    }

    private void deleteTempFiles() throws Exception{

        File file;
        file=new File(PATH_BACKUP_XL_FILE);
        if(file.exists())
            file.delete();


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
