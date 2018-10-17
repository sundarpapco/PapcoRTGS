package com.papco.sundar.papcortgs;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.graphics.drawable.GradientDrawable;
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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ReceiverSelectFragment extends Fragment {

    RecyclerView recycler;
    FloatingActionButton fab;
    SearchView searchView;
    TransactionActivityVM viewModel;
    ReceiverAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = ViewModelProviders.of(getActivity()).get(TransactionActivityVM.class);
        viewModel.receivers.observe(this, new Observer<List<Receiver>>() {
            @Override
            public void onChanged(@Nullable List<Receiver> receivers) {
                if (receivers != null)
                    adapter.setData(receivers);
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        ((TransactionActivity)getActivity()).getSupportActionBar().setTitle("Select receiver");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View ui = inflater.inflate(R.layout.senders_list_fragment, container, false);

        recycler = ui.findViewById(R.id.sender_fragment_recycler);
        fab = ui.findViewById(R.id.sender_fragment_fab);
        searchView = ui.findViewById(R.id.sender_fragment_search_view);
        fab.hide();

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
        recycler.addItemDecoration(new DividerDecoration(getActivity(), (GradientDrawable) getResources().getDrawable(R.drawable.divider)));

        if(adapter==null)
            adapter = new ReceiverAdapter(new ArrayList<Receiver>());
        recycler.setAdapter(adapter);
        return ui;
    }


    // RecyclerView item click and long click callbacks which will be called from the viewholder
    private void recyclerItemClicked(View view, Receiver receiver, int adapterPosition) {

        viewModel.selectedReceiver.setValue(receiver);
        ((TransactionActivity)getActivity()).popBackStack();

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
            }
        }
    }
}
