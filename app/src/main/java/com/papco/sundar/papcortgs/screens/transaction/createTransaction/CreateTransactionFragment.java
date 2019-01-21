package com.papco.sundar.papcortgs.screens.transaction.createTransaction;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.papco.sundar.papcortgs.R;
import com.papco.sundar.papcortgs.database.receiver.Receiver;
import com.papco.sundar.papcortgs.database.sender.Sender;
import com.papco.sundar.papcortgs.screens.password.PasswordDialog;
import com.papco.sundar.papcortgs.screens.transaction.common.TransactionActivity;
import com.papco.sundar.papcortgs.screens.transaction.common.TransactionActivityVM;

public class CreateTransactionFragment extends Fragment {


    public static CreateTransactionFragment getInstance(int groupId, int loadTransactionId){

        Bundle bundle=new Bundle();
        bundle.putInt(KEY_GROUP_ID,groupId);
        bundle.putInt(KEY_LOAD_TRANSACTION_ID,loadTransactionId);
        CreateTransactionFragment fragment=new CreateTransactionFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    public static final String KEY_GROUP_ID = "key_groupId";
    public static final String KEY_LOAD_TRANSACTION_ID = "key_loadTransactionId";
    EditText senderField,receiverField,amountField,remarksField;
    ImageView senderMore,receiverMore;
    CreateTransactionVM viewModel;

    // region override Methods ---------------------------------------

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        if(isEditingTransaction()) {
            setTitle("Update transaction");
        }else {
            setTitle("Create transaction");
        }
        setSubTitle(null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(viewModel ==null)
            initializeViewModel();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View ui=inflater.inflate(R.layout.create_transaction,container,false);
        
        senderField=ui.findViewById(R.id.create_transaction_from);
        receiverField=ui.findViewById(R.id.create_transaction_to);
        amountField=ui.findViewById(R.id.create_transaction_amount);
        remarksField=ui.findViewById(R.id.create_transaction_remarks);
        senderMore=ui.findViewById(R.id.create_transaction_sender_dots);
        receiverMore=ui.findViewById(R.id.create_transaction_receiver_dots);

        senderField.setKeyListener(null);
        senderField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(viewModel.getSelectedSender().getValue()==null)
                    ((TransactionActivity)getActivity()).showSenderActivity();
                else
                    ((TransactionActivity)getActivity()).showSenderSelectFragment();
            }
        });

        receiverField.setKeyListener(null);
        receiverField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(viewModel.getSelectedReceiver().getValue()==null)
                    ((TransactionActivity)getActivity()).showReceiverActivity();
                else
                    ((TransactionActivity)getActivity()).showReceiverSelectFragment();

            }
        });
        
        senderMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askForPassword(PasswordDialog.CODE_SENDERS);
            }
        });
        
        receiverMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askForPassword(PasswordDialog.CODE_RECEIVERS);
            }
        });


        
        return ui;
    }

    // endregion override Methods ---------------------------------------

    private void setTitle(String title){

        //Using try catch because if there is a casting error, then this method will simply not work
        try {
            if (((AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
                ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle(title);
            }
        }catch (Exception e){

        }

    }

    private void setSubTitle(String subTitle){

        //Using try catch because if there is a casting error, then this method will simply not work
        try {
            if (((AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
                ((AppCompatActivity) requireActivity()).getSupportActionBar().setSubtitle(subTitle);
            }
        }catch (Exception e){

        }

    }

    private boolean isEditingTransaction(){
        return getArguments().getInt(KEY_LOAD_TRANSACTION_ID)!=-1;
    }

    private void initializeViewModel(){

        viewModel =ViewModelProviders.of(getActivity()).get(TransactionActivityVM.class)
                .getCreateTransactionVM(getArguments().getInt(KEY_GROUP_ID),getArguments().getInt(KEY_LOAD_TRANSACTION_ID));

        viewModel.getSelectedSender().observe(this, new Observer<Sender>() {
            @Override
            public void onChanged(@Nullable Sender sender) {
                if(sender==null){
                    senderField.setText("Tap to add a Sender");
                    return;
                }else{
                    senderField.setText(sender.name);
                }
            }
        });


        viewModel.getSelectedReceiver().observe(this, new Observer<Receiver>() {
            @Override
            public void onChanged(@Nullable Receiver receiver) {
                if(receiver==null)
                    receiverField.setText("Tap to add beneficiery");
                else {
                    receiverField.setText(receiver.name);
                }
            }
        });

        viewModel.getAmount().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                if(integer==null || integer==0)
                    amountField.setText("");
                else
                    amountField.setText(Integer.toString(integer));

            }
        });

        viewModel.getRemarks().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String remarks) {
                if(remarks==null)
                    remarksField.setText("ON ACCOUNT");
                else
                    remarksField.setText(remarks);
            }
        });

    }

    private void askForPassword(int code){

        PasswordDialog passwordDialog=new PasswordDialog();
        FragmentManager manager=getActivity().getSupportFragmentManager();
        passwordDialog.setRequestCode(code);
        passwordDialog.show(manager,"passwordDialog");
    }

    private void validateAndSave() {

        if(viewModel.getSelectedSender().getValue()==null) {
            Toast.makeText(getActivity(),"Select a Sender",Toast.LENGTH_SHORT).show();
            return;
        }

        if(viewModel.getSelectedReceiver().getValue()==null) {
            Toast.makeText(getActivity(),"Select a Receiver",Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(amountField.getText()) || Integer.parseInt(amountField.getText().toString())==0) {
            Toast.makeText(getActivity(),"Enter amount",Toast.LENGTH_SHORT).show();
            return;
        }

        hideKeyboard();

        viewModel.setAmount(Integer.parseInt(amountField.getText().toString()));
        viewModel.setRemarks(remarksField.getText().toString());

        if(isEditingTransaction())
            viewModel.updateTransaction();
        else
            viewModel.saveNewTransaction();


        ((TransactionActivity) getActivity()).popBackStack();

    }

    private void hideKeyboard(){

            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(amountField.getWindowToken(), 0);

    }
}
