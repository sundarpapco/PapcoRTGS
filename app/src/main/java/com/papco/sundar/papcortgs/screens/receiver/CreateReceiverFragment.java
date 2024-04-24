package com.papco.sundar.papcortgs.screens.receiver;

import android.os.Bundle;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputLayout;
import com.papco.sundar.papcortgs.R;
import com.papco.sundar.papcortgs.common.Event;
import com.papco.sundar.papcortgs.database.receiver.Receiver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CreateReceiverFragment extends Fragment {

    private static final String KEY_RECEIVER_ID = "key_for_editing_receiver_id";
    public static final String EVENT_SUCCESS = "EVENT_SUCCESS";

    static CreateReceiverFragment getInstance(int editingReceiverId) {

        Bundle args = new Bundle();
        args.putInt(KEY_RECEIVER_ID, editingReceiverId);
        CreateReceiverFragment instance = new CreateReceiverFragment();
        instance.setArguments(args);
        return instance;

    }


    EditText editName, editAccountNumber, confirmAccountNumber, editAccountType, editIfsc, editMobile, editBank, editEmail,editDisplayName;
    TextInputLayout nameLayout, accountNumberLayout, confirmAccountNumberLayout, accountTypeLayout, ifscLayout, mobileLayout, bankLayout, emailLayout,displayNameLayout;
    CreateReceiverVM viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CreateReceiverVM.class);
        setHasOptionsMenu(true);

        if(isEditingMode())
            viewModel.loadReceiver(getEditingReceiverId());
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
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
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            if (isEditingMode())
                actionBar.setTitle("Update sender");
            else
                actionBar.setTitle("Create sender");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_edit_person, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        linkViews(view);
        initViews();
        observeViewModel();
    }

    private void initViews() {
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

        editDisplayName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                editDisplayName.setError(null);
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
    }

    private void observeViewModel() {

        viewModel.getEventStatus().observe(getViewLifecycleOwner(), new Observer<Event<String>>() {
            @Override
            public void onChanged(@Nullable Event<String> event) {

                if (event == null || event.isAlreadyHandled())
                    return;

                String result = event.handleEvent();
                if (result.equals(EVENT_SUCCESS))
                    ((ReceiverActivity) requireActivity()).popBackStack();
                else
                    Toast.makeText(requireContext(), result, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getReceiver().observe(getViewLifecycleOwner(), new Observer<Event<Receiver>>() {
            @Override
            public void onChanged(Event<Receiver> event) {

                if (event == null || event.isAlreadyHandled())
                    return;

                loadValues(event.handleEvent());
            }
        });

    }


    private void linkViews(View view) {
        editName = view.findViewById(R.id.sender_account_name);
        editDisplayName=view.findViewById(R.id.sender_display_name);
        editAccountNumber = view.findViewById(R.id.sender_account_number);
        confirmAccountNumber = view.findViewById(R.id.sender_confirm_account_number);
        editAccountType = view.findViewById(R.id.sender_account_type);
        editIfsc = view.findViewById(R.id.sender_ifsc);
        editBank = view.findViewById(R.id.sender_bank);
        editMobile = view.findViewById(R.id.sender_mobile_number);
        editEmail = view.findViewById(R.id.sender_email);
        nameLayout = view.findViewById(R.id.sender_name_layout);
        accountNumberLayout = view.findViewById(R.id.sender_account_number_layout);
        displayNameLayout=view.findViewById(R.id.sender_display_name_layout);
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

    private void loadValues(Receiver receiver) {

        editName.setText(receiver.name);
        editDisplayName.setText(receiver.displayName);
        editAccountNumber.setText(receiver.accountNumber);
        confirmAccountNumber.setText(receiver.accountNumber);
        editAccountType.setText(receiver.accountType);
        editIfsc.setText(receiver.ifsc);
        editBank.setText(receiver.bank);
        editMobile.setText(receiver.mobileNumber);
        editEmail.setText(receiver.email);
    }

    private void validateAndSave() {

        if (!isAllFieldsValid())
            return;

        Receiver receiver = new Receiver();

        receiver.name = editName.getText().toString().trim();
        receiver.accountNumber = editAccountNumber.getText().toString().trim();
        receiver.accountType = editAccountType.getText().toString().trim();
        receiver.ifsc = editIfsc.getText().toString().trim();
        receiver.bank = editBank.getText().toString().trim();
        receiver.mobileNumber = editMobile.getText().toString().trim();
        receiver.email = editEmail.getText().toString().trim();
        receiver.displayName=editDisplayName.getText().toString();

        if (!isEditingMode()) { //add as new receiver
            viewModel.addReceiver(receiver);
        } else {
            receiver.id = getEditingReceiverId();
            viewModel.updateReceiver(receiver);

        }


    }

    private boolean isAllFieldsValid() {

        boolean result = true;

        if (TextUtils.isEmpty(editDisplayName.getEditableText())) {
            editDisplayName.setError("Enter a valid display name");
            result = false;
        }

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

        String EMAIL_REGEX = "^[\\w-+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-zA-Z]{2,})$";
        Pattern pattern = Pattern.compile(EMAIL_REGEX, Pattern.CASE_INSENSITIVE);
        Matcher matcher;

        matcher = pattern.matcher(email);
        return matcher.matches();

    }

    private int getEditingReceiverId() {

        if (getArguments() != null)
            return getArguments().getInt(KEY_RECEIVER_ID, -1);
        else
            return -1;

    }

    private boolean isEditingMode() {
        return getEditingReceiverId() != -1;
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
