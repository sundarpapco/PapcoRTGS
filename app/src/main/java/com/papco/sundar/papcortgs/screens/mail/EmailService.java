package com.papco.sundar.papcortgs.screens.mail;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.TaskStackBuilder;

import com.papco.sundar.papcortgs.screens.transactionGroup.GroupActivity;
import com.papco.sundar.papcortgs.R;
import com.papco.sundar.papcortgs.database.transaction.Transaction;

import java.lang.ref.WeakReference;
import java.util.List;

public class EmailService extends Service implements EmailCallBack {

    private static boolean IS_RUNNING=false;


    // region Constants --------------------------------------------------------

    public static final String ACTION_START_SERVICE="com.papco.sundar.papcortgs.startemailservice";
    public static final String ACTION_STOP_SERVICE="com.papco.sundar.papcortgs.stopemailservice";

    public static final int STATUS_DEFAULT=3;
    public static final int STATUS_QUEUED=4;
    public static final int STATUS_SENDING=5;
    public static final int STATUS_SENT=6;
    public static final int STATUS_FAILED=7;

    private final int NOTIFICATION_ID=2;

    public static final int WORK_STATUS_DEFAULT=0;
    public static final int WORK_STATUS_WAITING_FOR_LIST=8;
    public static final int WORK_STATUS_WORKING=9;
    public static final int WORK_STATUS_COMPLETED=10;

    // endregion Constants --------------------------------------------------------

    private List<Transaction> emailList=null;
    private int currentGroupId=-1; //for creating the pending Intent to launch when tap notification
    private String currentGroupName=null;
    private int workingStatus=WORK_STATUS_DEFAULT;
    private NotificationCompat.Builder builder;
    private WeakReference<EmailCallBack> weakCallback=null;
    private EmailBinder binder;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent.getAction().equals(ACTION_START_SERVICE)){

            if(isIsRunning())
                return START_STICKY;
            else {
                initializeService(intent);
            }
        }

        if(intent.getAction().equals(ACTION_STOP_SERVICE)){
            stopTheService();
        }

        return START_STICKY;

    }

    private void initializeService(Intent intent) {

        IS_RUNNING=true;


        if(intent.getExtras()!=null) { //group id needed for preparing the pendingIntent of notification
            currentGroupId = intent.getExtras().getInt("groupId", -1);
            currentGroupName=intent.getExtras().getString("groupName");
        }

        workingStatus=WORK_STATUS_WAITING_FOR_LIST;
        showNotification(); //This will make the service foreground
    }

    public void setCallBack(EmailCallBack callBack){

        weakCallback=new WeakReference<>(callBack);

        if(workingStatus==WORK_STATUS_WORKING){
            if(weakCallback.get()!=null && emailList!=null){
               weakCallback.get().onObserverAttached(emailList);
            }
        }

        if(workingStatus==WORK_STATUS_COMPLETED){
            if(weakCallback.get()!=null && emailList!=null)
                weakCallback.get().onComplete(emailList);
        }
    }

    public void removeCallBack(){

        weakCallback.clear();
    }

    private void showNotification() {

            Intent intent=new Intent(this,ActivityEmail.class);
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
            builder=new NotificationCompat.Builder(this,GroupActivity.NOTIFICATION_CHANNEL_ID);
            builder.setSmallIcon(R.drawable.app_icon);
            builder.setContentTitle("Sending Email...");
            builder.setProgress(100,0,true);
            builder.setPriority(NotificationCompat.PRIORITY_LOW);
            builder.setContentIntent(resultPendingIntent);
            startForeground(NOTIFICATION_ID,builder.build());

    }

    private void stopTheService(){

        IS_RUNNING=false;
        stopForeground(true);
        stopSelf();

    }

    public void setListAndStartSending(List<Transaction> list){

        if(emailList!=null || list==null) //just ignore this call if a list has been already set
            return;

        emailList=list;
        startSendingMail();
    }

    private void startSendingMail() {

        EmailTask task=new EmailTask(getApplicationContext(),this);
        task.execute(emailList);
    }

    private void updateProgressBar(List<Transaction> list) {

        int currentStatus=0;
        boolean isThereAnyError=false;
        int status=STATUS_DEFAULT;
        for(Transaction transaction:list){

            status=transaction.emailStatus;

            if(status==STATUS_QUEUED)
                break; //We can break the loop here since all remaining will be queued only

            if(status==STATUS_SENT || status==STATUS_FAILED) {
                currentStatus = currentStatus + 1;
            }

            if(status==STATUS_FAILED)
                isThereAnyError=true;


        }

        if(currentStatus==list.size()){
            //All sending completed. update the progressbar to complete
            builder.setContentTitle("Sending mail complete");
            builder.setProgress(0,0,false); //remove the progressbar
            if(isThereAnyError)
                builder.setContentText("Sending mails completed with some errors! Tap for info");
            else
                builder.setContentText("Successfully sent mails to beneficiaries");

        }else{

            builder.setProgress(list.size(), currentStatus, false);
            builder.setContentTitle("Sending mail " + Integer.toString(currentStatus + 1) + " of " + Integer.toString(list.size()));
        }

        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID,builder.build());

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if(binder==null)
            binder=new EmailBinder();

        return binder;
    }

    public static boolean isIsRunning(){
        return IS_RUNNING;
    }

    //region ************* Callbacks for the AsyncTask**************

    @Override
    public void onObserverAttached(List<Transaction> currentList) {

    }

    @Override
    public void onStartSending() {

        workingStatus=WORK_STATUS_WORKING;
        if(weakCallback.get()!=null){
            weakCallback.get().onStartSending();
        }

    }

    @Override
    public void onUpdate(int updatedPosition) {

        updateProgressBar(emailList);
        if(weakCallback.get()!=null && emailList!=null){
            weakCallback.get().onUpdate(updatedPosition);
        }

    }

    @Override
    public void onComplete(List<Transaction> list) {

        workingStatus=WORK_STATUS_COMPLETED;
        if(weakCallback.get()!=null && list!=null){
            weakCallback.get().onComplete(list);
        }

    }

    //endregion************** End of callbacks

    public class EmailBinder extends Binder {

        public EmailService getService(){

            return EmailService.this;
        }
    }


}
