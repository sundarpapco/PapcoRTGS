package com.papco.sundar.papcortgs.screens.mail;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.papco.sundar.papcortgs.R;

public class ActivityEmail extends AppCompatActivity {

    ActivityEmailVM viewModel;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_layout);

        viewModel = new ViewModelProvider(this).get(ActivityEmailVM.class);

        if (savedInstanceState == null) {
            readTransactionGroupFromBundle(getIntent().getExtras());
        }

        loadSignInFragment();
        setTitle("Email Receivers");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return false;
    }

    private void loadSignInFragment() {

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.container, new FragmentGoogleSignIn(), FragmentGoogleSignIn.TAG);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }


    private void readTransactionGroupFromBundle(Bundle b) {

        if (b != null) {
            viewModel.currentGroupId = b.getInt(EmailService.KEY_GROUP_ID);
            viewModel.currentGroupName = b.getString(EmailService.KEY_GROUP_NAME);
            viewModel.currentDefaultSenderId = b.getInt(EmailService.KEY_DEFAULT_SENDER_ID);
        }

    }


}
