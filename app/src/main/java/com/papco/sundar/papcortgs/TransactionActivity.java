package com.papco.sundar.papcortgs;

import android.Manifest;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.papco.sundar.papcortgs.password.PasswordCallback;
import com.papco.sundar.papcortgs.password.PasswordDialog;

public class TransactionActivity extends AppCompatActivity implements FileExporter.WriteFileListener,PasswordCallback {

    static final int PERMISSION_REQUEST_STORAGE=1;

    TransactionActivityVM viewmodel;
    ViewGroup container;
    boolean needtopop=false;
    String TAG="SUNDAR";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //testing
        if(savedInstanceState==null)
            Log.d(TAG, "onCreate of TransactionActivity with null state");
        else
            Log.d(TAG, "onCreate of TransactionActivity without null state");

        setContentView(R.layout.container_layout);
        container=findViewById(R.id.container);
        viewmodel=ViewModelProviders.of(this).get(TransactionActivityVM.class);
        Log.d(TAG, TransactionActivityVM.class.getCanonicalName());

        if(viewmodel.currentGroup==null){

            Log.d(TAG, "currentGroup of viewmodel is null");
            Bundle b=getIntent().getExtras();

            if(b!=null) {
                TransactionGroup group=new TransactionGroup();
                group.id = b.getInt("groupId");
                group.name=b.getString("groupName");
                viewmodel.currentGroup=group;
            }
        }


        if(savedInstanceState==null) {
            loadTransactionListFragment();
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(needtopop){
            needtopop=false;
            popBackStack();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("NEEDPOPUP",needtopop);
        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState!=null)
            needtopop=savedInstanceState.getBoolean("NEEDPOPUP");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==android.R.id.home){

            if(getSupportFragmentManager().getBackStackEntryCount()==0)
                finish();
            else
                popBackStack();

            return true;

        }

        return false;
    }

    private void loadTransactionListFragment() {

        FragmentManager manager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.container, new TransactionListFragment(),"fragmentTransactionList");
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }


    public void showAddTransactionFragment() {

        FragmentManager manager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction transaction = manager.beginTransaction();
        //transaction.setCustomAnimations(R.anim.enter_from_bottom,R.anim.exit_to_top,R.anim.enter_from_top,R.anim.exit_to_bottom);
        transaction.setCustomAnimations(R.anim.fade_in,R.anim.fade_out,R.anim.fade_in,R.anim.fade_out);
        transaction.replace(R.id.container, new CreateTransactionFragment());
        transaction.addToBackStack("addTransactionFragment");
        transaction.commit();

    }

    public void showSenderSelectFragment(){
        FragmentManager manager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.container, new SenderSelectFragment());
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.addToBackStack("senderSelectFragment");
        transaction.commit();
    }

    public void showReceiverSelectFragment(){
        FragmentManager manager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.container, new ReceiverSelectFragment());
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.addToBackStack("receiverSelectFragment");
        transaction.commit();
    }

    public void showSenderActivity(){

        needtopop=true;
        Intent intent=new Intent(this,SenderActivity.class);
        startActivity(intent);
        //popBackStack();
    }

    public void showReceiverActivity(){

        needtopop=true;
        Intent intent=new Intent(this,ReceiverActivity.class);
        startActivity(intent);
        //popBackStack();
    }



    public void showSMSActivity(){

        Intent intent=new Intent(this,ActivitySMS.class);
        Bundle b=new Bundle();
        b.putInt("groupId",viewmodel.currentGroup.id);
        b.putString("groupName",viewmodel.currentGroup.name);
        intent.putExtras(b);
        startActivity(intent);
    }

    public void showEmailActivity(){

        Intent intent=new Intent(this,ActivityEmail.class);
        Bundle b=new Bundle();
        b.putInt("groupId",viewmodel.currentGroup.id);
        b.putString("groupName",viewmodel.currentGroup.name);
        intent.putExtras(b);
        startActivity(intent);
    }

    public void popBackStack() {

        hideKeyboard();
        FragmentManager manager = getSupportFragmentManager();
        manager.popBackStack();

    }

    private void hideKeyboard(){

        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(container.getWindowToken(), 0);

    }

    public void exportFile(){
        if(weHaveStoragePermission())
            CreateXLFile();
        else
            requestStoragePermission();

    }

    public boolean weHaveStoragePermission() {

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED)
            return false;

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED)
            return false;

        return true;
    }


    private void CreateXLFile() {

        new FileExporter(getApplication(),this).execute(viewmodel.currentGroup);
    }

    private void requestStoragePermission() {

        String[] permissions={Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(TransactionActivity.this,permissions,PERMISSION_REQUEST_STORAGE);
    }



    @Override
    public void onWriteFileComplete(String filename) {

        Fragment frag=getSupportFragmentManager().findFragmentByTag("fragmentTransactionList");
        if(frag!=null){
            ((TransactionListFragment)frag).shareFile(filename);
        }

        //shareFile(filename);
    }

    @Override
    public void onPasswordOk(int code) {
        if(code==PasswordDialog.CODE_RECEIVERS)
            showReceiverActivity();

        if(code==PasswordDialog.CODE_SENDERS)
            showSenderActivity();
    }
}
