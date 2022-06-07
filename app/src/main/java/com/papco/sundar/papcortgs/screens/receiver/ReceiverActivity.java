package com.papco.sundar.papcortgs.screens.receiver;

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

public class ReceiverActivity extends AppCompatActivity {

    ViewGroup container;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_layout);
        container = findViewById(R.id.container);

        if (savedInstanceState == null)
            loadReceiverListFragment();

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

    private void loadReceiverListFragment() {

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.container, new ReceiverListFragment());
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }


    public void showAddReceiverFragment(int editingReceiverId) {

        CreateReceiverFragment fragment = CreateReceiverFragment.getInstance(editingReceiverId);

        hideKeyboard();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack("addReceiverFragment");
        transaction.commit();

    }

    public void popBackStack() {

        hideKeyboard();
        FragmentManager manager = getSupportFragmentManager();
        manager.popBackStackImmediate();
    }

    private void hideKeyboard() {

        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(container.getWindowToken(), 0);

    }
}
