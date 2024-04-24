package com.papco.sundar.papcortgs.screens.transaction.createTransaction;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.papco.sundar.papcortgs.R;
import com.papco.sundar.papcortgs.database.receiver.Receiver;
import com.papco.sundar.papcortgs.database.sender.Sender;
import com.papco.sundar.papcortgs.screens.password.PasswordDialog;
import com.papco.sundar.papcortgs.screens.transaction.common.TransactionActivity;

import static com.papco.sundar.papcortgs.screens.transaction.createTransaction.ReceiverSelectFragment.KEY_RECEIVER_SELECTION;
import static com.papco.sundar.papcortgs.screens.transaction.createTransaction.SenderSelectFragment.KEY_SENDER_SELECTION;

public class CreateTransactionFragment extends Fragment {


    public static CreateTransactionFragment getInstance(int groupId, int loadTransactionId, int defaultSenderId) {

        Bundle bundle = new Bundle();
        bundle.putInt(KEY_GROUP_ID, groupId);
        bundle.putInt(KEY_LOAD_TRANSACTION_ID, loadTransactionId);
        bundle.putInt(KEY_DEFAULT_SENDER_ID, defaultSenderId);

        CreateTransactionFragment fragment = new CreateTransactionFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    public static final String KEY_GROUP_ID = "key_groupId";
    public static final String KEY_LOAD_TRANSACTION_ID = "key_loadTransactionId";
    private static final String KEY_DEFAULT_SENDER_ID = "key_default_sender";

    EditText senderField, receiverField, amountField, remarksField;
    ImageView senderMore, receiverMore;
    CreateTransactionVM viewModel;

    // region override Methods ---------------------------------------

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CreateTransactionVM.class);
        setHasOptionsMenu(true);
        installSenderAndReceiverSelectionListeners();

        if (isEditingTransaction())
            viewModel.loadTransaction(getTransactionId());
        else
            viewModel.createBlankTransaction(getGroupId(), getDefaultSenderId());

    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_done, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("SUNDAR", "Menu Item selected");
        if (item.getItemId() == R.id.action_done) {
            validateAndSave();
            return true;
        }
        return false;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (isEditingTransaction()) {
            setTitle("Update transaction");
        } else {
            setTitle("Create transaction");
        }
        clearSubTitle();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.create_transaction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        observeViewModel();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (!TextUtils.isEmpty(amountField.getText().toString().trim()))
            viewModel.saveAmount(Integer.parseInt(amountField.getText().toString()));
        viewModel.saveRemarks(remarksField.getText().toString());
    }

    private void initViews(View ui) {

        senderField = ui.findViewById(R.id.create_transaction_from);
        receiverField = ui.findViewById(R.id.create_transaction_to);
        amountField = ui.findViewById(R.id.create_transaction_amount);
        remarksField = ui.findViewById(R.id.create_transaction_remarks);
        senderMore = ui.findViewById(R.id.create_transaction_sender_dots);
        receiverMore = ui.findViewById(R.id.create_transaction_receiver_dots);

        senderField.setKeyListener(null);
        senderField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (viewModel.getSelectedSender().getValue() == null)
                    ((TransactionActivity) requireActivity()).showSenderActivity();
                else
                    ((TransactionActivity) requireActivity()).showSenderSelectFragment();
            }
        });

        receiverField.setKeyListener(null);
        receiverField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (viewModel.getSelectedReceiver().getValue() == null)
                    ((TransactionActivity) requireActivity()).showReceiverActivity();
                else
                    ((TransactionActivity) requireActivity()).showReceiverSelectFragment();

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

    }

    private void observeViewModel() {

        viewModel.getSelectedSender().observe(getViewLifecycleOwner(), new Observer<Sender>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onChanged(@Nullable Sender sender) {
                if (sender == null) {
                    senderField.setText("Tap to add a Sender");
                } else {
                    //Toast.makeText(requireContext(),"Selecting sender " + sender.name,Toast.LENGTH_SHORT).show();
                    senderField.setText(sender.displayName);
                }
            }
        });


        viewModel.getSelectedReceiver().observe(getViewLifecycleOwner(), new Observer<Receiver>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onChanged(@Nullable Receiver receiver) {
                if (receiver == null)
                    receiverField.setText("Tap to add beneficiary");
                else {
                    receiverField.setText(receiver.displayName);
                }
            }
        });

        viewModel.getAmount().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onChanged(@Nullable Integer integer) {

                //If this call is due to config change, then ignore this call
                //because user might have edited the amount and we should not
                // reset with initial loading data

                if (integer == null || integer == 0)
                    amountField.setText("");
                else
                    amountField.setText(Integer.toString(integer));

            }
        });

        viewModel.getRemarks().observe(getViewLifecycleOwner(), new Observer<String>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onChanged(@Nullable String remarks) {

                if (remarks == null)
                    remarksField.setText("ON ACCOUNT");
                else
                    remarksField.setText(remarks);
            }
        });

    }

    private void installSenderAndReceiverSelectionListeners() {
        //Setting a listener for the receiver selection so that this fragment is notified once the
        //user selects some receiver in the select receiver fragment.
        //This callback is the recommended way of sending result between two fragments
        getParentFragmentManager().setFragmentResultListener(
                KEY_RECEIVER_SELECTION,
                this,
                new FragmentResultListener() {
                    @Override
                    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                        int selectedReceiverId = result.getInt(KEY_RECEIVER_SELECTION);
                        viewModel.selectReceiver(selectedReceiverId);
                    }
                });

        getParentFragmentManager().setFragmentResultListener(
                KEY_SENDER_SELECTION,
                this,
                new FragmentResultListener() {
                    @Override
                    public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                        int selectedSenderId = result.getInt(KEY_SENDER_SELECTION);
                        viewModel.selectSender(selectedSenderId);
                    }
                });

    }


    // endregion override Methods ---------------------------------------

    private void setTitle(String title) {

        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }

    }

    private void clearSubTitle() {

        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle(null);
        }

    }

    private boolean isEditingTransaction() {
        if (getArguments() != null)
            return getArguments().getInt(KEY_LOAD_TRANSACTION_ID) != -1;
        else
            return false;
    }


    private void askForPassword(int code) {

        PasswordDialog passwordDialog = new PasswordDialog();
        FragmentManager manager = requireActivity().getSupportFragmentManager();
        passwordDialog.setRequestCode(code);
        passwordDialog.show(manager, "passwordDialog");
    }

    private void validateAndSave() {

        if (viewModel.getSelectedSender().getValue() == null) {
            Toast.makeText(requireActivity(), "Select a Sender", Toast.LENGTH_SHORT).show();
            return;
        }

        if (viewModel.getSelectedReceiver().getValue() == null) {
            Toast.makeText(requireActivity(), "Select a Receiver", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(amountField.getText()) || Integer.parseInt(amountField.getText().toString()) == 0) {
            Toast.makeText(requireActivity(), "Enter amount", Toast.LENGTH_SHORT).show();
            return;
        }

        hideKeyboard();

        viewModel.setAmount(Integer.parseInt(amountField.getText().toString()));
        viewModel.setRemarks(remarksField.getText().toString());

        if (isEditingTransaction())
            viewModel.updateTransaction(getGroupId(), getTransactionId());
        else
            viewModel.saveNewTransaction(getGroupId());


        ((TransactionActivity) requireActivity()).popBackStack();

    }

    private int getGroupId() {

        if (getArguments() != null)
            return getArguments().getInt(KEY_GROUP_ID);
        else
            return -1;

    }

    private int getTransactionId() {

        if (getArguments() != null)
            return getArguments().getInt(KEY_LOAD_TRANSACTION_ID);
        else
            return -1;

    }

    private int getDefaultSenderId() {

        if (getArguments() != null)
            return getArguments().getInt(KEY_DEFAULT_SENDER_ID, 0);
        else
            return 0;

    }

    private void hideKeyboard() {

        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(amountField.getWindowToken(), 0);

    }
}
