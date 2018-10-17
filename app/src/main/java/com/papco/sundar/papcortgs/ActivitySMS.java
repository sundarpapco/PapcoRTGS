package com.papco.sundar.papcortgs;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ActivitySMS extends AppCompatActivity {

    static final int PERMISSION_REQUEST_SMS=2;
    private static final String SMS_SENT_ACTION = "com.papco.sundar.papcortgs.SMS_SENT";
    private static final String EXTRA_MODE = "mode";
    private static final String EXTRA_TRANS_ID="transId";

    private static final int SMS_STATUS_SENT=1;
    private static final int SMS_STATUS_FAILED=2;
    private static final int SMS_STATUS_NOT_ATTEMPTED=-1;
    private static final int SMS_STATUS_RECEIVER_NUMBER_INVALID=4;
    private static final int SMS_STATUS_PERMISSION_ERROR=5;
    private static final int SMS_STATUS_NO_SERVICE=6;
    private static final int SMS_STATUS_GENERIC_FAILURE=7;
    private static final int SMS_STATUS_UNKNOWN_ERROR=8;


    private SmsManager smsManager;
    private IntentFilter intentFilter;
    private BroadcastReceiver smsReceiver;


    RecyclerView recycler;
    SMSAdapter adapter;
    ActivitySMSVM viewmodel;
    Button sendSms;
    TextView progress;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);
        viewmodel=ViewModelProviders.of(this).get(ActivitySMSVM.class);

        recycler=findViewById(R.id.sms_recycler);
        sendSms=findViewById(R.id.sms_button_send_all);
        progress=findViewById(R.id.sms_status_progress);

        if(savedInstanceState==null) {

            Bundle b = getIntent().getExtras();

            if (b != null) {
                viewmodel.currentGroupId = b.getInt("groupId");
            }
        }

        viewmodel.getTransactions().observe(this, new Observer<List<TransactionForList>>() {
            @Override
            public void onChanged(@Nullable List<TransactionForList> transactionForLists) {
                if(transactionForLists==null)
                    return;

                adapter.setData(transactionForLists);


            }
        });

        sendSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSms.setEnabled(false);
                sendSMS();
            }
        });

        adapter=new SMSAdapter(new ArrayList<TransactionForList>());
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.addItemDecoration(new DividerDecoration(this,(GradientDrawable)getResources().getDrawable(R.drawable.divider)));
        recycler.setAdapter(adapter);

        getSupportActionBar().setTitle("Send SMS to Receivers");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        smsManager=SmsManager.getDefault();
        smsReceiver=new SmsResultReceiver();
        intentFilter = new IntentFilter(SMS_SENT_ACTION);

        if(savedInstanceState!=null && viewmodel.sendingSms){

            sendSMS();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(smsReceiver);
    }

    @Override
    protected void onResume(){
        super.onResume();
        registerReceiver(smsReceiver,intentFilter);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
            return true;
        }

        return false;
    }

    public void sendSMS(){

        clearAllSmsStatus();

        if(weHaveSMSPermission()) {
            if(viewmodel.currentlySendingTransaction==null)
                sendNextMessage();
            else {
                sendSingleMessage(viewmodel.currentlySendingTransaction);
            }
        }else
            requestSMSPermission();

    }

    private boolean weHaveSMSPermission() {

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.SEND_SMS)!=PackageManager.PERMISSION_GRANTED)
            return false;

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED)
            return false;


        return true;
    }

    private void requestSMSPermission() {

        String[] permissions={Manifest.permission.READ_PHONE_STATE,Manifest.permission.SEND_SMS,Manifest.permission.RECEIVE_SMS,Manifest.permission.READ_SMS,};
        ActivityCompat.requestPermissions(this,permissions,PERMISSION_REQUEST_SMS);
    }

    private void dispatchSMSWithPermission(){

        String phoneNo="9047013696";
        String sms="Hello android world";

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, sms, null, null);
            Toast.makeText(getApplicationContext(), "SMS Sent!",
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(),
                    "SMS faild, please try again later!",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }


    }


    private void sendSingleMessage(TransactionForList trans){

        int requestCode;


        //First check if the receiver mobile number is valid
        if(!isValidMobileNumber(trans.receiverMobile)){
            trans.smsStatus=SMS_STATUS_RECEIVER_NUMBER_INVALID;
            adapter.refresh();
            return;
        }

        //we have a valid number. send the message
        requestCode=trans.id;
        Intent sentIntent = new Intent(SMS_SENT_ACTION);

        String number = trans.receiverMobile;
        String message = composeMessage(trans);
        sentIntent.putExtra(EXTRA_TRANS_ID,trans.id);
        sentIntent.putExtra(EXTRA_MODE,"single");


        // Construct the PendingIntents for the results.
        // FLAG_ONE_SHOT cancels the PendingIntent after use so we
        // can safely reuse the request codes in subsequent runs.
        PendingIntent sentPI = PendingIntent.getBroadcast(this,
                requestCode,
                sentIntent,
                PendingIntent.FLAG_ONE_SHOT);


        try {
            // Send our message.

            if(message.length()>160) {

                ArrayList<String> parts = smsManager.divideMessage(message);
                ArrayList<PendingIntent> sentIntents = new ArrayList<>();
                sentIntents.add(sentPI);
                smsManager.sendMultipartTextMessage(number, null, parts, sentIntents, null);
            }else{

                smsManager.sendTextMessage(number,null,message,sentPI,null);

            }

            viewmodel.currentlySendingTransaction=null;

        } catch (Exception e) {
            trans.smsStatus=SMS_STATUS_PERMISSION_ERROR;
            adapter.refresh();
            e.printStackTrace();
        }
    }



    private void sendNextMessage() {

        progress.setText("Sending sms. Please wait...");

        int requestCode;
        viewmodel.sendingSms=true;

        for(TransactionForList trans:viewmodel.getTransactions().getValue()) {

            //make sure that we have not sent message to this person already
            if(trans.smsStatus!=SMS_STATUS_NOT_ATTEMPTED)
                continue;

            //First check if the receiver mobile number is valid
            if(!isValidMobileNumber(trans.receiverMobile)){
                trans.smsStatus=SMS_STATUS_RECEIVER_NUMBER_INVALID;
                adapter.refresh();
                continue;
            }

            //we have a valid number. send the message
            requestCode=trans.id;
            Intent sentIntent = new Intent(SMS_SENT_ACTION);

            String number = trans.receiverMobile;
            String message = composeMessage(trans);
            sentIntent.putExtra(EXTRA_TRANS_ID,trans.id);
            sentIntent.putExtra(EXTRA_MODE,"multi");


            // Construct the PendingIntents for the results.
            // FLAG_ONE_SHOT cancels the PendingIntent after use so we
            // can safely reuse the request codes in subsequent runs.
            PendingIntent sentPI = PendingIntent.getBroadcast(this,
                    requestCode,
                    sentIntent,
                    PendingIntent.FLAG_ONE_SHOT);


            try {
                // Send our message.

                if(message.length()>160) {

                    ArrayList<String> parts = smsManager.divideMessage(message);
                    ArrayList<PendingIntent> sentIntents = new ArrayList<>();
                    sentIntents.add(sentPI);
                    smsManager.sendMultipartTextMessage(number, null, parts, sentIntents, null);
                }else{

                    smsManager.sendTextMessage(number,null,message,sentPI,null);

                }
                return;

            } catch (Exception e) {
                trans.smsStatus=SMS_STATUS_PERMISSION_ERROR;
                adapter.refresh();
                e.printStackTrace();
            }
        }
        viewmodel.sendingSms=false;
        sendSms.setEnabled(true);
        progress.setText("");

    }

    private boolean isValidMobileNumber(String number){

        Log.d("CHECKING NUMBER:",number);
        if(number.trim().length()!=10)
            return false;

        if(!TextUtils.isDigitsOnly(number))
            return false;

        return true;

    }

    private String composeMessage(TransactionForList trans){

        String result="We have done RTGS transfer of ";
        result=result+Transaction.amountAsString(trans.amount);
        result=result+" to your account " ;
        result=result+trans.receiver;
        result=result+". Kindly acknowledge the same. -PAPCO OFFSET";

        return result;

    }

    private void clearAllSmsStatus(){

        for(TransactionForList trans:viewmodel.getTransactions().getValue()){
            trans.smsStatus=SMS_STATUS_NOT_ATTEMPTED;
        }
        adapter.refresh();
    }

    private void recyclerViewLongClicked(View view, final TransactionForList transactionForList) {

        PopupMenu popup=new PopupMenu(this,view);
        popup.getMenuInflater().inflate(R.menu.send_sms,popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                if(menuItem.getItemId()==R.id.action_single_sms){

                    viewmodel.currentlySendingTransaction=transactionForList;
                    sendSMS();
                    return true;
                }

                return false;
            }
        });
        popup.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode==PERMISSION_REQUEST_SMS){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                if(viewmodel.currentlySendingTransaction==null)
                    sendNextMessage();
                else {
                    sendSingleMessage(viewmodel.currentlySendingTransaction);
                }
            }else{
                viewmodel.sendingSms=false;
            }
        }
    }

    class SMSAdapter extends RecyclerView.Adapter<SMSAdapter.SMSViewHolder>{


        private List<TransactionForList> data;


        public SMSAdapter(List<TransactionForList> data){
            this.data=data;
            setHasStableIds(true);
        }

        @Override
        public long getItemId(int position) {
            return data.get(position).id;
        }

        @NonNull
        @Override
        public SMSViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            return new SMSViewHolder(getLayoutInflater().inflate(R.layout.sms_list_item,parent,false));
        }

        @Override
        public void onBindViewHolder(@NonNull SMSViewHolder holder, int position) {

            holder.name.setText(data.get(holder.getAdapterPosition()).receiver);
            holder.status.setText(getSmsStatus(data.get(holder.getAdapterPosition()).smsStatus));
            if(data.get(holder.getAdapterPosition()).receiverMobile.equals("")){
                holder.mobilenumer.setVisibility(View.INVISIBLE);
                holder.icon.setVisibility(View.INVISIBLE);
            }else{
                holder.mobilenumer.setVisibility(View.VISIBLE);
                holder.icon.setVisibility(View.VISIBLE);
                holder.mobilenumer.setText(data.get(holder.getAdapterPosition()).receiverMobile);
            }

            if(data.get(holder.getAdapterPosition()).smsStatus!=SMS_STATUS_SENT)
                holder.status.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            else
                holder.status.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public void setData(List<TransactionForList> data){
            this.data=data;
            notifyDataSetChanged();

        }

        public void refresh(){
            notifyDataSetChanged();
        }

        private String getSmsStatus(int smsStatus){

            switch (smsStatus){

                case SMS_STATUS_NOT_ATTEMPTED:
                    return "";

                case SMS_STATUS_SENT:
                    return "SMS SENT";

                case SMS_STATUS_NO_SERVICE:
                    return "NO SERVICE";

                case SMS_STATUS_RECEIVER_NUMBER_INVALID:
                    return "INVALID PHONE NO";

            }

            return "UNKNOWN ERROR";

        }

        class SMSViewHolder extends RecyclerView.ViewHolder{

            TextView name,status,mobilenumer;
            ImageView icon;

            public SMSViewHolder(View itemView) {
                super(itemView);
                name=itemView.findViewById(R.id.sms_name);
                status=itemView.findViewById(R.id.sms_status);
                mobilenumer=itemView.findViewById(R.id.sms_mobilenumber);
                icon=itemView.findViewById(R.id.sms_icon);

                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        if(viewmodel.sendingSms || viewmodel.currentlySendingTransaction!=null)
                            return false;
                        else
                            recyclerViewLongClicked(view,data.get(getAdapterPosition()));

                        return true;
                    }
                });
            }
        }
    }



    private class SmsResultReceiver extends BroadcastReceiver {


        @Override
        public void onReceive(Context context, Intent intent) {

            // A simple result Toast text.
            String mode = null;

            // Get the result action.
            String action = intent.getAction();

            // Retrieve the recipient's number and message.
            int transId=intent.getIntExtra(EXTRA_TRANS_ID,-1);
            mode=intent.getStringExtra(EXTRA_MODE);

            TransactionForList foundTrans=new TransactionForList();
            for(TransactionForList t:viewmodel.getTransactions().getValue()){

                if(t.id==transId){
                    foundTrans=t;
                }

            }

            // This is the result for a send.
            if (SMS_SENT_ACTION.equals(action)) {
                int resultCode = getResultCode();
                foundTrans.smsStatus=translateSentResult(resultCode);
                adapter.refresh();

            }

            if(mode.equals("multi"))
                sendNextMessage();
            //result = number + ", " + message + "\n" + result;
            //Toast.makeText(context, result, Toast.LENGTH_LONG).show();
        }

        int translateSentResult(int resultCode) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    return SMS_STATUS_SENT;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    return SMS_STATUS_GENERIC_FAILURE;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    return SMS_STATUS_UNKNOWN_ERROR;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    return SMS_STATUS_UNKNOWN_ERROR;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    return SMS_STATUS_NO_SERVICE;
                default:
                    return SMS_STATUS_UNKNOWN_ERROR;
            }
        }


    }

}
