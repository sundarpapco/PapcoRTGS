package com.papco.sundar.papcortgs.screens.transaction.createTransaction;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.papco.sundar.papcortgs.R;
import com.papco.sundar.papcortgs.common.DividerDecoration;
import com.papco.sundar.papcortgs.common.TextFunctions;
import com.papco.sundar.papcortgs.database.sender.Sender;
import com.papco.sundar.papcortgs.screens.transaction.common.TransactionActivity;
import com.papco.sundar.papcortgs.screens.transaction.common.TransactionActivityVM;

import java.util.ArrayList;
import java.util.List;

public class SenderSelectFragment extends Fragment {


    private RecyclerView recycler;
    private FloatingActionButton fab;
    private SearchView searchView;
    private SenderSelectionVM viewModel;
    private SenderAdapter adapter;
    private boolean isInitialLoad = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = ViewModelProviders.of(getActivity()).get(TransactionActivityVM.class).
                getSenderSelectionVM(isInitialLoad);
        viewModel.getSenders().observe(this, new Observer<List<Sender>>() {
            @Override
            public void onChanged(@Nullable List<Sender> senders) {
                if (senders != null)
                    adapter.setData(senders);
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        ((TransactionActivity) getActivity()).getSupportActionBar().setTitle("Select sender");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (savedInstanceState != null)
            isInitialLoad = false;

        View ui = inflater.inflate(R.layout.senders_list_fragment, container, false);

        recycler = ui.findViewById(R.id.sender_fragment_recycler);
        fab = ui.findViewById(R.id.sender_fragment_fab);
        searchView = ui.findViewById(R.id.sender_fragment_search_view);
        fab.hide();

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
        if (adapter == null)
            adapter = new SenderAdapter(new ArrayList<Sender>());
        recycler.setAdapter(adapter);

        return ui;
    }


    // RecyclerView item click and long click callbacks which will be called from the viewholder
    private void recyclerItemClicked(View view, Sender sender, int adapterPosition) {

        viewModel.selectSender(sender);
        ((TransactionActivity) getActivity()).popBackStack();

    }


    // Adapter class for the recyclerview
    class SenderAdapter extends RecyclerView.Adapter<SenderAdapter.TagViewHolder> implements Filterable {


        private List<Sender> unfilteredData;
        private List<Sender> data;

        public SenderAdapter(List<Sender> data) {
            unfilteredData = data;
            this.data = unfilteredData;
            setHasStableIds(true);
        }

        @NonNull
        @Override
        public SenderAdapter.TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new SenderAdapter.TagViewHolder(getLayoutInflater().inflate(R.layout.list_item_tag, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull SenderAdapter.TagViewHolder holder, int position) {
            if (!TextUtils.isEmpty(searchView.getQuery()))
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

        public void setData(List<Sender> senders) {

            this.unfilteredData = senders;
            getFilter().filter(searchView.getQuery());
        }

        @Override
        public Filter getFilter() {

            return new Filter() {

                List<Sender> filteredList = new ArrayList<>();

                @Override
                protected FilterResults performFiltering(CharSequence charSequence) {

                    String stringToSearch = charSequence.toString().toLowerCase();
                    if (stringToSearch.isEmpty()) {
                        filteredList = unfilteredData;
                    } else {

                        for (Sender sender : unfilteredData) {
                            if (sender.name.toLowerCase().contains(stringToSearch)) {
                                sender.highlightedName = TextFunctions.getHighlitedString(sender.name, stringToSearch, Color.YELLOW);
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
                    data = (List<Sender>) filterResults.values;
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
