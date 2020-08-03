package com.papco.sundar.papcortgs.screens.transaction.createTransaction;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import android.widget.Toast;

import com.papco.sundar.papcortgs.R;
import com.papco.sundar.papcortgs.common.DividerDecoration;
import com.papco.sundar.papcortgs.common.TextFunctions;
import com.papco.sundar.papcortgs.database.receiver.Receiver;
import com.papco.sundar.papcortgs.screens.transaction.common.TransactionActivity;
import com.papco.sundar.papcortgs.screens.transaction.common.TransactionActivityVM;

import java.util.ArrayList;
import java.util.List;

public class ReceiverSelectFragment extends Fragment {


    public static final String KEY_GROUP_ID = "key_group_id";

    public static ReceiverSelectFragment getInstance(int groupId) {

        Bundle bundle = new Bundle();
        bundle.putInt(KEY_GROUP_ID, groupId);
        ReceiverSelectFragment fragment = new ReceiverSelectFragment();
        fragment.setArguments(bundle);
        return fragment;
    }


    private RecyclerView recycler;
    private FloatingActionButton fab;
    private SearchView searchView;
    private ReceiverSelectionVM viewModel;
    private ReceiverAdapter adapter;
    private boolean isInitialLoad = true;


    @Override
    public void onStart() {
        super.onStart();
        ((TransactionActivity) getActivity()).getSupportActionBar().setTitle("Select receiver");
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

        if (adapter == null)
            adapter = new ReceiverAdapter(new ArrayList<Receiver>());
        recycler.setAdapter(adapter);
        return ui;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (viewModel != null)
            return;

        viewModel = ViewModelProviders.of(getActivity()).get(TransactionActivityVM.class).
                getReceiverSelectionVM(isInitialLoad, getGroupId());
        viewModel.getReceivers().observe(this, new Observer<List<Receiver>>() {
            @Override
            public void onChanged(@Nullable List<Receiver> receivers) {
                if (receivers != null)
                    adapter.setData(receivers);
            }
        });
    }

    private int getGroupId() {
        return getArguments().getInt(KEY_GROUP_ID);
    }

    // RecyclerView item click and long click callbacks which will be called from the viewholder
    private void recyclerItemClicked(View view, Receiver receiver, int adapterPosition) {

        viewModel.selectReceiver(receiver);
        ((TransactionActivity) getActivity()).popBackStack();

    }


    // Adapter class for the recyclerview
    class ReceiverAdapter extends RecyclerView.Adapter<ReceiverAdapter.TagViewHolder> implements Filterable {


        private List<Receiver> unfilteredData;
        private List<Receiver> data;
        @ColorInt int black=ContextCompat.getColor(requireContext(),android.R.color.black);
        @ColorInt int grey=ContextCompat.getColor(requireContext(),android.R.color.darker_gray);

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

            Receiver receiver=data.get(holder.getAdapterPosition());
            if (!TextUtils.isEmpty(searchView.getQuery()))
                holder.txtViewName.setText(receiver.highlightedName);
            else
                holder.txtViewName.setText(receiver.name);

            if(receiver.accountNumber.equals("-1"))
                holder.txtViewName.setTextColor(grey);
            else
                holder.txtViewName.setTextColor(black);
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
                        if (!data.get(getAdapterPosition()).accountNumber.equals("-1"))
                            recyclerItemClicked(view, data.get(getAdapterPosition()), getAdapterPosition());
                        else
                            Toast.makeText(requireContext(), "This beneficiary has already been added", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }
}
