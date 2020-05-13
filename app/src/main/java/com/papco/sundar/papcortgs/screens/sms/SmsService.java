package com.papco.sundar.papcortgs.screens.sms;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.arch.lifecycle.MutableLiveData;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;

import com.papco.sundar.papcortgs.screens.transactionGroup.GroupActivity;
import com.papco.sundar.papcortgs.R;
import com.papco.sundar.papcortgs.common.TextFunctions;
import com.papco.sundar.papcortgs.database.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SmsService extends Service {

    public static final String ACTION_START_SERVICE="com.papco.sundar.papcortgs.startsmsservice";
    public static final String ACTION_STOP_SERVICE="com.papco.sundar.papcortgs.stopsmsservice";
    public static boolean IS_SERVICE_RUNNING=false;
    private static final int NOTIFICATION_ID=1;

    private static final String SMS_SENT_ACTION = "com.papco.sundar.papcortgs.SMS_SENT";
    private static final String EXTRA_MODE = "mode";
    private static final String EXTRA_TRANS_ID="transId";

    public static final int SMS_STATUS_SENT=1;
    public static final int SMS_STATUS_NOT_ATTEMPTED=-1;
    public static final int SMS_STATUS_RECEIVER_NUMBER_INVALID=4;
    public static final int SMS_STATUS_PERMISSION_ERROR=5;
    public static final int SMS_STATUS_NO_SERVICE=6;
    public static final int SMS_STATUS_GENERIC_FAILURE=7;
    public static final int SMS_STATUS_UNKNOWN_ERROR=8;
    public static final int SMS_STATUS_TIMEOUT=9;

    public static final int WORK_STATUS_WAITING_FOR_LIST=3;
    public static final int WORK_STATUS_WORKING=1;
    public static final int WORK_STATUS_COMPLETED=2;

    private List<Transaction> smsList=null;
    private int currentGroupId=-1; //for creating the pending Intent to launch when tap notification
    private String currentGroupName=null;

    SmsBinder binder;
    private SmsManager smsManager;
    NotificationCompat.Builder builder;
    private MutableLiveData<List<Transaction>> transactions;
    private MutableLiveData<Integer> workingStatus;
    private SmsResultReceiver receiver;
    private boolean encounteredError=false;
    private Timer timer;
    private SharedPreferences pref;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent.getAction().equals(ACTION_START_SERVICE)){

            if(IS_SERVICE_RUNNING) //ignore if the service is being started for the second time
                return START_STICKY;

            if(intent.getExtras()!=null) { //group id needed for preparing the pendingIntent of notification
                currentGroupId = intent.getExtras().getInt("groupId", -1);
                currentGroupName=intent.getExtras().getString("groupName");
            }

            IS_SERVICE_RUNNING=true;
            showNotification(); //this will make the service forground

            if(transactions==null) {
                transactions = new MutableLiveData<>();
            }
            if(workingStatus==null){
                workingStatus=new MutableLiveData<>();
                workingStatus.setValue(WORK_STATUS_WAITING_FOR_LIST);
            }
            smsManager=SmsManager.getDefault();

        }

        if(intent.getAction().equals(ACTION_STOP_SERVICE)){
            stopTheService();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        IS_SERVICE_RUNNING=false;
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if(binder==null)
            binder=new SmsBinder();

        return binder;
    }



    private void showNotification() {

        Intent intent=new Intent(this, ActivitySMS.class);
        Bundle b=new Bundle();
        b.putInt("groupId",currentGroupId);
        intent.putExtras(b);

        TaskStackBuilder taskBuilder=TaskStackBuilder.create(this);
        taskBuilder.addNextIntentWithParentStack(intent);


        Intent transactionIntent=taskBuilder.editIntentAt(1);
        Bundle options=new Bundle();
        options.putInt("groupId",currentGroupId);
        options.putString("groupName",currentGroupName);
        transactionIntent.putExtras(options);
        PendingIntent resultPendingIntent=taskBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);

        builder=new NotificationCompat.Builder(this, GroupActivity.NOTIFICATION_CHANNEL_ID);
        builder.setSmallIcon(R.drawable.app_icon);
        builder.setContentTitle("Sending SMS...");
        builder.setProgress(100,0,true);
        builder.setPriority(NotificationCompat.PRIORITY_LOW);
        builder.setContentIntent(resultPendingIntent);
        startForeground(1,builder.build());

    }

    public MutableLiveData<List<Transaction>> getTransactions(){

        return transactions;
    }

    public void setListAndStartSending(List<Transaction> list){

        if(transactions.getValue()!=null) //just ignore this call if a list has been already set
            return;

        transactions.setValue(list);
        this.smsList=list;
        startSendingMessages();
    }

    private void startSendingMessages(){

        workingStatus.setValue(WORK_STATUS_WORKING);
        if(receiver==null)
            receiver=new SmsResultReceiver();
        registerReceiver(receiver,new IntentFilter(SMS_SENT_ACTION));
        sendNextMessage();

    }

    public MutableLiveData<Integer> getWorkingStatus(){
        return workingStatus;
    }

    private void updateProgressBar(int currentProgress) {

        if(currentProgress>=smsList.size()){
            builder.setContentTitle("Sending sms complete");
            builder.setProgress(0,0,false); //remove the progressbar
            if(encounteredError)
                builder.setContentText("Sending Sms completed with some errors! Tap for info");
            else
                builder.setContentText("Successfully sent Sms to beneficiaries");

        }else {
            builder.setProgress(smsList.size(), currentProgress, false);
            builder.setContentTitle("Sending sms " + Integer.toString(currentProgress + 1) + " of " + Integer.toString(smsList.size()));
        }

        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID,builder.build());

    }

    private boolean isValidMobileNumber(String number){

        if(number.trim().length()!=10)
            return false;

        if(!TextUtils.isDigitsOnly(number))
            return false;

        return true;

    }

    private String composeMessage(Transaction trans){

        if(pref==null)
            pref=getSharedPreferences("mysettings",MODE_PRIVATE);

        String format=pref.getString("message_template","NULL");
        format = format.replaceAll(TextFunctions.TAG_RECEIVER_ACC_NAME,trans.receiver.name);
        format = format.replaceAll(TextFunctions.TAG_RECEIVER_ACC_NUMBER,trans.receiver.accountNumber);
        format = format.replaceAll(TextFunctions.TAG_AMOUNT,Transaction.amountAsString(trans.amount));
        format = format.replaceAll(TextFunctions.TAG_RECEIVER_BANK,trans.receiver.bank);
        format = format.replaceAll(TextFunctions.TAG_RECEIVER_IFSC,trans.receiver.ifsc);
        format = format.replaceAll(TextFunctions.TAG_SENDER_NAME,trans.sender.name);
        return format;


    }

    private void sendNextMessage() {

        int requestCode;

        int position;
        for(position=0;position<smsList.size();position++) {

            Transaction trans=smsList.get(position);

            //make sure that we have not sent message to this person already
            if(trans.smsStatus!=SMS_STATUS_NOT_ATTEMPTED) {
                continue;
            }

            updateProgressBar(position);

            //First check if the receiver mobile number is valid
            if(!isValidMobileNumber(trans.receiver.mobileNumber)){
                trans.smsStatus=SMS_STATUS_RECEIVER_NUMBER_INVALID;
                transactions.setValue(smsList);
                encounteredError=true;
                continue;
            }

            //we have a valid number. send the message
            requestCode=trans.id;
            Intent sentIntent = new Intent(SMS_SENT_ACTION);

            String number = trans.receiver.mobileNumber;
            String message = composeMessage(trans);
            sentIntent.putExtra(EXTRA_TRANS_ID,trans.id);
            sentIntent.putExtra(EXTRA_MODE,"multi");


            // Construct the PendingIntents for the results.
            // FLAG_ONE_SHOT cancels the PendingIntent after use so we
            // can safely reuse the request codes in subsequent runs.
            PendingIntent sentPI = PendingIntent.getBroadcast(this,
                    requestCode,
                    sentIntent,
                    PendingIntent.FLAG_ONE_SHOT);


            try {
                // Send our message.

                if(message.length()>160) {

                    ArrayList<String> parts = smsManager.divideMessage(message);
                    ArrayList<PendingIntent> sentIntents = new ArrayList<>();
                    sentIntents.add(sentPI);
                    smsManager.sendMultipartTextMessage(number, null, parts, sentIntents, null);
                }else{

                    smsManager.sendTextMessage(number,null,message,sentPI,null);

                }
                //schedule a timer. this timer will explode and finish the work if the
                //sent sms broadcast was not received within 5 seconds
                //on receiving an broadcast, this timer will be cancelled there
                timer=new Timer();
                timer.schedule(new TimeOutTask(),5000);
                return;

            } catch (Exception e) {
                trans.smsStatus=SMS_STATUS_PERMISSION_ERROR;
                transactions.setValue(smsList);
                encounteredError=true;
                e.printStackTrace();

            }
        }

        completeWork(true);

    }

    private void doTimeoutAndFinish(){

        //mark all transactions which was never attempted to timeout
        for(Transaction trans:smsList){
            if(trans.smsStatus==SMS_STATUS_NOT_ATTEMPTED)
                trans.smsStatus=SMS_STATUS_TIMEOUT;
        }
        //mark that we have encountered error during send
        encounteredError=true;
        completeWork(false);

    }

    private void completeWork(boolean isMainThread){

        //mention that the work has been completed
        //but dont stop the service here. SMS Activity will stop the service
        //after getting the result from it
        if(isMainThread)
            workingStatus.setValue(WORK_STATUS_COMPLETED);
        else
            workingStatus.postValue(WORK_STATUS_COMPLETED);

        updateProgressBar(smsList.size());
    }

    private void stopTheService(){

        IS_SERVICE_RUNNING=false;
        stopForeground(true);
        stopSelf();

    }


    public class SmsBinder extends Binder{

        public SmsService getService(){

            return SmsService.this;
        }
    }

    private class SmsResultReceiver extends BroadcastReceiver {


        @Override
        public void onReceive(Context context, Intent intent) {

            //First of all, cancel the timeout timer since we got a broadcast
            timer.cancel();

            // A simple result Toast text.
            String mode = null;

            // Get the result action.
            String action = intent.getAction();

            // Retrieve the recipient's number and message.
            int transId = intent.getIntExtra(EXTRA_TRANS_ID, -1);
            mode = intent.getStringExtra(EXTRA_MODE);

            Transaction foundTrans = new Transaction();
            for (Transaction t : smsList) {

                if (t.id == transId) {
                    foundTrans = t;
                }

            }

            // This is the result for a send.
            if (SMS_SENT_ACTION.equals(action)) {
                int resultCode = getResultCode();
                foundTrans.smsStatus = translateSentResult(resultCode);
                transactions.setValue(smsList);
                if(resultCode!=Activity.RESULT_OK)
                    encounteredError=true;

            }

            if (mode.equals("multi"))
                sendNextMessage();

            //result = number + ", " + message + "\n" + result;
            //Toast.makeText(context, result, Toast.LENGTH_LONG).show();
        }

        int translateSentResult(int resultCode) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    return SMS_STATUS_SENT;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    return SMS_STATUS_GENERIC_FAILURE;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    return SMS_STATUS_UNKNOWN_ERROR;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    return SMS_STATUS_UNKNOWN_ERROR;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    return SMS_STATUS_NO_SERVICE;
                default:
                    return SMS_STATUS_UNKNOWN_ERROR;
            }
        }
    }

    class TimeOutTask extends TimerTask{

        @Override
        public void run() {
            doTimeoutAndFinish();
        }
    }


}
