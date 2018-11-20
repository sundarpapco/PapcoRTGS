package com.papco.sundar.papcortgs;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

public class ReceiverActivity extends AppCompatActivity {

    ReceiverActivityVM viewModel;
    ViewGroup container;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_layout);
        container=findViewById(R.id.container);

        viewModel = ViewModelProviders.of(this).get(ReceiverActivityVM.class);

        if(savedInstanceState==null)
            loadReceiverListFragment();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


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

    private void loadReceiverListFragment() {

        FragmentManager manager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.container, new ReceiverListFragment());
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }


    public void showAddReceiverFragment() {

        hideKeyboard();
        FragmentManager manager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right,R.anim.exit_to_left,R.anim.enter_from_left,R.anim.exit_to_right);
        transaction.replace(R.id.container, new CreateReceiverFragment());
        transaction.addToBackStack("addReceiverFragment");
        transaction.commit();

    }

    public void popBackStack(){

        hideKeyboard();
        FragmentManager manager = getSupportFragmentManager();
        manager.popBackStackImmediate();
    }

    private void hideKeyboard(){

        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(container.getWindowToken(), 0);

    }
}
