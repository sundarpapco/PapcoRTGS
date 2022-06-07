package com.papco.sundar.papcortgs.screens.transaction.createTransaction;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.papco.sundar.papcortgs.R;
import com.papco.sundar.papcortgs.common.DividerDecoration;
import com.papco.sundar.papcortgs.common.TextFunctions;
import com.papco.sundar.papcortgs.database.sender.Sender;
import com.papco.sundar.papcortgs.screens.transaction.common.TransactionActivity;

import java.util.ArrayList;
import java.util.List;

public class SenderSelectFragment extends Fragment {

    public static final String KEY_SENDER_SELECTION="key_sender_selection";

    private RecyclerView recycler;
    private SearchView searchView;
    private SenderSelectionVM viewModel;
    private SenderAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SenderSelectionVM.class);
    }

    @Override
    public void onStart() {
        super.onStart();
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null)
            actionBar.setTitle("Select sender");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.senders_list_fragment, container, false);
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

    private void initViews(View ui) {

        recycler = ui.findViewById(R.id.sender_fragment_recycler);
        FloatingActionButton fab = ui.findViewById(R.id.sender_fragment_fab);
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


        recycler.setLayoutManager(new LinearLayoutManager(requireActivity()));
        recycler.addItemDecoration(new DividerDecoration(requireActivity()));
        if (adapter == null)
            adapter = new SenderAdapter(new ArrayList<Sender>());
        recycler.setAdapter(adapter);

    }

    private void observeViewModel() {
        viewModel.getSenders().observe(getViewLifecycleOwner(), new Observer<List<Sender>>() {
            @Override
            public void onChanged(@Nullable List<Sender> senders) {
                if (senders != null)
                    adapter.setData(senders);
            }
        });
    }


    // RecyclerView item click and long click callbacks which will be called from the viewholder
    private void recyclerItemClicked(Sender sender) {

        Bundle result = new Bundle();
        result.putInt(KEY_SENDER_SELECTION, sender.id);
        getParentFragmentManager().setFragmentResult(KEY_SENDER_SELECTION, result);

        ((TransactionActivity) requireActivity()).popBackStack();

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
                                sender.highlightedName = TextFunctions.getHighlightedString(sender.name, stringToSearch, Color.YELLOW);
                                filteredList.add(sender);
                            }
                        }
                    }
                    FilterResults results = new FilterResults();
                    results.values = filteredList;
                    return results;
                }

                @Override
                @SuppressWarnings("unchecked")
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

                        recyclerItemClicked(data.get(getAdapterPosition()));
                    }
                });
            }
        }
    }

}
