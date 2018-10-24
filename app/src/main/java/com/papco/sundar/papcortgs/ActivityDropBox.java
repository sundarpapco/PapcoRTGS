package com.papco.sundar.papcortgs;

import android.Manifest;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.android.Auth;
import com.dropbox.core.v2.DbxClientV2;

public class ActivityDropBox extends AppCompatActivity implements BackupTask.BackupCallBack {

    private static int PERMISSION_REQUEST_INTERNET=11;

    Button start,backup,restore;
    TextView progress;
    String access_token=null;
    SharedPreferences pref;
    boolean restoreFlag=false;
    boolean flagBackingup,flagRestoring;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dropbox);
        start=findViewById(R.id.btn_link_dropbox);
        backup=findViewById(R.id.dropbox_btn_backup);
        restore=findViewById(R.id.dropbox_btn_restore);
        progress=findViewById(R.id.dropbox_progess);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                progress.setText("");

                if(access_token==null)
                    Auth.startOAuth2Authentication(ActivityDropBox.this,"i2owryva8qyt10c");
                else{
                    access_token=null;
                    pref.edit().remove("access_token").commit();
                    start.setText("LINK TO DROPBOX");
                    hideDropboxOperations(true);
                }
            }
        });
        backup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //if already backup in progress, do nothing
                if(flagBackingup)
                    return;

                if(weHaveInternetConnection())
                    checkPermissionAndBackup();
                else
                    progress.setText("Error: No Internet connection or necessary permissions");
                }
        });
        restore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //if already restoring in progress, do nothing
                if(flagRestoring)
                    return;

                showAlertDialogForRestore();


            }
        });

        pref=getPreferences(MODE_PRIVATE);
        access_token=pref.getString("access_token","invalid");
        if(access_token.equals("invalid"))
            access_token=null;

    }

    @Override
    protected void onResume() {
        super.onResume();

        String mtoken=Auth.getOAuth2Token();

        if(access_token==null && mtoken!=null){

            pref.edit().putString("access_token",mtoken).commit();
            access_token=mtoken;

        }

        if(access_token==null) {
            start.setText("LINK TO DROPBOX");
            hideDropboxOperations(true);
        }else {
            start.setText("UNLINK FROM DROPBOX");
            hideDropboxOperations(false);
        }
    }

    private boolean weHaveInternetConnection(){

        ConnectivityManager cmanager=(ConnectivityManager)getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork=cmanager.getActiveNetworkInfo();
        if(activeNetwork!=null && activeNetwork.isConnectedOrConnecting())
            return true;
        else
            return false;
    }

    private boolean weHaveRequiredPermission() {

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.INTERNET)!=PackageManager.PERMISSION_GRANTED)
            return false;

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED)
            return false;

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED)
            return false;

        return true;

    }

    private void checkPermissionAndBackup(){

        if(weHaveRequiredPermission())
            startBackup();
        else
            requestRequiredPermission();

    }

    private void checkPermissionAndRestore(){

        if(weHaveRequiredPermission())
            startRestore();
        else{
            restoreFlag=true;
            requestRequiredPermission();
        }

    }

    private void showAlertDialogForRestore(){

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Caution!");
        builder.setMessage("Restoring from backup will erase and overwrite all your current data with the dropbox data.\n" +
                "Sure want to restore and overwrite?");
        builder.setPositiveButton("RESORE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if(weHaveInternetConnection())
                    checkPermissionAndRestore();
                else
                    progress.setText("Error: No Internet connection or necessary permissions");
            }
        });
        builder.setNegativeButton("CANCEL",null);
        builder.create().show();

    }

    private void startRestore(){

        flagRestoring=true;
        progress.setText("Restoring. Please wait...");

        DbxClientV2 client=null;
        if(access_token!=null){

            DbxRequestConfig config = DbxRequestConfig.newBuilder("papcortgs").build();
            client = new DbxClientV2(config, access_token);

        }

        if(client!=null){

            new BackupTask(getApplication(),client,BackupOperation.RESTORE,ActivityDropBox.this).execute();

        }

    }

    private void requestRequiredPermission() {

        String[] permissions={Manifest.permission.INTERNET,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(this,permissions,PERMISSION_REQUEST_INTERNET);
    }

    private void startBackup(){

        flagBackingup=true;
        progress.setText("Backing up. Please wait...");

        DbxClientV2 client=null;
        if(access_token!=null){

            DbxRequestConfig config = DbxRequestConfig.newBuilder("papcortgs").build();
            client = new DbxClientV2(config, access_token);

        }

        if(client!=null){

            new BackupTask(getApplication(),client,BackupOperation.BACKUP,ActivityDropBox.this).execute();

        }

    }

    private void hideDropboxOperations(boolean hide){

        if(hide){
            backup.setVisibility(View.GONE);
            restore.setVisibility(View.GONE);
        }else{
            backup.setVisibility(View.VISIBLE);
            restore.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode==PERMISSION_REQUEST_INTERNET){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                if(restoreFlag){
                    restoreFlag=false;
                    startRestore();
                }else
                    startBackup();
            }
        }
    }

    @Override
    public void onBackupComplete(int result ) {

        flagBackingup=false;
        if(result==BackupTask.RESULT_COMPLETED)
            progress.setText("Backup successful!");
        else
            progress.setText("Backup failed");

    }

    @Override
    public void onProgessChanged(String prog) {

        progress.setText(prog);

    }

    @Override
    public void onRestoreComplete(int result) {

        flagRestoring=false;
        String resultText="Restore failed";
        switch (result){
            case BackupTask.RESULT_COMPLETED:
                resultText="Restore successful";
                break;

            case BackupTask.RESULT_NO_BACKUP_FOUND:
                resultText="No valid backup found in Dropbox";
                break;
        }

        progress.setText(resultText);

    }


}
