package com.papco.sundar.papcortgs;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.papco.sundar.papcortgs.mail.EmailCallBack;
import com.papco.sundar.papcortgs.mail.EmailService;

import java.util.List;

public class ActivityEmail extends AppCompatActivity {

    private static final int RC_SIGNIN=1;
    private static final int RC_PERMISSION=2;
    ViewGroup container;
    GoogleSignInClient googleClient;
    ActivityEmailVM viewmodel;
    EmailService emailService;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_layout);
        container=findViewById(R.id.container);

        viewmodel=ViewModelProviders.of(this).get(ActivityEmailVM.class);
        String scopeSendMail="https://www.googleapis.com/auth/gmail.send";
        GoogleSignInOptions.Builder builder=new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN);
        builder.requestScopes(new Scope(scopeSendMail));
        builder.requestEmail();
        builder.requestProfile();
        GoogleSignInOptions gso=builder.build();
        googleClient=GoogleSignIn.getClient(getApplicationContext(),gso);

        if(savedInstanceState==null) {

            Bundle b = getIntent().getExtras();

            if (b != null) {
                viewmodel.currentGroupId = b.getInt("groupId");
                viewmodel.currentGroupName=b.getString("groupName");
            }
        }

        if(GoogleSignIn.getLastSignedInAccount(getApplicationContext())==null)
            loadSignInFragment();
        else
            loadEmailFragment();

        setTitle("Email Receivers");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
            return true;
        }

        return false;
    }

    private void loadSignInFragment() {

        FragmentManager manager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.container, new FragmentGoogleSignIn(),"fragmentGoogleSignIn");
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();


    }

    private void loadEmailFragment(){

        FragmentManager manager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.container, new FragmentEmail(),"fragmentEmail");
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();


    }

    private void showEmailFragment(){

        FragmentManager manager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction transaction = manager.beginTransaction();
        //transaction.setCustomAnimations(R.anim.enter_from_bottom,R.anim.exit_to_top,R.anim.enter_from_top,R.anim.exit_to_bottom);
        transaction.replace(R.id.container, new FragmentEmail(),"fragmentEmail");
        transaction.commit();


    }

    private void showSignInFrament(){

        FragmentManager manager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction transaction = manager.beginTransaction();
        //transaction.setCustomAnimations(R.anim.enter_from_bottom,R.anim.exit_to_top,R.anim.enter_from_top,R.anim.exit_to_bottom);
        transaction.replace(R.id.container, new FragmentGoogleSignIn());
        transaction.commit();

    }

    public void signIn(){

        Intent signinIntent=googleClient.getSignInIntent();
        startActivityForResult(signinIntent,RC_SIGNIN);
    }

    public void signOut(){

        googleClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                showSignInFrament();
            }
        });

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==RC_SIGNIN) {
            handleSignInResult(GoogleSignIn.getSignedInAccountFromIntent(data));
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode==RC_PERMISSION)
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                Fragment fragment=getSupportFragmentManager().findFragmentByTag("fragmentEmail");
                if(fragment!=null){
                    ((FragmentEmail)fragment).showSendConfirmDialog();
                }
            }


    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {

        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
            showEmailFragment();
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.

        }
    }

    public boolean weHaveAccountsPermission(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.GET_ACCOUNTS)!=PackageManager.PERMISSION_GRANTED)
            return false;

        return true;
    }

    public void requestAccountsPermission() {

        String[] permissions={Manifest.permission.GET_ACCOUNTS};
        ActivityCompat.requestPermissions(this,permissions,RC_PERMISSION);
    }



}
