package com.papco.sundar.papcortgs.screens.sms;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.papco.sundar.papcortgs.R;
import com.papco.sundar.papcortgs.common.DividerDecoration;
import com.papco.sundar.papcortgs.database.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;

public class ActivitySMS extends AppCompatActivity {

    static final int PERMISSION_REQUEST_SMS = 2;

    RecyclerView recycler;
    SMSAdapter adapter;
    ActivitySMSVM viewModel;
    Button sendSms;
    TextView progress;
    SmsService smsService = null;
    smsServiceConnection connection;
    boolean mbound = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);
        viewModel = new ViewModelProvider(this).get(ActivitySMSVM.class);

        recycler = findViewById(R.id.sms_recycler);
        sendSms = findViewById(R.id.sms_button_send_all);
        progress = findViewById(R.id.sms_status_progress);
        connection = new smsServiceConnection();

        if (savedInstanceState == null) {

            Bundle b = getIntent().getExtras();

            if (b != null) {
                viewModel.currentGroupId = b.getInt("groupId");
                viewModel.currentGroupName = b.getString("groupName");
            }
        }

        viewModel.getSmsList().observe(this, new Observer<List<Transaction>>() {
            @Override
            public void onChanged(@Nullable List<Transaction> transactionForLists) {
                if (transactionForLists == null)
                    return;

                adapter.setData(transactionForLists);

            }
        });

        sendSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!SmsService.IS_SERVICE_RUNNING)
                    showSendConfirmDialog();

            }
        });

        adapter = new SMSAdapter(new ArrayList<Transaction>());
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.addItemDecoration(new DividerDecoration(this));
        recycler.setAdapter(adapter);

        getSupportActionBar().setTitle("Send SMS to Receivers");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void stopSmsService() {

        Intent intent = new Intent(ActivitySMS.this, SmsService.class);
        Bundle ex = new Bundle();
        ex.putInt("groupId", viewModel.currentGroupId);
        intent.putExtras(ex);
        intent.setAction(SmsService.ACTION_STOP_SERVICE);
        startService(intent);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return false;
    }


    private void showSendConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("SEND MESSAGES");
        builder.setMessage("Start sending messages to all beneficiaries? This operation cannot be cancelled in the middle");
        builder.setNegativeButton("CANCEL", null);
        builder.setPositiveButton("SEND", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                checkPermissionAndStartService();
            }
        });
        builder.create().show();
    }

    private void checkPermissionAndStartService() {

        if (weHaveSMSPermission())
            startSmsService();
        else
            requestSMSPermission();
    }

    public void startSmsService() {

        clearAllSmsStatus();

        Intent intent = new Intent(ActivitySMS.this, SmsService.class);
        Bundle ex = new Bundle();
        ex.putInt("groupId", viewModel.currentGroupId);
        ex.putString("groupName", viewModel.currentGroupName);
        intent.putExtras(ex);
        intent.setAction(SmsService.ACTION_START_SERVICE);
        startService(intent);
        bindToService();


    }

    @Override
    protected void onStop() {
        super.onStop();

        if (SmsService.IS_SERVICE_RUNNING && mbound) {
            unbindFromTheService();
        }

    }

    private void bindToService() {

        Intent bindIntent = new Intent(this, SmsService.class);
        if (connection == null)
            connection = new smsServiceConnection();
        bindService(bindIntent, connection, Context.BIND_AUTO_CREATE);


    }

    private void unbindFromTheService() {

        smsService.getWorkingStatus().removeObservers(this);
        smsService.getTransactions().removeObservers(this);
        unbindService(connection);
        progress.setText("");
        mbound = false;
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (SmsService.IS_SERVICE_RUNNING && !mbound) {

            bindToService();
        }
    }

    private boolean weHaveSMSPermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)
            return false;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
            return false;


        return true;
    }

    private void requestSMSPermission() {

        String[] permissions = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS,};
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_SMS);
    }

    private void clearAllSmsStatus() {

        for (Transaction trans : viewModel.getSmsList().getValue()) {
            trans.smsStatus = SmsService.SMS_STATUS_NOT_ATTEMPTED;
        }
        adapter.refresh();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_SMS)
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                startSmsService();


    }

    class SMSAdapter extends RecyclerView.Adapter<SMSAdapter.SMSViewHolder> {


        private List<Transaction> data;


        public SMSAdapter(List<Transaction> data) {
            this.data = data;
            setHasStableIds(true);
        }

        @Override
        public long getItemId(int position) {
            return data.get(position).id;
        }

        @NonNull
        @Override
        public SMSViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            return new SMSViewHolder(getLayoutInflater().inflate(R.layout.sms_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull SMSViewHolder holder, int position) {

            holder.name.setText(data.get(holder.getAdapterPosition()).receiver.name);
            holder.status.setText(getSmsStatus(data.get(holder.getAdapterPosition()).smsStatus));
            if (data.get(holder.getAdapterPosition()).receiver.mobileNumber.equals("")) {
                holder.mobilenumer.setVisibility(View.INVISIBLE);
                holder.icon.setVisibility(View.INVISIBLE);
            } else {
                holder.mobilenumer.setVisibility(View.VISIBLE);
                holder.icon.setVisibility(View.VISIBLE);
                holder.mobilenumer.setText(data.get(holder.getAdapterPosition()).receiver.mobileNumber);
            }

            if (data.get(holder.getAdapterPosition()).smsStatus != SmsService.SMS_STATUS_SENT)
                holder.status.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            else
                holder.status.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public void setData(List<Transaction> data) {
            this.data = data;
            notifyDataSetChanged();

        }

        public void refresh() {
            notifyDataSetChanged();
        }

        private String getSmsStatus(int smsStatus) {

            switch (smsStatus) {

                case SmsService.SMS_STATUS_NOT_ATTEMPTED:
                    return "";

                case SmsService.SMS_STATUS_SENT:
                    return "SMS SENT";

                case SmsService.SMS_STATUS_NO_SERVICE:
                    return "NO SERVICE";

                case SmsService.SMS_STATUS_RECEIVER_NUMBER_INVALID:
                    return "INVALID PHONE NO";

                case SmsService.SMS_STATUS_TIMEOUT:
                    return "TIMEOUT";

            }

            return "UNKNOWN ERROR";

        }

        public void updateSmsStatus() {

            notifyItemRangeChanged(0, data.size());

        }


        class SMSViewHolder extends RecyclerView.ViewHolder {

            TextView name, status, mobilenumer;
            ImageView icon;

            public SMSViewHolder(View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.sms_name);
                status = itemView.findViewById(R.id.sms_status);
                mobilenumer = itemView.findViewById(R.id.sms_mobilenumber);
                icon = itemView.findViewById(R.id.sms_icon);
            }
        }
    }

    class smsServiceConnection implements ServiceConnection {


        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            progress.setText("Sending sms. Please wait...");
            smsService = ((SmsService.SmsBinder) iBinder).getService();
            smsService.getTransactions().observe(ActivitySMS.this, new Observer<List<Transaction>>() {
                @Override
                public void onChanged(@Nullable List<Transaction> transactionForLists) {
                    adapter.updateSmsStatus();
                }
            });


            smsService.getWorkingStatus().observe(ActivitySMS.this, new Observer<Integer>() {
                @Override
                public void onChanged(@Nullable Integer integer) {

                    if (integer == smsService.WORK_STATUS_COMPLETED) {

                        viewModel.setSmsList(smsService.getTransactions().getValue()); //get the result and stop the service
                        unbindFromTheService();
                        stopSmsService();
                    }

                }
            });
            smsService.setListAndStartSending(viewModel.getSmsList().getValue());
            if (viewModel.getSmsList().getValue() == null)
                viewModel.setSmsList(smsService.getTransactions().getValue());

            mbound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            smsService = null;
            mbound = false;

        }
    }

}
