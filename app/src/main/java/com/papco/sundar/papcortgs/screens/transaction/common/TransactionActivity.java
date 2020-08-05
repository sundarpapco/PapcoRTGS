package com.papco.sundar.papcortgs.screens.transaction.common;

import android.Manifest;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
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
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.papco.sundar.papcortgs.R;
import com.papco.sundar.papcortgs.common.AutoFileExporter;
import com.papco.sundar.papcortgs.common.ManualFileExporter;
import com.papco.sundar.papcortgs.common.TextInputDialogFragment;
import com.papco.sundar.papcortgs.common.WriteFileListener;
import com.papco.sundar.papcortgs.database.common.MasterDatabase;
import com.papco.sundar.papcortgs.database.transactionGroup.TransactionGroup;
import com.papco.sundar.papcortgs.screens.mail.ActivityEmail;
import com.papco.sundar.papcortgs.screens.password.PasswordCallback;
import com.papco.sundar.papcortgs.screens.password.PasswordDialog;
import com.papco.sundar.papcortgs.screens.receiver.ReceiverActivity;
import com.papco.sundar.papcortgs.screens.sender.SenderActivity;
import com.papco.sundar.papcortgs.screens.sms.ActivitySMS;
import com.papco.sundar.papcortgs.screens.transaction.createTransaction.CreateTransactionFragment;
import com.papco.sundar.papcortgs.screens.transaction.createTransaction.ReceiverSelectFragment;
import com.papco.sundar.papcortgs.screens.transaction.createTransaction.SenderSelectFragment;
import com.papco.sundar.papcortgs.screens.transaction.listTransaction.TransactionListFragment;

import org.jetbrains.annotations.NotNull;

public class TransactionActivity extends AppCompatActivity implements
        WriteFileListener, PasswordCallback, TextInputDialogFragment.TextInputListener {


    public static Intent getStartingIntent(Context context, int groupId, String groupName) {

        Intent intent = new Intent(context, TransactionActivity.class);
        intent.putExtras(getStartingArguments(groupId, groupName));
        return intent;

    }

    public static Bundle getStartingArguments(int groupId, String groupName) {

        Bundle bundle = new Bundle();
        bundle.putInt(KEY_GROUP_ID, groupId);
        bundle.putString(KEY_GROUP_NAME, groupName);
        return bundle;

    }

    public static final String KEY_GROUP_ID = "groupId";
    public static final String KEY_GROUP_NAME = "groupName";
    private static final int PERMISSION_REQUEST_STORAGE = 1;

    private TransactionActivityVM viewmodel;
    private TransactionGroup transactionGroup;
    private ViewGroup container;
    private boolean needtopop = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.container_layout);
        container = findViewById(R.id.container);
        viewmodel = ViewModelProviders.of(this).get(TransactionActivityVM.class);


        Bundle b = getIntent().getExtras();

        if (b != null) {
            transactionGroup = new TransactionGroup();
            transactionGroup.id = b.getInt(KEY_GROUP_ID);
            transactionGroup.name = b.getString(KEY_GROUP_NAME);
        }


        if (savedInstanceState == null) {
            loadTransactionListFragment();
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (needtopop) {
            needtopop = false;
            popBackStack();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("NEEDPOPUP", needtopop);
        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null)
            needtopop = savedInstanceState.getBoolean("NEEDPOPUP");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {

            if (getSupportFragmentManager().getBackStackEntryCount() == 0)
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
        TransactionListFragment fragment = TransactionListFragment.getInstance(transactionGroup.id, transactionGroup.name);
        transaction.add(R.id.container, fragment, "fragmentTransactionList");
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }

    public void showAddTransactionFragment(int loadTransactionId) {

        FragmentManager manager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);
        transaction.replace(R.id.container, CreateTransactionFragment.getInstance(transactionGroup.id, loadTransactionId));
        transaction.addToBackStack("addTransactionFragment");
        transaction.commit();

    }

    public void showSenderSelectFragment() {
        FragmentManager manager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.container, new SenderSelectFragment());
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.addToBackStack("senderSelectFragment");
        transaction.commit();
    }

    public void showReceiverSelectFragment() {
        FragmentManager manager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.container, ReceiverSelectFragment.getInstance(transactionGroup.id));
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.addToBackStack("receiverSelectFragment");
        transaction.commit();
    }

    public void showSenderActivity() {

        needtopop = true;
        Intent intent = new Intent(this, SenderActivity.class);
        startActivity(intent);
        //popBackStack();
    }

    public void showReceiverActivity() {

        needtopop = true;
        Intent intent = new Intent(this, ReceiverActivity.class);
        startActivity(intent);
        //popBackStack();
    }


    public void showSMSActivity() {

        Intent intent = new Intent(this, ActivitySMS.class);
        Bundle b = new Bundle();
        b.putInt("groupId", transactionGroup.id);
        b.putString("groupName", transactionGroup.name);
        intent.putExtras(b);
        startActivity(intent);
    }

    public void showEmailActivity() {

        Intent intent = new Intent(this, ActivityEmail.class);
        Bundle b = new Bundle();
        b.putInt("groupId", transactionGroup.id);
        b.putString("groupName", transactionGroup.name);
        intent.putExtras(b);
        startActivity(intent);
    }

    public void popBackStack() {

        hideKeyboard();
        FragmentManager manager = getSupportFragmentManager();
        manager.popBackStack();

    }

    private void hideKeyboard() {

        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(container.getWindowToken(), 0);

    }

    public void exportFile() {
        if (weHaveStoragePermission())
            showChequeNumberInputDialog();
        else
            requestStoragePermission();

    }

    public void autoExportFile(long time) {
        if (weHaveStoragePermission())
            createAutoXLFile(time);
        else
            requestStoragePermission();
    }

    public boolean weHaveStoragePermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            return false;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            return false;

        return true;
    }


    private void createXLFile(String chequeNumber) {

        new ManualFileExporter(MasterDatabase.getInstance(this), this, chequeNumber).execute(transactionGroup);
    }

    private void createAutoXLFile(long time) {
        new AutoFileExporter(MasterDatabase.getInstance(this), this, time).execute(transactionGroup);
    }

    private void requestStoragePermission() {

        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(TransactionActivity.this, permissions, PERMISSION_REQUEST_STORAGE);
    }


    @Override
    public void onWriteFileComplete(String filename) {

        Fragment frag = getSupportFragmentManager().findFragmentByTag("fragmentTransactionList");
        if (frag != null) {
            ((TransactionListFragment) frag).shareFile(filename);
        }

    }

    @Override
    public void onPasswordOk(int code) {
        if (code == PasswordDialog.CODE_RECEIVERS)
            showReceiverActivity();

        if (code == PasswordDialog.CODE_SENDERS)
            showSenderActivity();
    }

    private void showChequeNumberInputDialog(){

        TextInputDialogFragment.Builder builder=new TextInputDialogFragment.Builder();
        builder.setTitle("Enter cheque number");
        builder.setHint("Cheque number");
        builder.setResponseCode(1);
        TextInputDialogFragment textInputDialog=builder.build();
        textInputDialog.show(
                getSupportFragmentManager(),
                TextInputDialogFragment.TAG);

    }

    @Override
    public void onTextEntered(@NotNull String text, int responseCode) {
        createXLFile(text);
    }
}
