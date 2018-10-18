package com.papco.sundar.papcortgs;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.arch.lifecycle.MutableLiveData;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
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

import java.util.ArrayList;
import java.util.List;

public class SmsService extends Service {

    public static final String ACTION_START_SERVICE="com.papco.sundar.papcortgs.startsmsservice";
    public static final String ACTION_STOP_SERVICE="com.papco.sundar.papcortgs.stopsmsservice";
    public static boolean IS_SERVICE_RUNNING=false;
    private static final int NOTIFICATION_ID=1;
    public static final int NOTIFICATION_ID_SUCCESS=2;

    private static final String SMS_SENT_ACTION = "com.papco.sundar.papcortgs.SMS_SENT";
    private static final String EXTRA_MODE = "mode";
    private static final String EXTRA_TRANS_ID="transId";

    private static final int SMS_STATUS_SENT=1;
    private static final int SMS_STATUS_FAILED=2;
    private static final int SMS_STATUS_NOT_ATTEMPTED=-1;
    private static final int SMS_STATUS_RECEIVER_NUMBER_INVALID=4;
    private static final int SMS_STATUS_PERMISSION_ERROR=5;
    private static final int SMS_STATUS_NO_SERVICE=6;
    private static final int SMS_STATUS_GENERIC_FAILURE=7;
    private static final int SMS_STATUS_UNKNOWN_ERROR=8;

    public static final int WORK_STATUS_WORKING=1;
    public static final int WORK_STATUS_COMPLETED=2;

    private List<TransactionForList> smsList=null;
    private int currentGroupId=-1; //for creating the pending Intent to launch when tap notification

    SmsBinder binder;
    private SmsManager smsManager;
    NotificationCompat.Builder builder;
    private MutableLiveData<List<TransactionForList>> transactions;
    private MutableLiveData<Integer> workingStatus;
    private SmsResultReceiver receiver;
    private ServiceConnection currentConnection=null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent.getAction().equals(ACTION_START_SERVICE)){

            if(IS_SERVICE_RUNNING)
                return START_STICKY;

            if(intent.getExtras()!=null)
                currentGroupId=intent.getExtras().getInt("groupId",-1);

            showNotification();
            IS_SERVICE_RUNNING=true;
            if(transactions==null)
                transactions=new MutableLiveData<>();

            if(workingStatus==null){
                workingStatus=new MutableLiveData<>();
                workingStatus.setValue(WORK_STATUS_WORKING);
            }
            smsManager=SmsManager.getDefault();
            receiver=new SmsResultReceiver();
            registerReceiver(receiver,new IntentFilter(SMS_SENT_ACTION));

        }

        if(intent.getAction().equals(ACTION_STOP_SERVICE)){
            stopForeground(true);
        }

        return START_STICKY;
    }

    private void showNotification() {

        Intent intent=new Intent(this,ActivitySMS.class);
        Bundle b=new Bundle();
        b.putInt("groupId",currentGroupId);
        intent.putExtras(b);

        TaskStackBuilder taskBuilder=TaskStackBuilder.create(this);
        taskBuilder.addNextIntentWithParentStack(intent);


        Intent transactionIntent=taskBuilder.editIntentAt(1);
        Bundle options=new Bundle();
        options.putInt("groupId",currentGroupId);
        options.putString("groupName","first transaction");
        transactionIntent.putExtras(options);
        PendingIntent resultPendingIntent=taskBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);

        builder=new NotificationCompat.Builder(this,GroupActivity.NOTIFICATION_CHANNEL_ID);
        builder.setSmallIcon(R.drawable.app_icon);
        builder.setContentTitle("Sending SMS...");
        builder.setProgress(100,0,true);
        builder.setPriority(NotificationCompat.PRIORITY_LOW);
        builder.setContentIntent(resultPendingIntent);
        startForeground(1,builder.build());

    }

    public MutableLiveData<List<TransactionForList>> getTransactions(){

        return transactions;
    }

    public void setListAndStartSending(List<TransactionForList> list){

        transactions.setValue(list);
        this.smsList=list;
        workingStatus.setValue(WORK_STATUS_WORKING);
        sendNextMessage();
    }

    public MutableLiveData<Integer> getWorkingStatus(){
        return workingStatus;
    }

    private void updateProgressBar(int currentProgress) {

        if(currentProgress>=smsList.size()){
            builder.setContentTitle("Sending sms complete");
            builder.setProgress(smsList.size(),currentProgress,false);
        }else {
            builder.setProgress(smsList.size(), currentProgress, false);
            builder.setContentTitle("Sending sms " + Integer.toString(currentProgress + 1) + " of " + Integer.toString(smsList.size()));
        }

        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID,builder.build());

    }



    private boolean isValidMobileNumber(String number){

        Log.d("CHECKING NUMBER:",number);
        if(number.trim().length()!=10)
            return false;

        if(!TextUtils.isDigitsOnly(number))
            return false;

        return true;

    }

    private String composeMessage(TransactionForList trans){

        String result="We have done RTGS transfer of ";
        result=result+Transaction.amountAsString(trans.amount);
        result=result+" to your account " ;
        result=result+trans.receiver;
        result=result+". Kindly acknowledge the same. -PAPCO OFFSET";

        return result;

    }

    private void sendNextMessage() {

        int requestCode;

        int position;
        for(position=0;position<smsList.size();position++) {

            TransactionForList trans=smsList.get(position);

            //make sure that we have not sent message to this person already
            if(trans.smsStatus!=SMS_STATUS_NOT_ATTEMPTED) {
                continue;
            }

            updateProgressBar(position);

            //First check if the receiver mobile number is valid
            if(!isValidMobileNumber(trans.receiverMobile)){
                trans.smsStatus=SMS_STATUS_RECEIVER_NUMBER_INVALID;
                transactions.setValue(smsList);
                continue;
            }

            //we have a valid number. send the message
            requestCode=trans.id;
            Intent sentIntent = new Intent(SMS_SENT_ACTION);

            String number = trans.receiverMobile;
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

                return;

            } catch (Exception e) {
                trans.smsStatus=SMS_STATUS_PERMISSION_ERROR;
                transactions.setValue(smsList);
                e.printStackTrace();

            }
        }

        //Stop the service here since the sms has been sent already
        IS_SERVICE_RUNNING=false;
        workingStatus.setValue(WORK_STATUS_COMPLETED);
        if(currentConnection!=null) {
            unbindService(currentConnection);
            currentConnection=null;
        }
        stopForeground(true);
        stopSelf();
        showSuccessNotification();

    }

    private void showSuccessNotification(){

        //NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID);
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this,GroupActivity.NOTIFICATION_CHANNEL_ID);
        builder.setSmallIcon(R.drawable.app_icon);
        builder.setContentTitle("Sms sending complete");
        builder.setContentText("Successfully sent sms to all the beneficiaries");
        builder.setPriority(NotificationCompat.PRIORITY_LOW);
        builder.setAutoCancel(true);
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID_SUCCESS,builder.build());

    }

    @Override
    public void onDestroy() {
        Log.d("SUNDAR","On destroy");
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

    public void setConnection(ServiceConnection connection){
        currentConnection=connection;
    }

    public class SmsBinder extends Binder{

        public SmsService getService(){

            return SmsService.this;
        }
    }

    private class SmsResultReceiver extends BroadcastReceiver {


        @Override
        public void onReceive(Context context, Intent intent) {

            // A simple result Toast text.
            String mode = null;

            // Get the result action.
            String action = intent.getAction();

            // Retrieve the recipient's number and message.
            int transId = intent.getIntExtra(EXTRA_TRANS_ID, -1);
            mode = intent.getStringExtra(EXTRA_MODE);

            TransactionForList foundTrans = new TransactionForList();
            for (TransactionForList t : smsList) {

                if (t.id == transId) {
                    foundTrans = t;
                }

            }

            // This is the result for a send.
            if (SMS_SENT_ACTION.equals(action)) {
                int resultCode = getResultCode();
                foundTrans.smsStatus = translateSentResult(resultCode);
                transactions.setValue(smsList);

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

}
