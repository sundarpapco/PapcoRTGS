package com.papco.sundar.papcortgs.screens.transaction.listTransaction;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.papco.sundar.papcortgs.R;
import com.papco.sundar.papcortgs.common.DatePickerFragment;
import com.papco.sundar.papcortgs.common.DividerDecoration;
import com.papco.sundar.papcortgs.database.transaction.Transaction;
import com.papco.sundar.papcortgs.database.transaction.TransactionForList;
import com.papco.sundar.papcortgs.screens.mail.EmailService;
import com.papco.sundar.papcortgs.screens.sms.SmsService;
import com.papco.sundar.papcortgs.screens.transaction.common.TransactionActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TransactionListFragment extends Fragment implements DatePickerFragment.OnDatePickedListener {

    public static final String KEY_GROUP_ID = "key_group_id";
    public static final String KEY_GROUP_NAME = "key_group_name";

    public static TransactionListFragment getInstance(int groupId, String groupName) {

        Bundle bundle = new Bundle();
        bundle.putInt(KEY_GROUP_ID, groupId);
        bundle.putString(KEY_GROUP_NAME, groupName);

        TransactionListFragment fragment = new TransactionListFragment();
        fragment.setArguments(bundle);
        return fragment;
    }


    RecyclerView recycler;
    FloatingActionButton fab;
    TransactionAdapter adapter = null;
    TransactionListVM viewModel;
    CoordinatorLayout rootView;
    int transactionListCount = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TransactionListVM.class);
        adapter = new TransactionAdapter(new ArrayList<TransactionForList>());
        setHasOptionsMenu(true);

    }

    @Override
    public void onStart() {
        super.onStart();
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null)
            actionBar.setTitle(getGroupName());
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.file_export, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_export) {

            if (transactionListCount > 0)
                ((TransactionActivity) requireActivity()).exportFile();
            else
                Toast.makeText(getActivity(), "Please add atleast one transaction to export", Toast.LENGTH_SHORT).show();
            return true;
        }

        if (item.getItemId() == R.id.action_export_auto) {

            showDatePickerDialog();
        }

        if (item.getItemId() == R.id.action_sms_all) {

            if (SmsService.IS_SERVICE_RUNNING) { //if the sms service is already running, dont allow user to open the activity again

                Toast.makeText(getActivity(), "Already sending SMS in progress. Please tap on notification", Toast.LENGTH_LONG).show();
                return true;

            }

            if (transactionListCount > 0)
                ((TransactionActivity) requireActivity()).showSMSActivity();
            else
                Toast.makeText(getActivity(), "Please add atleast one transaction to send SMS", Toast.LENGTH_SHORT).show();
            return true;

        }

        if (item.getItemId() == R.id.action_email_all) {

            if (EmailService.isRunning()) { //if the sms service is already running, don't allow user to open the activity again

                Toast.makeText(getActivity(), "Already sending Mails in progress. Please tap on notification", Toast.LENGTH_LONG).show();
                return true;

            }

            if (transactionListCount > 0)
                ((TransactionActivity) requireActivity()).showEmailActivity();
            else
                Toast.makeText(getActivity(), "Please add atleast one transaction to send Email", Toast.LENGTH_SHORT).show();
            return true;

        }

        return false;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.transaction_list_fragment, container, false);
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
        if (recycler != null)
            recycler.setAdapter(null);
    }

    private void initViews(View view) {

        rootView = view.findViewById(R.id.main_layout);
        recycler = view.findViewById(R.id.transaction_recycler);
        fab = view.findViewById(R.id.transaction_fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((TransactionActivity) requireActivity()).showAddTransactionFragment(-1);
            }
        });

        //Recycler view initialization
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recycler.addItemDecoration(new DividerDecoration(requireActivity(), getResources().getColor(R.color.selectionGrey)));
        recycler.setAdapter(adapter);

    }


    private void observeViewModel() {

        viewModel.getTransactions(getGroupId()).observe(getViewLifecycleOwner(), new Observer<List<TransactionForList>>() {
            @Override
            public void onChanged(@Nullable List<TransactionForList> transactionForLists) {

                int tot = 0;

                if (transactionForLists == null)
                    return;

                transactionListCount = transactionForLists.size();
                for (TransactionForList t : transactionForLists) {
                    tot = tot + t.amount;
                }

                adapter.setTotal(tot);
                adapter.setData(transactionForLists);
            }
        });

    }


    private int getGroupId() {

        if (getArguments() != null)
            return getArguments().getInt(KEY_GROUP_ID);
        else
            return -1;
    }

    private String getGroupName() {
        if (getArguments() != null)
            return getArguments().getString(KEY_GROUP_NAME);
        else
            return "";
    }

    private void recyclerViewClicked(TransactionForList transactionForList) {

        ((TransactionActivity) requireActivity()).showAddTransactionFragment(transactionForList.id);

    }

    private void recyclerItemLongClicked(View view, final TransactionForList transactionForList) {

        PopupMenu popup = new PopupMenu(requireContext(), view);
        popup.getMenuInflater().inflate(R.menu.menu_delete, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                if (item.getItemId() == R.id.action_delete) {
                    showAlertDialogForTransactionDeletion(transactionForList.id);
                }

                return false;
            }
        });
        popup.show();
    }

    private void showAlertDialogForTransactionDeletion(final int id) {

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setMessage("Are you sure you want to delete this transaction?");
        builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                viewModel.deleteTransaction(id);
            }
        });
        builder.setNegativeButton("CANCEL", null);
        builder.create().show();

    }

    public void showDatePickerDialog() {

        DatePickerFragment.Companion.getInstance().show(
                getChildFragmentManager(),
                DatePickerFragment.TAG
        );
    }

    public void shareFile(final String filename) {

        String fullPath;
        if (TextUtils.isEmpty(filename)) {
            fullPath = "File creation failed for some reason";
        } else {
            fullPath = "File created \n" + filename;
        }

        Snackbar snackbar = Snackbar.make(rootView, fullPath, Snackbar.LENGTH_LONG);

        if (!TextUtils.isEmpty(filename)) {

            snackbar.setAction("SHARE", new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    File sd = requireContext().getCacheDir();
                    File filelocation = new File(sd, filename);
                    Uri path = FileProvider.getUriForFile(requireContext(), "com.papco.sundar.papcortgs.fileprovider", filelocation);
                    Intent emailIntent = new Intent(Intent.ACTION_SEND);
                    //emailIntent.setDataAndType(path,"file/*");
                    //emailIntent.setType("vnd.android.cursor.dir/email");
                    emailIntent.setType("file/*");
                    emailIntent.putExtra(Intent.EXTRA_STREAM, path);
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "RTGS file from PAPCO");
                    startActivity(Intent.createChooser(emailIntent, "Share XL file via..."));

                }
            });
        }

        snackbar.show();

    }



    @Override
    public void onDatePicked(long selectedDayId) {

        if (transactionListCount > 0)
            ((TransactionActivity) requireActivity()).autoExportFile(selectedDayId);
        else
            Toast.makeText(getActivity(), "Please add atleast one transaction to export", Toast.LENGTH_SHORT).show();

    }

    class TransactionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public static final int VIEW_TYPE_TOTAL = 1;
        public static final int VIEW_TYPE_TRANSACTION = 2;

        List<TransactionForList> data;
        int total = 0;

        TransactionAdapter(List<TransactionForList> data) {
            this.data = data;
            setHasStableIds(true);
        }

        @Override
        public long getItemId(int position) {

            if (position == 0)
                return -1;
            else
                return data.get(position - 1).id;
        }

        @Override
        public int getItemViewType(int position) {

            if (position == 0)
                return VIEW_TYPE_TOTAL;
            else
                return VIEW_TYPE_TRANSACTION;

        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            if (viewType == VIEW_TYPE_TOTAL)
                return new TotalVH(getLayoutInflater().inflate(R.layout.list_item_transaction_total_heading, parent, false));
            else
                return new TransactionVH(getLayoutInflater().inflate(R.layout.transaction_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

            if (holder instanceof TotalVH)
                ((TotalVH) holder).total.setText(Transaction.formatAmountAsString(total));
            else {
                ((TransactionVH) holder).from.setText(data.get(holder.getAdapterPosition() - 1).sender);
                ((TransactionVH) holder).to.setText(data.get(holder.getAdapterPosition() - 1).receiver);
                ((TransactionVH) holder).amount.setText(data.get(holder.getAdapterPosition() - 1).getAmountAsString());
            }
        }

        @Override
        public int getItemCount() {
            return data.size() + 1;
        }

        public void setData(List<TransactionForList> data) {
            this.data = data;
            notifyDataSetChanged();
        }

        public void setTotal(int total) {

            this.total = total;
            notifyItemChanged(0);
        }

        class TransactionVH extends RecyclerView.ViewHolder {

            TextView from, to, amount;

            public TransactionVH(View itemView) {
                super(itemView);
                from = itemView.findViewById(R.id.transaction_from);
                to = itemView.findViewById(R.id.transaction_to);
                amount = itemView.findViewById(R.id.transaction_amount);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        recyclerViewClicked(data.get(getAdapterPosition() - 1));
                    }
                });

                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        recyclerItemLongClicked(view, data.get(getAdapterPosition() - 1));
                        return true;
                    }
                });

            }
        }

        class TotalVH extends RecyclerView.ViewHolder {

            TextView total;

            public TotalVH(View itemView) {
                super(itemView);
                total = itemView.findViewById(R.id.transaction_list_total);
            }
        }
    }


}
