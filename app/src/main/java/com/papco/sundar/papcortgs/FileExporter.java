package com.papco.sundar.papcortgs;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.VerticalAlignment;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class FileExporter extends AsyncTask<TransactionGroup, Void, String> {

    Context context;
    List<Transaction> transactions;
    WriteFileListener listener=null;
    MasterDatabase db;
    ArrayList<ColumnDetail> colDetails=new ArrayList<>(13);

    public FileExporter(Context context,WriteFileListener listener) {
        this.context = context;
        db=MasterDatabase.getInstance(context);
        this.listener=listener;

    }

    @Override
    protected String doInBackground(TransactionGroup... transactionGroups) {

        String resultPath="";

        try {

            loadTransactions(transactionGroups[0].id);
            loadDefaultColWidths();
            resultPath=writeToFile(transactionGroups[0].name);

        } catch (Exception e) {

            e.printStackTrace();
            resultPath = "";
        }

        return resultPath;
    }


    private void loadDefaultColWidths(){

        colDetails.add(0,new ColumnDetail(5));
        colDetails.add(1,new ColumnDetail(10));
        colDetails.add(2,new ColumnDetail(15));
        colDetails.add(3,new ColumnDetail(15));
        colDetails.add(4,new ColumnDetail(33));
        colDetails.add(5,new ColumnDetail(15));
        colDetails.add(6,new ColumnDetail(33));
        colDetails.add(7,new ColumnDetail(15));
        colDetails.add(8,new ColumnDetail(15));
        colDetails.add(9,new ColumnDetail(18));
        colDetails.add(10,new ColumnDetail(30));
        colDetails.add(11,new ColumnDetail(15));
        colDetails.add(12,new ColumnDetail(15));

    }

    private void setColumWidths(WritableSheet sheet){

        int i =0;
        for(ColumnDetail col:colDetails){

            if(col.recommendedWidth>col.minimumWidth)
                sheet.setColumnView(i, col.recommendedWidth); //setting the column width
            else
                sheet.setColumnView(i,col.minimumWidth);

            i++;

        }

    }

    private void loadTransactions(int groupId){

        transactions=db.getTransactionDao().getTransactionsNonLive(groupId);
        for(Transaction t:transactions){

            t.sender=db.getSenderDao().getSender(t.senderId);
            t.receiver=db.getReceiverDao().getReceiver(t.receiverId);
        }
    }

    private String writeToFile(String fileName) throws Exception {

        //Prepare the path and verify that the path exists

        File sd = Environment.getExternalStorageDirectory();
        String appDirectory = sd.getAbsolutePath() + "/papcoRTGS";
        fileName = fileName + ".xls";

        File directory = new File(appDirectory);

        //create directory if not exist
        if (!directory.isDirectory()) {
            directory.mkdirs();
        }
        WritableWorkbook workbook = null;

        //prepare the file and workbook settings
        File file = new File(directory, fileName);
        WorkbookSettings wbSettings = new WorkbookSettings();
        wbSettings.setLocale(new Locale("en", "EN"));

        //Create the workbook and first sheet
        workbook = Workbook.createWorkbook(file, wbSettings);
        WritableSheet sheet = workbook.createSheet("payments", 0);

        writeHeadings(sheet);
        writeTransactions(sheet);
        setColumWidths(sheet);

        workbook.write();
        workbook.close();
        return fileName;

    }

    private void writeTransactions(WritableSheet sheet) throws Exception {

        //prepare the content format for the cells
        WritableFont contentFont = new WritableFont(WritableFont.ARIAL, 10, WritableFont.NO_BOLD);
        WritableCellFormat contentformat = new WritableCellFormat(contentFont);
        contentformat.setAlignment(Alignment.CENTRE);
        contentformat.setVerticalAlignment(VerticalAlignment.BOTTOM);
        contentformat.setWrap(false);
        contentformat.setBorder(Border.ALL, BorderLineStyle.THIN);


        int row = 1;
        int totalAmount = 0;
        for (Transaction trans : transactions) {

            sheet.addCell(new Label(0, row, "", contentformat));

            calculateColumnWidth(1,Integer.toString(trans.amount));
            sheet.addCell(new Number(1, row, trans.amount, contentformat));

            calculateColumnWidth(2,trans.sender.accountType);
            sheet.addCell(new Label(2, row, trans.sender.accountType, contentformat));

            calculateColumnWidth(3,trans.sender.accountNumber);
            sheet.addCell(new Label(3, row, trans.sender.accountNumber, contentformat));

            calculateColumnWidth(4,trans.sender.name);
            sheet.addCell(new Label(4, row, trans.sender.name, contentformat));

            calculateColumnWidth(5,trans.sender.mobileNumber);
            sheet.addCell(new Label(5, row, trans.sender.mobileNumber, contentformat));

            calculateColumnWidth(6,trans.sender.name);
            sheet.addCell(new Label(6, row, trans.sender.name, contentformat));

            calculateColumnWidth(7,trans.receiver.ifsc);
            sheet.addCell(new Label(7, row, trans.receiver.ifsc, contentformat));

            calculateColumnWidth(8,trans.receiver.accountType);
            sheet.addCell(new Label(8, row, trans.receiver.accountType, contentformat));

            calculateColumnWidth(9,trans.receiver.accountNumber);
            sheet.addCell(new Label(9, row, trans.receiver.accountNumber, contentformat));

            calculateColumnWidth(10,trans.receiver.name);
            sheet.addCell(new Label(10, row, trans.receiver.name, contentformat));

            calculateColumnWidth(11,trans.remarks);
            sheet.addCell(new Label(11, row, trans.remarks, contentformat));

            calculateColumnWidth(12,trans.receiver.bank);
            sheet.addCell(new Label(12, row, trans.receiver.bank, contentformat));

            totalAmount += trans.amount;
            row++;

        }

        //write the total amount
        calculateColumnWidth(1,Integer.toString(totalAmount));
        sheet.addCell(new Number(1, row, totalAmount, contentformat));

    }


    private void writeHeadings(WritableSheet sheet) throws Exception {

        //prepare the heading format for the cells
        WritableFont headingFont = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
        WritableCellFormat headingformat = new WritableCellFormat(headingFont);
        headingformat.setAlignment(Alignment.CENTRE);
        headingformat.setWrap(true);
        headingformat.setBorder(Border.ALL, BorderLineStyle.THIN);

        //begin writing the column
        sheet.setColumnView(0, 5); //setting the column width
        sheet.addCell(new Label(0, 0, "TRAN ID", headingformat));

        sheet.setColumnView(1, 10); //setting the column width
        sheet.addCell(new Label(1, 0, "AMOUNT", headingformat));

        sheet.setColumnView(2, 15); //setting the column width
        sheet.addCell(new Label(2, 0, "SENDER ACCOUNT TYPE", headingformat));

        sheet.setColumnView(3, 15); //setting the column width
        sheet.addCell(new Label(3, 0, "SENDER ACCOUNT NUMBER", headingformat));

        sheet.setColumnView(4, 33); //setting the column width
        sheet.addCell(new Label(4, 0, "SENDER NAME", headingformat));

        sheet.setColumnView(5, 15); //setting the column width
        sheet.addCell(new Label(5, 0, "SMS EML", headingformat));

        sheet.setColumnView(6, 33); //setting the column width
        sheet.addCell(new Label(6, 0, "SENDER NAME", headingformat));

        sheet.setColumnView(7, 15); //setting the column width
        sheet.addCell(new Label(7, 0, "IFS CODE NO", headingformat));

        sheet.setColumnView(8, 15); //setting the column width
        sheet.addCell(new Label(8, 0, "BENEFICIARY ACCOUNT TYPE", headingformat));

        sheet.setColumnView(9, 18); //setting the column width
        sheet.addCell(new Label(9, 0, "ACCOUNT NUMBER", headingformat));

        sheet.setColumnView(10, 30); //setting the column width
        sheet.addCell(new Label(10, 0, "NAME OF THE BENEFICIARY", headingformat));

        sheet.setColumnView(11, 15); //setting the column width
        sheet.addCell(new Label(11, 0, "SENDER TO RECEIVER INFORMATION", headingformat));

        sheet.setColumnView(12, 15); //setting the column width
        sheet.addCell(new Label(12, 0, "NAME OF THE BANK & BRANCH", headingformat));


    }

    private void calculateColumnWidth(int position,String matter){

        int length=matter.length()+8;
        if(length > colDetails.get(position).recommendedWidth)
            colDetails.get(position).recommendedWidth=length;

    }


    @Override
    protected void onPostExecute(String result) {

        if(listener!=null)
            listener.onWriteFileComplete(result);

    }

    public static interface WriteFileListener{

        public void onWriteFileComplete(String filename);
    }

    public static class ColumnDetail{

        public ColumnDetail(int mWidth){
            this.minimumWidth=mWidth;
            recommendedWidth=mWidth;
        }

        int minimumWidth;
        int recommendedWidth;
    }
}
