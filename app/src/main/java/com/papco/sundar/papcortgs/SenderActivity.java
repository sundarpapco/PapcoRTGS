package com.papco.sundar.papcortgs;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public class SenderActivity extends AppCompatActivity {

    SenderActivityVM viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_layout);

        viewModel = ViewModelProviders.of(this).get(SenderActivityVM.class);

        if(savedInstanceState==null)
            loadSenderListFragment();

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

    private void loadSenderListFragment() {

        FragmentManager manager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.container, new SendersListFragment());
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }


    public void showAddSenderFragment() {

        FragmentManager manager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right,R.anim.exit_to_left,R.anim.enter_from_left,R.anim.exit_to_right);
        transaction.replace(R.id.container, new CreateSenderFragment());
        transaction.addToBackStack("addSenderFragment");
        transaction.commit();

    }

    public void popBackStack(){

        FragmentManager manager = getSupportFragmentManager();
        manager.popBackStackImmediate();
    }
}

