package com.papco.sundar.papcortgs;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class GroupListFragment extends Fragment {

    GroupActivityVM viewmodel;
    RecyclerView recycler;
    FloatingActionButton fab;
    GroupAdapter adapter=null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewmodel=ViewModelProviders.of(getActivity()).get(GroupActivityVM.class);
        viewmodel.groups.observe(this, new Observer<List<TransactionGroup>>() {
            @Override
            public void onChanged(@Nullable List<TransactionGroup> transactionGroups) {

                if(transactionGroups==null)
                    return;

                if(adapter!=null)
                    adapter.setData(transactionGroups);
            }
        });

        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        ((GroupActivity)getActivity()).getSupportActionBar().setTitle("RTGS XL Files");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_senders:
                showSendersActivity();
                break;

            case R.id.action_receivers:
                showReceiversActivity();
                break;

            case R.id.action_dropbox:
                ((GroupActivity)getActivity()).showDropBoxActivity();
                break;
        }

        return true;
    }

    private void showReceiversActivity() {

        Intent intent=new Intent(getActivity(),ReceiverActivity.class);
        startActivity(intent);
    }

    private void showSendersActivity() {

        Intent intent=new Intent(getActivity(),SenderActivity.class);
        startActivity(intent);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View ui=inflater.inflate(R.layout.transaction_list_fragment,container,false);
        recycler=ui.findViewById(R.id.transaction_recycler);
        fab=ui.findViewById(R.id.transaction_fab);

        fab.setImageResource(R.drawable.ic_xl_sheet);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewmodel.editingGroup=null; //Indicating rhe dialog that new item to create
                ((GroupActivity)getActivity()).showCreateEditDialog();
            }
        });

        if(adapter==null)
            adapter= new GroupAdapter(new ArrayList<TransactionGroup>());
        recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        recycler.addItemDecoration(new DividerDecoration(getActivity(),(GradientDrawable)getResources().getDrawable(R.drawable.divider)));
        recycler.setAdapter(adapter);

        return ui;
    }

    private void recyclerItemLongClicked(View view, final TransactionGroup group) {

        viewmodel.editingGroup=group;
        ((GroupActivity)getActivity()).showCreateEditDialog();

    }

    private void recyclerItemClicked(TransactionGroup group) {

        ((GroupActivity)getActivity()).showTransactionsActivity(group);
    }


    class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupVH>{

        List<TransactionGroup> data;

        GroupAdapter(List<TransactionGroup> data){
            this.data=data;
            setHasStableIds(true);
        }


        @NonNull
        @Override
        public GroupVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new GroupVH(getLayoutInflater().inflate(R.layout.excel_list_item,parent,false));
        }

        @Override
        public void onBindViewHolder(@NonNull GroupVH holder, int position) {

            holder.groupName.setText(data.get(holder.getAdapterPosition()).name);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        @Override
        public long getItemId(int position) {
            return data.get(position).id;
        }

        public void setData(List<TransactionGroup> data){
            this.data=data;
            notifyDataSetChanged();
        }

        class GroupVH extends RecyclerView.ViewHolder{

            TextView groupName;

            public GroupVH(View itemView) {
                super(itemView);
                groupName=itemView.findViewById(R.id.excel_list_item_name);
                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        recyclerItemLongClicked(view,data.get(getAdapterPosition()));
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
