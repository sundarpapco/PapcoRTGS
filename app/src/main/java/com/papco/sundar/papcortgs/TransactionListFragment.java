package com.papco.sundar.papcortgs;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TransactionListFragment extends Fragment {

    RecyclerView recycler;
    FloatingActionButton fab;
    TransactionAdapter adapter=null;
    TransactionActivityVM viewmodel;
    CoordinatorLayout rootView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewmodel=ViewModelProviders.of(getActivity()).get(TransactionActivityVM.class);
        viewmodel.getTransactions().observe(getActivity(), new Observer<List<TransactionForList>>() {
            @Override
            public void onChanged(@Nullable List<TransactionForList> transactionForLists) {

                int tot=0;

                if(transactionForLists==null)
                    return;

                for(TransactionForList t:transactionForLists){
                    tot=tot+t.amount;
                }

                ((TransactionActivity)getActivity()).getSupportActionBar().setSubtitle(Transaction.formatAmountAsString(tot));

                if(adapter!=null)
                    adapter.setData(transactionForLists);
            }
        });
        setHasOptionsMenu(true);

    }

    @Override
    public void onStart() {
        super.onStart();
        ((TransactionActivity)getActivity()).getSupportActionBar().setTitle(viewmodel.currentGroup.name);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.file_export,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId()==R.id.action_export){

            if(viewmodel.getTransactions().getValue().size()>0)
                ((TransactionActivity)getActivity()).exportFile();
            else
                Toast.makeText(getActivity(),"Please add atleast one transaction to export",Toast.LENGTH_SHORT).show();
            return true;
        }

        if(item.getItemId()==R.id.action_sms_all){

            if(viewmodel.getTransactions().getValue().size()>0)
                ((TransactionActivity)getActivity()).showSMSActivity();
            else
                Toast.makeText(getActivity(),"Please add atleast one transaction to send SMS",Toast.LENGTH_SHORT).show();
            return true;

        }

        return false;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View ui=inflater.inflate(R.layout.transaction_list_fragment,container,false);
        rootView=ui.findViewById(R.id.main_layout);
        recycler=ui.findViewById(R.id.transaction_recycler);
        fab=ui.findViewById(R.id.transaction_fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                viewmodel.editingTransactionId=-1;
                viewmodel.editingTransaction.setValue(null);
                viewmodel.selectedSender.setValue(null);
                viewmodel.selectedReceiver.setValue(null);
                ((TransactionActivity)getActivity()).showAddTransactionFragment();
            }
        });

        if(adapter==null)
            adapter= new TransactionAdapter(new ArrayList<TransactionForList>());
        recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        recycler.addItemDecoration(new DividerDecoration(getActivity(),(GradientDrawable)getResources().getDrawable(R.drawable.divider)));
        recycler.setAdapter(adapter);

        return ui;
    }

    private void recyclerViewClicked(TransactionForList transactionForList) {

        viewmodel.editingTransactionId=transactionForList.id;
        viewmodel.editingTransaction.setValue(null);
        ((TransactionActivity)getActivity()).showAddTransactionFragment();

    }

    private void recyclerItemLongClicked(View view, final TransactionForList transactionForList) {

        PopupMenu popup=new PopupMenu(getActivity(),view);
        popup.getMenuInflater().inflate(R.menu.menu_delete,popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                if(item.getItemId()==R.id.action_delete){
                    showAlertDialogForTransactionDeletion(transactionForList.id);
                }

                return false;
            }
        });
        popup.show();
    }

    private void showAlertDialogForTransactionDeletion(final int id){

        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setMessage("Are you sure you want to delete this transaction?");
        builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                viewmodel.deleteTransaction(id);
            }
        });
        builder.setNegativeButton("CANCEL",null);
        builder.create().show();

    }

    public void shareFile(final String filename){

        String fullpath="";
        if(TextUtils.isEmpty(filename)){
            fullpath="File creation failed for some reason";
        }else {
            fullpath = "File created \npapcoRTGS/" + filename;
        }

        Snackbar snackbar=Snackbar.make(rootView,fullpath,Snackbar.LENGTH_LONG);

        if(!TextUtils.isEmpty(filename)) {

            snackbar.setAction("SHARE", new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    File sd = Environment.getExternalStorageDirectory();
                    String appDirectory = sd.getAbsolutePath() + "/papcoRTGS";
                    File filelocation = new File(appDirectory, filename);
                    Uri path = FileProvider.getUriForFile(getActivity(), "com.papco.sundar.papcortgs.fileprovider", filelocation);
                    Intent emailIntent = new Intent(Intent.ACTION_SEND);
                    emailIntent.setType("vnd.android.cursor.dir/email");
                    emailIntent.putExtra(Intent.EXTRA_STREAM, path);
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "RTGS file from PAPCO");
                    startActivity(Intent.createChooser(emailIntent, "Share XL file via..."));

                }
            });
        }

        snackbar.show();

    }

    class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionVH>{

        List<TransactionForList> data;

        TransactionAdapter(List<TransactionForList> data){
            this.data=data;
            setHasStableIds(true);
        }

        @Override
        public long getItemId(int position) {
            return data.get(position).id;
        }

        @NonNull
        @Override
        public TransactionVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new TransactionVH(getLayoutInflater().inflate(R.layout.transaction_list_item,parent,false));
        }

        @Override
        public void onBindViewHolder(@NonNull TransactionVH holder, int position) {
            holder.from.setText(data.get(holder.getAdapterPosition()).sender);
            holder.to.setText(data.get(holder.getAdapterPosition()).receiver);
            holder.amount.setText(data.get(holder.getAdapterPosition()).getAmountAsString());
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public void setData(List<TransactionForList> data){
            this.data=data;
            notifyDataSetChanged();
        }

        class TransactionVH extends RecyclerView.ViewHolder{

            TextView from,to,amount;

            public TransactionVH(View itemView) {
                super(itemView);
                from=itemView.findViewById(R.id.transaction_from);
                to=itemView.findViewById(R.id.transaction_to);
                amount=itemView.findViewById(R.id.transaction_amount);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        recyclerViewClicked(data.get(getAdapterPosition()));
                    }
                });

                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        recyclerItemLongClicked(view,data.get(getAdapterPosition()));
                        return true;
                    }
                });

            }
        }
    }




}