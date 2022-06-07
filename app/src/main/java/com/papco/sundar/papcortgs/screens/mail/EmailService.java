package com.papco.sundar.papcortgs.screens.mail;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;

import com.papco.sundar.papcortgs.screens.transaction.common.TransactionActivity;
import com.papco.sundar.papcortgs.screens.transactionGroup.GroupActivity;
import com.papco.sundar.papcortgs.R;
import com.papco.sundar.papcortgs.database.transaction.Transaction;

import java.lang.ref.WeakReference;
import java.util.List;

public class EmailService extends Service implements EmailCallBack {



    public static Intent getStartingIntent(Context context, int groupId, String groupName,int defaultSenderId){

        //groupId, groupName and defaultSenderId are needed to prepare the pending intent of notification
        Bundle bundle=new Bundle();
        bundle.putInt(KEY_GROUP_ID,groupId);
        bundle.putString(KEY_GROUP_NAME,groupName);
        bundle.putInt(KEY_DEFAULT_SENDER_ID,defaultSenderId);

        Intent intent=new Intent(context,EmailService.class);
        intent.setAction(ACTION_START_SERVICE);
        intent.putExtras(bundle);
        return intent;

    }

    public static Intent getStoppingIntent(Context context){

        Intent intent=new Intent(context,EmailService.class);
        intent.setAction(ACTION_STOP_SERVICE);
        return intent;
    }


    // region Constants --------------------------------------------------------

    private static final String ACTION_START_SERVICE = "com.papco.sundar.papcortgs.startemailservice";
    private static final String ACTION_STOP_SERVICE = "com.papco.sundar.papcortgs.stopemailservice";
    public static final String KEY_GROUP_ID = "groupId";
    public static final String KEY_GROUP_NAME = "groupName";
    public static final String KEY_DEFAULT_SENDER_ID="key:Default:Sender:Id";
    private static boolean IS_RUNNING = false;

    public static final int STATUS_DEFAULT = 3;
    public static final int STATUS_QUEUED = 4;
    public static final int STATUS_SENDING = 5;
    public static final int STATUS_SENT = 6;
    public static final int STATUS_FAILED = 7;

    private final int NOTIFICATION_ID = 2;

    public static final int WORK_STATUS_DEFAULT = 0;
    public static final int WORK_STATUS_WAITING_FOR_LIST = 8;
    public static final int WORK_STATUS_WORKING = 9;
    public static final int WORK_STATUS_COMPLETED = 10;

    // endregion Constants --------------------------------------------------------

    private List<Transaction> emailList = null;
    private int currentGroupId = -1; //for creating the pending Intent to launch when tap notification
    private String currentGroupName = null;
    private int currentDefaultSenderId=0;
    private int workingStatus = WORK_STATUS_DEFAULT;
    private NotificationCompat.Builder builder;
    private WeakReference<EmailCallBack> weakCallback = null;
    private EmailBinder binder;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.getAction().equals(ACTION_START_SERVICE)) {

            if (isRunning())
                return START_STICKY;
            else {
                initializeService(intent);
            }
        }

        if (intent.getAction().equals(ACTION_STOP_SERVICE)) {
            stopTheService();
        }

        return START_STICKY;

    }

    private void initializeService(Intent intent) {

        IS_RUNNING = true;


        if (intent.getExtras() != null) { //group id needed for preparing the pendingIntent of notification
            currentGroupId = intent.getExtras().getInt(KEY_GROUP_ID, -1);
            currentGroupName = intent.getExtras().getString(KEY_GROUP_NAME);
            currentDefaultSenderId=intent.getExtras().getInt(KEY_DEFAULT_SENDER_ID,0);
        }

        workingStatus = WORK_STATUS_WAITING_FOR_LIST;
        showNotification(); //This will make the service foreground
    }

    public void setCallBack(EmailCallBack callBack) {

        weakCallback = new WeakReference<>(callBack);

        if (weakCallback.get() != null && emailList != null) {
            weakCallback.get().onObserverAttached(emailList);
        }


        if (workingStatus == WORK_STATUS_COMPLETED) {
            if (weakCallback.get() != null && emailList != null)
                weakCallback.get().onComplete(emailList);
        }
    }

    public void removeCallBack() {

        weakCallback.clear();
    }

    private void stopTheService() {

        IS_RUNNING = false;
        stopForeground(true);
        stopSelf();

    }

    public void setListAndStartSending(List<Transaction> list) {

        if (emailList != null || list == null) //just ignore this call if a list has been already set
            return;

        emailList = list;
        startSendingMail();
    }

    private void startSendingMail() {

        EmailTask task = new EmailTask(getApplicationContext(), this);
        task.execute(emailList);
    }

    private void showNotification() {

        Intent intent = new Intent(this, ActivityEmail.class);
        Bundle b = new Bundle();
        b.putInt("groupId", currentGroupId);
        intent.putExtras(b);

        TaskStackBuilder taskBuilder = TaskStackBuilder.create(this);
        taskBuilder.addNextIntentWithParentStack(intent);


        Intent transactionIntent = taskBuilder.editIntentAt(1);
        transactionIntent.putExtras(TransactionActivity.getStartingArguments(currentGroupId, currentGroupName,currentDefaultSenderId));
        PendingIntent resultPendingIntent = taskBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder = new NotificationCompat.Builder(this, GroupActivity.NOTIFICATION_CHANNEL_ID);
        builder.setSmallIcon(R.drawable.app_icon);
        builder.setContentTitle("Sending Email...");
        builder.setProgress(100, 0, true);
        builder.setPriority(NotificationCompat.PRIORITY_LOW);
        builder.setContentIntent(resultPendingIntent);
        startForeground(NOTIFICATION_ID, builder.build());

    }

    private void updateNotification(int updatedIndex) {

        //progress will be updated only when a mail is sent or failed. Sending updation is to be ignored
        if (emailList.get(updatedIndex).emailStatus == STATUS_SENDING)
            return;

        builder.setProgress(emailList.size(), updatedIndex, false);
        builder.setContentTitle("Sending mail " + Integer.toString(updatedIndex + 1) + " of " + Integer.toString(emailList.size()));

        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, builder.build());

    }

    private void updateNotificationCompleted() {

        boolean isErrorSpotted = false;

        //search the list to find if there was any error found while sending mail
        for (Transaction transaction : emailList) {

            if (transaction.emailStatus == STATUS_FAILED) {
                isErrorSpotted = true;
                break;
            }
        }

        builder.setContentTitle("Sending mail complete");
        builder.setProgress(0, 0, false); //remove the progressbar
        if (isErrorSpotted)
            builder.setContentText("Sending mails completed with some failures! Tap for info");
        else
            builder.setContentText("Successfully sent mails to beneficiaries");

        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, builder.build());

    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (binder == null)
            binder = new EmailBinder();

        return binder;
    }

    public static boolean isRunning() {
        return IS_RUNNING;
    }

    //region ************* Callbacks for the AsyncTask**************

    @Override
    public void onObserverAttached(List<Transaction> currentList) {

    }

    @Override
    public void onStartSending() {

        workingStatus = WORK_STATUS_WORKING;
        if (weakCallback.get() != null) {
            weakCallback.get().onStartSending();
        }

    }

    @Override
    public void onUpdate(int updatedPosition) {

        //updateProgressBar(emailList);
        updateNotification(updatedPosition);
        if (weakCallback.get() != null && emailList != null) {
            weakCallback.get().onUpdate(updatedPosition);
        }

    }

    @Override
    public void onComplete(List<Transaction> list) {

        workingStatus = WORK_STATUS_COMPLETED;
        updateNotificationCompleted();
        if (weakCallback.get() != null && list != null) {
            weakCallback.get().onComplete(list);
        }

    }

    //endregion************** End of callbacks

    public class EmailBinder extends Binder {

        public EmailService getService() {

            return EmailService.this;
        }
    }


}
