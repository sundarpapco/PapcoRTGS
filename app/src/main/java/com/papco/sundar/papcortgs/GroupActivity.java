package com.papco.sundar.papcortgs;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.papco.sundar.papcortgs.password.PasswordCallback;
import com.papco.sundar.papcortgs.password.PasswordDialog;

public class GroupActivity extends AppCompatActivity implements PasswordCallback {

    public static final String NOTIFICATION_CHANNEL_ID="smsChannelID";
    public static final String NOTIFICATION_CHANNEL_NAME="Papco RTGS";
    public static final String NOTIFICATION_CHANNEL_DESC="Notifications from papcoRTGS app";


    GroupActivityVM viewmodel;
    SharedPreferences pref;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.container_layout);

        //Create the notification Channel for this app
        // this should be done for android 8.0 and up
        createNotificationChannel();
        if(isFirstRun())
            writeDefaultMessageTemplate();

        viewmodel=ViewModelProviders.of(this).get(GroupActivityVM.class);

        if(savedInstanceState==null)
            loadGroupListFragment();

    }


    private void loadGroupListFragment() {

        FragmentManager manager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.container, new GroupListFragment());
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }

    public void showCreateEditDialog(){
        new GroupCreateEditDialog().show(getSupportFragmentManager().beginTransaction(),"dialog");
    }

    public void showTransactionsActivity(TransactionGroup group){

        Intent intent=new Intent(this,TransactionActivity.class);
        Bundle b=new Bundle();
        b.putInt("groupId",group.id);
        b.putString("groupName",group.name);
        intent.putExtras(b);
        startActivity(intent);
    }

    public void showDropBoxActivity(){

        Intent intent=new Intent(this,ActivityDropBox.class);
        startActivity(intent);

    }

    public void popBackStack() {

        FragmentManager manager = getSupportFragmentManager();
        manager.popBackStack();

    }

    private void createNotificationChannel(){

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, importance);
            channel.setDescription(NOTIFICATION_CHANNEL_DESC);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private boolean isFirstRun(){

        pref=getSharedPreferences("mysettings",MODE_PRIVATE);
        if(pref.contains("first_run")){
            return false;
        }else{
            pref.edit().putBoolean("first_run",false).commit();
            return true;
        }

    }

    private void writeDefaultMessageTemplate(){

        if(pref!=null) {
            pref.edit().putString("message_template", TextFunctions.getDefaultMessageFormat()).commit();
            Log.d("SUNDAR","Writing default message");

        }
    }

    private void showReceiversActivity() {

        Intent intent=new Intent(this,ReceiverActivity.class);
        startActivity(intent);
    }

    private void showSendersActivity() {

        Intent intent=new Intent(this,SenderActivity.class);
        startActivity(intent);

    }

    @Override
    public void onPasswordOk(int code) {
        if(code==PasswordDialog.CODE_RECEIVERS)
            showReceiversActivity();

        if(code==PasswordDialog.CODE_SENDERS)
            showSendersActivity();
    }
}
