package com.papco.sundar.papcortgs.screens.sender;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.papco.sundar.papcortgs.R;
import com.papco.sundar.papcortgs.screens.receiver.CreateReceiverFragment;

public class SenderActivity extends AppCompatActivity {

    ViewGroup container;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_layout);
        container = findViewById(R.id.container);

        if (savedInstanceState == null)
            loadSenderListFragment();

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);


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

    private void loadSenderListFragment() {

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.container, new SendersListFragment());
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }


    public void showAddSenderFragment(int senderId) {

        CreateSenderFragment fragment = CreateSenderFragment.getInstance(senderId);

        hideKeyBoard();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack("addSenderFragment");
        transaction.commit();

    }

    public void popBackStack() {

        hideKeyBoard();
        FragmentManager manager = getSupportFragmentManager();
        manager.popBackStackImmediate();

    }

    private void hideKeyBoard() {

        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(container.getWindowToken(), 0);

    }
}

