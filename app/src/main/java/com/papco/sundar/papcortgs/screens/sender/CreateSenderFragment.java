package com.papco.sundar.papcortgs.screens.sender;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.papco.sundar.papcortgs.R;
import com.papco.sundar.papcortgs.database.sender.Sender;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateSenderFragment extends Fragment {

    EditText editName,editAccountNumber,editAccountType,editIfsc,editMobile,editBank,editEmail;
    TextInputLayout nameLayout,accountNumberLayout,accountTypeLayout,ifscLayout,mobileLayout,bankLayout,emailLayout;
    SenderActivityVM viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel=ViewModelProviders.of(getActivity()).get(SenderActivityVM.class);
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
        if(viewModel.editingSender==null)
            ((SenderActivity)getActivity()).getSupportActionBar().setTitle("Create sender");
        else
            ((SenderActivity)getActivity()).getSupportActionBar().setTitle("Update sender");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.fragment_new_edit_person,container,false);

        editName=view.findViewById(R.id.sender_account_name);
        editAccountNumber=view.findViewById(R.id.sender_account_number);
        editAccountType=view.findViewById(R.id.sender_account_type);
        editIfsc=view.findViewById(R.id.sender_ifsc);
        editBank=view.findViewById(R.id.sender_bank);
        editMobile=view.findViewById(R.id.sender_mobile_number);
        editEmail=view.findViewById(R.id.sender_email);
        nameLayout=view.findViewById(R.id.sender_name_layout);
        accountNumberLayout=view.findViewById(R.id.sender_account_number_layout);
        accountTypeLayout=view.findViewById(R.id.sender_account_type_layout);
        ifscLayout=view.findViewById(R.id.sender_account_ifsc_layout);
        bankLayout=view.findViewById(R.id.sender_account_bank_layout);
        mobileLayout=view.findViewById(R.id.sender_mobile_layout);
        emailLayout=view.findViewById(R.id.sender_email_layout);


        editName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                nameLayout.setError(null);
                nameLayout.setErrorEnabled(false);
            }
        });

        editAccountNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                accountNumberLayout.setError(null);
                accountNumberLayout.setErrorEnabled(false);
            }
        });

        editAccountType.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                accountTypeLayout.setError(null);
                accountTypeLayout.setErrorEnabled(false);
            }
        });

        InputFilter[] filters={new InputFilter.AllCaps(),new InputFilter.LengthFilter(11)};
        editIfsc.getText().setFilters(filters);

        editIfsc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                ifscLayout.setError(null);
                ifscLayout.setErrorEnabled(false);
            }
        });

        editIfsc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(editIfsc.getSelectionStart()<4)
                    editIfsc.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                else
                    editIfsc.setInputType(InputType.TYPE_CLASS_NUMBER);

            }
        });

        editBank.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                bankLayout.setError(null);
                bankLayout.setErrorEnabled(false);
            }
        });

        editEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                bankLayout.setError(null);
                bankLayout.setErrorEnabled(false);
            }
        });

        if(viewModel.editingSender!=null)
            loadValues();


        return view;
    }

    private void loadValues() {

        Sender sender=viewModel.editingSender;
        editName.setText(sender.name);
        editAccountNumber.setText(sender.accountNumber);
        editAccountType.setText(sender.accountType);
        editIfsc.setText(sender.ifsc);
        editBank.setText(sender.bank);
        editMobile.setText(sender.mobileNumber);
        editEmail.setText(sender.email);
    }

    private void validateAndSave() {

        if(!isAllFieldsValid())
            return;

        if(viewModel.editingSender==null){ //add as new sender

            Sender sender=new Sender();
            sender.name=editName.getText().toString();
            sender.accountNumber=editAccountNumber.getText().toString();
            sender.accountType=editAccountType.getText().toString();
            sender.ifsc=editIfsc.getText().toString();
            sender.bank=editBank.getText().toString();
            sender.mobileNumber=editMobile.getText().toString();
            sender.email=editEmail.getText().toString();

            viewModel.addSender(sender);
            ((SenderActivity)getActivity()).popBackStack();
        }else{

            Sender sender=new Sender();
            sender.id=viewModel.editingSender.id;
            sender.name=editName.getText().toString();
            sender.accountNumber=editAccountNumber.getText().toString();
            sender.accountType=editAccountType.getText().toString();
            sender.ifsc=editIfsc.getText().toString();
            sender.bank=editBank.getText().toString();
            sender.mobileNumber=editMobile.getText().toString();
            sender.email=editEmail.getText().toString();

            viewModel.editingSender=null;
            viewModel.updateSender(sender);
            ((SenderActivity)getActivity()).popBackStack();
            return;
        }


    }

    private boolean isAllFieldsValid(){

        boolean result=true;
        if (TextUtils.isEmpty(editName.getEditableText())){
            nameLayout.setErrorEnabled(true);
            nameLayout.setError("Enter a valid name");
            result=false;
        }

        if(TextUtils.isEmpty(editAccountNumber.getEditableText()) || Long.parseLong(editAccountNumber.getText().toString())==0){
            accountNumberLayout.setErrorEnabled(true);
            accountNumberLayout.setError("Enter valid account number");
            result=false;
        }

        if (TextUtils.isEmpty(editAccountType.getEditableText())){
            accountTypeLayout.setErrorEnabled(true);
            accountTypeLayout.setError("Enter valid account type");
            result=false;
        }

        if (TextUtils.isEmpty(editIfsc.getEditableText())){
            ifscLayout.setErrorEnabled(true);
            ifscLayout.setError("Enter valid IFS code");
            result=false;
        }

        if (TextUtils.isEmpty(editBank.getEditableText())){
            bankLayout.setErrorEnabled(true);
            bankLayout.setError("Enter valid bank name");
            result=false;
        }

        if(!TextUtils.isEmpty(editEmail.getEditableText())){
            if(!isValidEmail(editEmail.getText().toString())){
                emailLayout.setErrorEnabled(true);
                emailLayout.setError("Enter a valid email");
                return false;
            }

        }

        return result;
    }

    private boolean isValidEmail(String email){

        String EMAIL_REGEX="^[\\w-\\+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-zA-Z]{2,})$";
        Pattern pattern=Pattern.compile(EMAIL_REGEX,Pattern.CASE_INSENSITIVE);
        Matcher matcher;

        matcher=pattern.matcher(email);
        return matcher.matches();

    }
}
