package com.papco.sundar.papcortgs;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

public class GroupActivity extends AppCompatActivity {

    GroupActivityVM viewmodel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.container_layout);
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

}
