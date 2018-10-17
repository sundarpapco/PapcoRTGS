package com.papco.sundar.papcortgs;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.List;

public class CreateTransactionFragment extends Fragment {

    EditText sender,receiver,amount,remarks;
    ImageView senderMore,receiverMore;
    TransactionActivityVM viewmodel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewmodel=ViewModelProviders.of(getActivity()).get(TransactionActivityVM.class);

        viewmodel.senders.observe(this, new Observer<List<Sender>>() {
            @Override
            public void onChanged(@Nullable List<Sender> senders) {
                if(sender==null || senders.size()==0){
                    viewmodel.selectedSender.setValue(null);
                    return;
                }

                if(viewmodel.selectedSender.getValue()==null){
                    viewmodel.selectedSender.setValue(senders.get(0));
                }
            }
        });

        viewmodel.selectedSender.observe(this, new Observer<Sender>() {
            @Override
            public void onChanged(@Nullable Sender sender) {
                if(sender==null)
                    CreateTransactionFragment.this.sender.setText("Tap to add  sender");
                else
                    CreateTransactionFragment.this.sender.setText(sender.name);
            }
        });

        viewmodel.receivers.observe(this, new Observer<List<Receiver>>() {
            @Override
            public void onChanged(@Nullable List<Receiver> receivers) {
                if(receivers==null || receivers.size()==0){
                    viewmodel.selectedReceiver.setValue(null);
                    return;
                }

                if(viewmodel.selectedReceiver.getValue()==null){
                    viewmodel.selectedReceiver.setValue(receivers.get(0));
                }
            }
        });

        viewmodel.selectedReceiver.observe(this, new Observer<Receiver>() {
            @Override
            public void onChanged(@Nullable Receiver receiver) {
                Log.d("SUNDAR","Receiver change");
                if(receiver==null)
                    CreateTransactionFragment.this.receiver.setText("Tap to add beneficiery");
                else {
                    CreateTransactionFragment.this.receiver.setText(receiver.name);
                }
            }
        });

        viewmodel.editingTransaction.observe(this, new Observer<Transaction>() {
            @Override
            public void onChanged(@Nullable Transaction transaction) {
                if(transaction!=null)
                    loadTransactionValues();
            }
        });

        setHasOptionsMenu(true);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_done,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.action_done){
            validateAndSave();
            return true;
        }
        return false;
    }

    @Override
    public void onStart() {
        super.onStart();

        if(viewmodel.editingTransactionId!=-1 && viewmodel.editingTransaction.getValue()==null) {
            ((TransactionActivity) getActivity()).getSupportActionBar().setTitle("Update transaction");
            ((TransactionActivity)getActivity()).getSupportActionBar().setSubtitle(null);
        }else {
            ((TransactionActivity) getActivity()).getSupportActionBar().setTitle("Create transaction");
            ((TransactionActivity)getActivity()).getSupportActionBar().setSubtitle(null);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View ui=inflater.inflate(R.layout.create_transaction,container,false);
        
        sender=ui.findViewById(R.id.create_transaction_from);
        receiver=ui.findViewById(R.id.create_transaction_to);
        amount=ui.findViewById(R.id.create_transaction_amount);
        remarks=ui.findViewById(R.id.create_transaction_remarks);
        senderMore=ui.findViewById(R.id.create_transaction_sender_dots);
        receiverMore=ui.findViewById(R.id.create_transaction_receiver_dots);

        sender.setKeyListener(null);
        sender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(viewmodel.selectedSender.getValue()==null)
                    ((TransactionActivity)getActivity()).showSenderActivity();
                else
                    ((TransactionActivity)getActivity()).showSenderSelectFragment();
            }
        });

        receiver.setKeyListener(null);
        receiver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(viewmodel.selectedReceiver.getValue()==null)
                    ((TransactionActivity)getActivity()).showReceiverActivity();
                else
                    ((TransactionActivity)getActivity()).showReceiverSelectFragment();

            }
        });
        
        senderMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((TransactionActivity)getActivity()).showSenderActivity();
            }
        });
        
        receiverMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((TransactionActivity)getActivity()).showReceiverActivity();
            }
        });

        if(viewmodel.editingTransactionId!=-1 && viewmodel.editingTransaction.getValue()==null){
            viewmodel.loadTransaction(viewmodel.editingTransactionId);
        }else{
            amount.setText("");
            remarks.setText("ON ACCOUNT");
        }
        
        return ui;
    }


    private void validateAndSave() {

        if(viewmodel.selectedSender.getValue()==null) {
            Toast.makeText(getActivity(),"Select a Sender",Toast.LENGTH_SHORT).show();
            return;
        }

        if(viewmodel.selectedReceiver.getValue()==null) {
            Toast.makeText(getActivity(),"Select a Receiver",Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(amount.getText()) || Integer.parseInt(amount.getText().toString())==0) {
            Toast.makeText(getActivity(),"Enter amount",Toast.LENGTH_SHORT).show();
            return;
        }

        hideKeyboard();


        if(viewmodel.editingTransactionId==-1) {

            //savehere
            Transaction transaction = new Transaction();
            transaction.groupId = viewmodel.currentGroup.id;
            transaction.senderId = viewmodel.selectedSender.getValue().id;
            transaction.receiverId = viewmodel.selectedReceiver.getValue().id;
            transaction.amount = Integer.parseInt(amount.getText().toString());
            transaction.remarks = remarks.getText().toString();
            viewmodel.addTransaction(transaction);
            ((TransactionActivity) getActivity()).popBackStack();

        }else{

            //updatehere
            Transaction transaction=viewmodel.editingTransaction.getValue();
            transaction.senderId = viewmodel.selectedSender.getValue().id;
            transaction.receiverId = viewmodel.selectedReceiver.getValue().id;
            transaction.amount = Integer.parseInt(amount.getText().toString());
            transaction.remarks = remarks.getText().toString();
            viewmodel.updateTransaction(transaction);
            ((TransactionActivity) getActivity()).popBackStack();
        }

    }

    private void hideKeyboard(){

            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(amount.getWindowToken(), 0);

    }

    private void loadTransactionValues(){

        Transaction trans=viewmodel.editingTransaction.getValue();
        viewmodel.selectedSender.setValue(trans.sender);
        viewmodel.selectedReceiver.setValue(trans.receiver);
        amount.setText(Integer.toString(trans.amount));
        remarks.setText(trans.remarks);

    }
}
