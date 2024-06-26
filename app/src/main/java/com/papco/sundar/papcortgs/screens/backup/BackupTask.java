package com.papco.sundar.papcortgs.screens.backup;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.UploadBuilder;
import com.dropbox.core.v2.files.WriteMode;
import com.papco.sundar.papcortgs.database.common.MasterDatabase;
import com.papco.sundar.papcortgs.database.receiver.Receiver;
import com.papco.sundar.papcortgs.database.sender.Sender;
import com.papco.sundar.papcortgs.database.transaction.Transaction;
import com.papco.sundar.papcortgs.database.transactionGroup.TransactionGroup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
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

public class BackupTask extends AsyncTask<Void, String, Integer> {

    private String PATH_BACKUP_XL_FILE;
    private String PATH_DBX_BACKUP_XL_FILE="/apps/papcortgs/papcortgsbackup.xls";

    public static final int RESULT_COMPLETED=1;
    public static final int RESULT_NO_BACKUP_FOUND=2;
    public static final int RESULT_OTHER_FAILURE=3;

    BackupOperation operation;
    DbxClientV2 client;
    MasterDatabase db;
    WeakReference<BackupCallBack> callback;
    Context context;

    public BackupTask(Context context,DbxClientV2 client, BackupOperation opertation, BackupCallBack callBack) {

        this.operation = opertation;
        this.client = client;
        this.callback = new WeakReference<>(callBack);
        db= MasterDatabase.getInstance(context);
        this.context=context;

    }

    @Override
    protected void onPreExecute() {

        String dirPath=context.getCacheDir().getAbsolutePath();
        dirPath=dirPath+"/Temp";

        File sdDir=new File(dirPath);

        if(!sdDir.isDirectory())
            sdDir.mkdirs();

        PATH_BACKUP_XL_FILE=dirPath+"/papcortgsbackup.xls";
        /*
        String dirPath=Environment.getExternalStorageDirectory().getAbsolutePath();
        dirPath=dirPath+"/papcoRTGS/Temp";

        File sdDir=new File(dirPath);

        if(!sdDir.isDirectory())
            sdDir.mkdirs();

        PATH_BACKUP_XL_FILE=dirPath+"/papcortgsbackup.xls";

         */

    }

    @Override
    protected Integer doInBackground(Void... strings) {

        try {

            if(operation==BackupOperation.BACKUP)
                return backupFile();
            else
                return restoreFile();

        } catch (Exception e) {
            e.printStackTrace();
            return RESULT_OTHER_FAILURE;
        }

    }


    //******** Main calls for backup and restore

    private int backupFile() throws Exception {

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

        return RESULT_COMPLETED;

    }

    private int restoreFile() throws Exception {

        publishProgress("Checking for valid backup...");
        if(!whetherValidBackupExists()){

            Log.d("SUNDAR","BACKUP NOT FOUND");
            return RESULT_NO_BACKUP_FOUND;
        }

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

        return RESULT_COMPLETED;
    }


    // region ********** Methods for backup

    private void clearAllTables() throws Exception{

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

            if(receiver.email==null) //since its added in the later version, older objects may have null value
                receiver.email="";

            sheet.addCell(new Number(0,row,receiver.id,contentformat));
            sheet.addCell(new Label(1,row,receiver.accountType,contentformat));
            sheet.addCell(new Label(2,row,receiver.accountNumber,contentformat));
            sheet.addCell(new Label(3,row,receiver.name,contentformat));
            sheet.addCell(new Label(4,row,receiver.mobileNumber,contentformat));
            sheet.addCell(new Label(5,row,receiver.ifsc,contentformat));
            sheet.addCell(new Label(6,row,receiver.bank,contentformat));
            sheet.addCell(new Label(7,row,receiver.email,contentformat));
            sheet.addCell(new Label(8,row,receiver.displayName,contentformat));
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

            if(sender.email==null) //Since email was added in later version, older senders may have this as null
                sender.email="";

            sheet.addCell(new Number(0,row,sender.id,contentformat));
            sheet.addCell(new Label(1,row,sender.accountType,contentformat));
            sheet.addCell(new Label(2,row,sender.accountNumber,contentformat));
            sheet.addCell(new Label(3,row,sender.name,contentformat));
            sheet.addCell(new Label(4,row,sender.mobileNumber,contentformat));
            sheet.addCell(new Label(5,row,sender.ifsc,contentformat));
            sheet.addCell(new Label(6,row,sender.bank,contentformat));
            sheet.addCell(new Label(7,row,sender.email,contentformat));
            sheet.addCell(new Label(8,row,sender.displayName,contentformat));
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
            sheet.addCell(new Number(2,row,group.defaultSenderId,contentformat));
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


    //endregion *********************


    //region ************ Methods for Restore

    private boolean whetherValidBackupExists(){


        try{
            client.files().getMetadata(PATH_DBX_BACKUP_XL_FILE);
        }catch (Exception e){

            return false;

        }

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
            currentReceiver.email=sheet.getCell(7,row).getContents();

             /*
            A try block is required because this field wont exist in the backup file
            if the backup file is for older database version than the current version. So,
            in case the display name field is not there in the backup file, simply migrate by making
            the display name same as the account name field
             */
            try{
                currentReceiver.displayName=sheet.getCell(8,row).getContents();
            }catch(Exception e){
                currentReceiver.displayName=currentReceiver.name;
            }


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
            currentSender.email=sheet.getCell(7,row).getContents();

            /*
            A try block is required because this field wont exist in the backup file
            if the backup file is for older database version than the current version. So,
            in case the display name field is not there in the backup file, simply migrate by making
            the display name same as the account name field
             */
            try{
                currentSender.displayName=sheet.getCell(8,row).getContents();
            }catch(Exception e){
                currentSender.displayName=currentSender.name;
            }

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

            /*
            Using a special try catch block here. Because the defaultSenderId feature is added later
            in database migration Version 3. So, if we are restoring a old Version 2 database backup,
            then the column defaultSenderId wont exist there and thus will cause Index Out of bounds
            exception. We are catching that case manually if an exception occurs, then we are simply
            restoring a default value of 0
             */
            try {
                String defaultSender = sheet.getCell(2, row).getContents();
                currentGroup.defaultSenderId = Integer.parseInt(defaultSender);
            }catch (ArrayIndexOutOfBoundsException e){
                currentGroup.defaultSenderId=0;
            }
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

    //endregion **********************

    @Override
    protected void onProgressUpdate(String... values) {

        if(callback.get()!=null)
            callback.get().onProgessChanged(values[0]);
    }

    @Override
    protected void onPostExecute(Integer result) {

        String toastMsg;
        if(result==RESULT_COMPLETED){

            if(operation==BackupOperation.BACKUP)
                toastMsg="Backup Successful!";
            else
                toastMsg="Restore Successful!";

        }else{

            if(operation==BackupOperation.BACKUP)
                toastMsg="Backup failed";
            else
                toastMsg="Restore failed";

        }

        if(context!=null)
            Toast.makeText(context,toastMsg,Toast.LENGTH_SHORT).show();

        if(callback.get()!=null){

            if(operation==BackupOperation.BACKUP)
                callback.get().onBackupComplete(result);
            else
                callback.get().onRestoreComplete(result);

            return;

        }

    }


    public static interface BackupCallBack {

        public void onBackupComplete(int resultcode);
        public void onProgessChanged(String progress);
        public void onRestoreComplete(int resultcode);
    }
}
