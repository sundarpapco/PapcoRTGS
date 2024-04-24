package com.papco.sundar.papcortgs.screens.transactionGroup;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.papco.sundar.papcortgs.R;
import com.papco.sundar.papcortgs.common.DividerDecoration;
import com.papco.sundar.papcortgs.database.transactionGroup.TransactionGroupListItem;
import com.papco.sundar.papcortgs.screens.password.PasswordDialog;
import com.papco.sundar.papcortgs.screens.sms.ActivityComposeMessage;
import com.papco.sundar.papcortgs.screens.transactionGroup.manage.ManageTransactionGroupDialog;

import java.util.ArrayList;
import java.util.List;

public class GroupListFragment extends Fragment {

    GroupActivityVM viewModel;
    RecyclerView recycler;
    FloatingActionButton fab;
    GroupAdapter adapter = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(requireActivity()).get(GroupActivityVM.class);
        setHasOptionsMenu(true);

    }

    @Override
    public void onStart() {
        super.onStart();
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null)
            actionBar.setTitle("RTGS XL Files");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_senders:
                askForPassword(PasswordDialog.CODE_SENDERS);
                break;

            case R.id.action_receivers:
                //showReceiversActivity();
                askForPassword(PasswordDialog.CODE_RECEIVERS);
                break;

            case R.id.action_message_format:
                showMessageFormatActivity();
                break;

            case R.id.action_dropbox:
                ((GroupActivity) requireActivity()).showDropBoxActivity();
                break;
        }

        return true;
    }

    private void showMessageFormatActivity() {

        Intent intent = new Intent(getActivity(), ActivityComposeMessage.class);
        startActivity(intent);

    }

    private void askForPassword(int code) {

        PasswordDialog passwordDialog = new PasswordDialog();
        FragmentManager manager = requireActivity().getSupportFragmentManager();
        passwordDialog.setRequestCode(code);
        passwordDialog.show(manager, "passwordDialog");
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_group_list, container, false);
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
        //must to avoid leaking memory
        recycler.setAdapter(null);
    }

    private void initViews(View view) {

        recycler = view.findViewById(R.id.transaction_recycler);
        fab = view.findViewById(R.id.transaction_fab);

        fab.setImageResource(R.drawable.ic_xl_sheet);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*viewModel.editingGroup = null; //Indicating rhe dialog that new item to create
                ((GroupActivity) requireActivity()).showCreateEditDialog();*/
                showManageTransactionDialog();
            }
        });

        //Initialize Recycler
        if (adapter == null)
            adapter = new GroupAdapter(new ArrayList<TransactionGroupListItem>());
        recycler.setLayoutManager(new LinearLayoutManager(requireActivity()));
        recycler.addItemDecoration(new DividerDecoration(requireActivity()));
        recycler.setAdapter(adapter);

    }

    private void observeViewModel() {

        viewModel.groups.observe(getViewLifecycleOwner(), new Observer<List<TransactionGroupListItem>>() {
            @Override
            public void onChanged(@Nullable List<TransactionGroupListItem> transactionGroups) {

                if (transactionGroups == null)
                    return;

                if (adapter != null)
                    adapter.setData(transactionGroups);
            }
        });

    }

    private void recyclerItemLongClicked(final TransactionGroupListItem group) {
        showEditTransactionDialog(group.transactionGroup.id);
    }

    private void recyclerItemClicked(TransactionGroupListItem group) {

        ((GroupActivity) requireActivity()).showTransactionsActivity(group.transactionGroup);
    }

    private void showManageTransactionDialog() {

        new ManageTransactionGroupDialog().show(
                getChildFragmentManager(),
                ManageTransactionGroupDialog.TAG
        );
    }

    private void showEditTransactionDialog(int groupId) {

        ManageTransactionGroupDialog.Companion.getEditModeInstance(groupId).show(
                getChildFragmentManager(),
                ManageTransactionGroupDialog.TAG
        );

    }


    class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupVH> {

        List<TransactionGroupListItem> data;

        GroupAdapter(List<TransactionGroupListItem> data) {
            this.data = data;
            setHasStableIds(true);
        }


        @NonNull
        @Override
        public GroupVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new GroupVH(getLayoutInflater().inflate(R.layout.excel_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull GroupVH holder, int position) {

            TransactionGroupListItem listItem = data.get(holder.getAdapterPosition());
            holder.groupName.setText(listItem.transactionGroup.name);

            if (listItem.sender == null) {
                holder.defaultSender.setText(" ");
                holder.defaultSender.setVisibility(View.GONE);
            } else {
                holder.defaultSender.setText(listItem.sender.displayName);
                holder.defaultSender.setVisibility(View.VISIBLE);
            }


        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        @Override
        public long getItemId(int position) {
            return data.get(position).transactionGroup.id;
        }

        public void setData(List<TransactionGroupListItem> data) {
            this.data = data;
            notifyDataSetChanged();
        }

        class GroupVH extends RecyclerView.ViewHolder {

            TextView groupName;
            TextView defaultSender;

            public GroupVH(View itemView) {
                super(itemView);

                groupName = itemView.findViewById(R.id.excel_list_item_name);
                defaultSender = itemView.findViewById(R.id.default_sender);

                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        recyclerItemLongClicked(data.get(getAdapterPosition()));
                        return true;
                    }
                });

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        recyclerItemClicked(data.get(getAdapterPosition()));
                    }
                });
            }
        }
    }


}
