package com.papco.sundar.papcortgs;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

public class GroupActivity extends AppCompatActivity {

    public static final String NOTIFICATION_CHANNEL_ID="smsChannelID";
    public static final String NOTIFICATION_CHANNEL_NAME="Papco RTGS";
    public static final String NOTIFICATION_CHANNEL_DESC="Notifications from papcoRTGS app";


    GroupActivityVM viewmodel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.container_layout);

        //Create the notification Channel for this app
        // this should be done for android 8.0 and up
        createNotificationChannel();

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
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, importance);
            channel.setDescription(NOTIFICATION_CHANNEL_DESC);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
