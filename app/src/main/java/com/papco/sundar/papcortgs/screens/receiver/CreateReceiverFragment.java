package com.papco.sundar.papcortgs.screens.receiver;

import android.arch.lifecycle.Observer;
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
import com.papco.sundar.papcortgs.common.Event;
import com.papco.sundar.papcortgs.database.receiver.Receiver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateReceiverFragment extends Fragment {

    EditText editName, editAccountNumber, confirmAccountNumber, editAccountType, editIfsc, editMobile, editBank, editEmail;
    TextInputLayout nameLayout, accountNumberLayout, confirmAccountNumberLayout, accountTypeLayout, ifscLayout, mobileLayout, bankLayout, emailLayout;
    ReceiverActivityVM viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(getActivity()).get(ReceiverActivityVM.class);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_done, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_done) {
            validateAndSave();
            return true;
        }

        return false;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (viewModel.editingReceiver == null)
            ((ReceiverActivity) getActivity()).getSupportActionBar().setTitle("Create receiver");
        else
            ((ReceiverActivity) getActivity()).getSupportActionBar().setTitle("Update receiver");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_new_edit_person, container, false);

        linkViews(view);

        editName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                editName.setError(null);
            }
        });
        editAccountNumber.addTextChangedListener(new ClearErrorTextWatcher(editAccountNumber));
        editAccountNumber.addTextChangedListener(new ClearErrorTextWatcher(confirmAccountNumber));
        editAccountNumber.setOnFocusChangeListener(new CheckSameContentListener(confirmAccountNumber));

        confirmAccountNumber.addTextChangedListener(new ClearErrorTextWatcher(confirmAccountNumber));
        confirmAccountNumber.setCustomSelectionActionModeCallback(new DisableEditTextPastingCallBack());
        confirmAccountNumber.setLongClickable(false);
        confirmAccountNumber.setTextIsSelectable(false);
        confirmAccountNumber.setOnFocusChangeListener(new CheckSameContentListener(editAccountNumber));

        editAccountType.addTextChangedListener(new ClearErrorTextWatcher(editAccountType));


        editIfsc.addTextChangedListener(new ClearErrorTextWatcher(editIfsc));
        InputFilter[] filters = {new InputFilter.AllCaps(), new InputFilter.LengthFilter(11)};
        editIfsc.getText().setFilters(filters);
        editIfsc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (editIfsc.getSelectionStart() < 4)
                    editIfsc.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                else
                    editIfsc.setInputType(InputType.TYPE_CLASS_NUMBER);

            }
        });

        editBank.addTextChangedListener(new ClearErrorTextWatcher(editBank));
        editEmail.addTextChangedListener(new ClearErrorTextWatcher(editEmail));


        if (viewModel.editingReceiver != null)
            loadValues();


        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        viewModel.shouldPopUpBackStack.observe(getViewLifecycleOwner(), new Observer<Event<Boolean>>() {
            @Override
            public void onChanged(@Nullable Event<Boolean> event) {

                if (event != null && !event.isAlreadyHandled()) {

                    Boolean result = event.handleEvent();
                    if (result != null && result)
                        ((ReceiverActivity) getActivity()).popBackStack();
                }
            }
        });

    }

    private void linkViews(View view) {
        editName = view.findViewById(R.id.sender_account_name);
        editAccountNumber = view.findViewById(R.id.sender_account_number);
        confirmAccountNumber = view.findViewById(R.id.sender_confirm_account_number);
        editAccountType = view.findViewById(R.id.sender_account_type);
        editIfsc = view.findViewById(R.id.sender_ifsc);
        editBank = view.findViewById(R.id.sender_bank);
        editMobile = view.findViewById(R.id.sender_mobile_number);
        editEmail = view.findViewById(R.id.sender_email);
        nameLayout = view.findViewById(R.id.sender_name_layout);
        accountNumberLayout = view.findViewById(R.id.sender_account_number_layout);
        confirmAccountNumberLayout = view.findViewById(R.id.sender_confirm_account_number_layout);
        accountTypeLayout = view.findViewById(R.id.sender_account_type_layout);
        ifscLayout = view.findViewById(R.id.sender_account_ifsc_layout);
        bankLayout = view.findViewById(R.id.sender_account_bank_layout);
        mobileLayout = view.findViewById(R.id.sender_mobile_layout);
        emailLayout = view.findViewById(R.id.sender_email_layout);
    }


    private void checkAccountNumbersAndSetError() {
        String accountNumber = editAccountNumber.getText().toString();
        String confirmationNumber = confirmAccountNumber.getText().toString();

        if (!accountNumber.equals(confirmationNumber)) {
            confirmAccountNumber.setError("Account number and confirmation number does not match");
        } else {
            confirmAccountNumber.setError(null);
        }
    }

    private void loadValues() {

        Receiver sender = viewModel.editingReceiver;
        editName.setText(sender.name);
        editAccountNumber.setText(sender.accountNumber);
        confirmAccountNumber.setText(sender.accountNumber);
        editAccountType.setText(sender.accountType);
        editIfsc.setText(sender.ifsc);
        editBank.setText(sender.bank);
        editMobile.setText(sender.mobileNumber);
        editEmail.setText(sender.email);
    }

    private void validateAndSave() {

        if (!isAllFieldsValid())
            return;

        if (viewModel.editingReceiver == null) { //add as new sender

            Receiver sender = new Receiver();
            sender.name = editName.getText().toString();
            sender.accountNumber = editAccountNumber.getText().toString();
            sender.accountType = editAccountType.getText().toString();
            sender.ifsc = editIfsc.getText().toString();
            sender.bank = editBank.getText().toString();
            sender.mobileNumber = editMobile.getText().toString();
            sender.email = editEmail.getText().toString();

            viewModel.addReceiver(sender);
            //((ReceiverActivity) getActivity()).popBackStack();
        } else {

            Receiver sender = new Receiver();
            sender.id = viewModel.editingReceiver.id;
            sender.name = editName.getText().toString();
            sender.accountNumber = editAccountNumber.getText().toString();
            sender.accountType = editAccountType.getText().toString();
            sender.ifsc = editIfsc.getText().toString();
            sender.bank = editBank.getText().toString();
            sender.mobileNumber = editMobile.getText().toString();
            sender.email = editEmail.getText().toString();

            viewModel.editingReceiver = null;
            viewModel.updateReceiver(sender);
            //((ReceiverActivity) getActivity()).popBackStack();
            return;
        }


    }

    private boolean isAllFieldsValid() {

        boolean result = true;
        if (TextUtils.isEmpty(editName.getEditableText())) {
            editName.setError("Enter a valid name");
            result = false;
        }

        String accountNumber = editAccountNumber.getEditableText().toString();
        if (TextUtils.isEmpty(accountNumber)) {
            editAccountNumber.setError("Enter valid account number");
            result = false;
        }

        String confirmationAccountNumber = confirmAccountNumber.getEditableText().toString();
        if (!accountNumber.equals(confirmationAccountNumber)) {
            confirmAccountNumber.setError("Account number and confirmation number not matching");
            result = false;
        }


        if (TextUtils.isEmpty(editAccountType.getEditableText())) {
            editAccountType.setError("Enter valid account type");
            result = false;
        }

        if (TextUtils.isEmpty(editIfsc.getEditableText())) {
            editIfsc.setError("Enter valid IFS code");
            result = false;
        }

        if (TextUtils.isEmpty(editBank.getEditableText())) {
            editBank.setError("Enter valid bank name");
            result = false;
        }

        if (!TextUtils.isEmpty(editEmail.getEditableText())) {
            if (!isValidEmail(editEmail.getText().toString())) {
                editEmail.setError("Enter a valid email");
                return false;
            }

        }

        return result;
    }

    private boolean isValidEmail(String email) {

        String EMAIL_REGEX = "^[\\w-\\+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-zA-Z]{2,})$";
        Pattern pattern = Pattern.compile(EMAIL_REGEX, Pattern.CASE_INSENSITIVE);
        Matcher matcher;

        matcher = pattern.matcher(email);
        return matcher.matches();

    }

    private class CheckSameContentListener implements View.OnFocusChangeListener {

        private EditText editText;

        CheckSameContentListener(EditText checkingField) {
            this.editText = checkingField;
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus || editText.hasFocus())
                return;

            checkAccountNumbersAndSetError();
        }
    }

}
