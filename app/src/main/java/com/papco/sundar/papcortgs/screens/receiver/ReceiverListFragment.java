package com.papco.sundar.papcortgs.screens.receiver;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.papco.sundar.papcortgs.R;
import com.papco.sundar.papcortgs.common.DividerDecoration;
import com.papco.sundar.papcortgs.common.TextFunctions;
import com.papco.sundar.papcortgs.database.receiver.Receiver;

import java.util.ArrayList;
import java.util.List;

public class ReceiverListFragment extends Fragment {

    RecyclerView recycler;
    FloatingActionButton fab;
    SearchView searchView;
    ReceiverActivityVM viewModel;
    ReceiverAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = ViewModelProviders.of(getActivity()).get(ReceiverActivityVM.class);
        viewModel.receivers.observe(this, new Observer<List<Receiver>>() {
            @Override
            public void onChanged(@Nullable List<Receiver> senders) {
                if (senders != null) {
                    adapter.setData(senders);
                    setSubtitle(senders.size());
                }
            }
        });

        setHasOptionsMenu(true);


    }

    @Override
    public void onStart() {
        super.onStart();
        ((ReceiverActivity)getActivity()).getSupportActionBar().setTitle("Manage receivers");
        setSubtitle(adapter.getItemCount());
    }

    @Override
    public void onStop() {
        super.onStop();
        ((ReceiverActivity)getActivity()).getSupportActionBar().setSubtitle("");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View ui = inflater.inflate(R.layout.senders_list_fragment, container, false);

        recycler = ui.findViewById(R.id.sender_fragment_recycler);
        fab = ui.findViewById(R.id.sender_fragment_fab);
        searchView = ui.findViewById(R.id.sender_fragment_search_view);
        fab.setImageResource(R.drawable.ic_receiver);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                viewModel.editingReceiver = null; //Indicating rhe dialog that new item to create
                ((ReceiverActivity) getActivity()).showAddReceiverFragment();
            }
        });

        searchView.setQueryHint("Search receivers");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });



        recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        recycler.addItemDecoration(new DividerDecoration(getActivity()));
        if(adapter==null)
            adapter=new ReceiverAdapter(new ArrayList<Receiver>());
        recycler.setAdapter(adapter);

        return ui;
    }


    private void setSubtitle(int count){

        String subtitle;
        if(count>0)
            subtitle=Integer.toString(count)+ " Receivers";
        else
            subtitle="";

        ((ReceiverActivity)getActivity()).getSupportActionBar().setSubtitle(subtitle);


    }

    private void deleteReceiver(final Receiver delReceiver) {

        //deleteReceiver here
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle("Are you sure want to delete?");
        builder.setMessage("All transactions sent to this receiver will be deleted!");
        builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                viewModel.deleteReceiver(delReceiver);
            }
        });
        builder.setNegativeButton("CANCEL",null);
        builder.create().show();
    }


    // RecyclerView item click and long click callbacks which will be called from the viewholder
    private void recyclerItemClicked(View view, Receiver sender, int adapterPosition) {

        viewModel.editingReceiver = sender;
        ((ReceiverActivity) getActivity()).showAddReceiverFragment();

    }

    private void recyclerItemLongClicked(View view, final Receiver sender) {

        PopupMenu popup=new PopupMenu(getActivity(),view);
        popup.getMenuInflater().inflate(R.menu.menu_delete,popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                if(item.getItemId()==R.id.action_delete){
                    deleteReceiver(sender);
                    return true;
                }

                return false;
            }
        });
        popup.show();

    }


    // Adapter class for the recyclerview
    class ReceiverAdapter extends RecyclerView.Adapter<ReceiverAdapter.TagViewHolder> implements Filterable {


        private List<Receiver> unfilteredData;
        private List<Receiver> data;

        public ReceiverAdapter(List<Receiver> data) {
            unfilteredData = data;
            this.data = unfilteredData;
            setHasStableIds(true);
        }

        @NonNull
        @Override
        public ReceiverAdapter.TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ReceiverAdapter.TagViewHolder(getLayoutInflater().inflate(R.layout.list_item_tag, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ReceiverAdapter.TagViewHolder holder, int position) {
            if(!TextUtils.isEmpty(searchView.getQuery()))
                holder.txtViewName.setText(data.get(holder.getAdapterPosition()).highlightedName);
            else
                holder.txtViewName.setText(data.get(holder.getAdapterPosition()).name);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        @Override
        public long getItemId(int position) {
            return data.get(position).id;
        }

        public void setData(List<Receiver> senders) {

            this.unfilteredData = senders;
            getFilter().filter(searchView.getQuery());
        }

        @Override
        public Filter getFilter() {

            return new Filter() {

                List<Receiver> filteredList = new ArrayList<>();

                @Override
                protected FilterResults performFiltering(CharSequence charSequence) {

                    String stringToSearch = charSequence.toString().toLowerCase();
                    if (stringToSearch.isEmpty()) {
                        filteredList = unfilteredData;
                    } else {

                        for (Receiver sender : unfilteredData) {
                            if (sender.name.toLowerCase().contains(stringToSearch)) {
                                sender.highlightedName= TextFunctions.getHighlitedString(sender.name,stringToSearch,Color.YELLOW);
                                filteredList.add(sender);
                            }
                        }
                    }
                    FilterResults results = new FilterResults();
                    results.values = filteredList;
                    return results;
                }

                @Override
                protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                    data = (List<Receiver>) filterResults.values;
                    notifyDataSetChanged();
                }
            };

        }

        class TagViewHolder extends RecyclerView.ViewHolder {

            public TextView txtViewName;

            public TagViewHolder(final View itemView) {
                super(itemView);

                txtViewName = itemView.findViewById(R.id.tag_name);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        recyclerItemClicked(view, data.get(getAdapterPosition()), getAdapterPosition());
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
