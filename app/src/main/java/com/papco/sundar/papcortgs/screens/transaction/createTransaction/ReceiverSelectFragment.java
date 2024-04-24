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
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.papco.sundar.papcortgs.R;
import com.papco.sundar.papcortgs.common.DividerDecoration;
import com.papco.sundar.papcortgs.common.TextFunctions;
import com.papco.sundar.papcortgs.database.receiver.Receiver;
import com.papco.sundar.papcortgs.screens.transaction.common.TransactionActivity;

import java.util.ArrayList;
import java.util.List;

public class ReceiverSelectFragment extends Fragment {


    public static final String KEY_GROUP_ID = "key_group_id";
    public static final String KEY_RECEIVER_SELECTION = "key_receiver_selection";

    public static ReceiverSelectFragment getInstance(int groupId) {

        Bundle bundle = new Bundle();
        bundle.putInt(KEY_GROUP_ID, groupId);
        ReceiverSelectFragment fragment = new ReceiverSelectFragment();
        fragment.setArguments(bundle);
        return fragment;
    }


    private RecyclerView recycler;
    private SearchView searchView;
    private ReceiverSelectionVM viewModel;
    private ReceiverAdapter adapter;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(ReceiverSelectionVM.class);

        if (getGroupId() == -1)
            throw new IllegalStateException("Group ID argument not found");

        viewModel.loadReceivers(getGroupId());

    }

    @Override
    public void onStart() {
        super.onStart();
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null)
            actionBar.setTitle("Select receiver");
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
        //To avoid memory leak
        if (recycler != null)
            recycler.setAdapter(null);
    }

    private void initViews(View ui) {

        recycler = ui.findViewById(R.id.sender_fragment_recycler);
        FloatingActionButton fab = ui.findViewById(R.id.sender_fragment_fab);
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

        //Recycler view initializing
        recycler.setLayoutManager(new LinearLayoutManager(requireActivity()));
        recycler.addItemDecoration(new DividerDecoration(requireActivity()));
        if (adapter == null)
            adapter = new ReceiverAdapter(new ArrayList<Receiver>());
        recycler.setAdapter(adapter);
    }


    private void observeViewModel() {

        viewModel.getReceivers().observe(getViewLifecycleOwner(), new Observer<List<Receiver>>() {
            @Override
            public void onChanged(@Nullable List<Receiver> receivers) {
                if (receivers != null)
                    adapter.setData(receivers);
            }
        });

    }


    // RecyclerView item click and long click callbacks which will be called from the view holder
    private void recyclerItemClicked(Receiver receiver) {

        //Set the result to the fragment manager so that the parent fragment which requested will
        //receive the result once the back stack is popped up
        Bundle result = new Bundle();
        result.putInt(KEY_RECEIVER_SELECTION, receiver.id);
        getParentFragmentManager().setFragmentResult(KEY_RECEIVER_SELECTION, result);

        ((TransactionActivity) requireActivity()).popBackStack();

    }

    private int getGroupId() {
        if (getArguments() != null)
            return getArguments().getInt(KEY_GROUP_ID);
        else
            return -1;
    }


    // Adapter class for the recyclerview
    class ReceiverAdapter extends RecyclerView.Adapter<ReceiverAdapter.TagViewHolder> implements Filterable {


        private List<Receiver> unfilteredData;
        private List<Receiver> data;
        @ColorInt
        int black = ContextCompat.getColor(requireContext(), android.R.color.black);
        @ColorInt
        int grey = ContextCompat.getColor(requireContext(), android.R.color.darker_gray);

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

            Receiver receiver = data.get(holder.getAdapterPosition());
            if (!TextUtils.isEmpty(searchView.getQuery()))
                holder.txtViewName.setText(receiver.highlightedName);
            else
                holder.txtViewName.setText(receiver.displayName);

            if (receiver.accountNumber.equals("-1"))
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
                            if (sender.displayName.toLowerCase().contains(stringToSearch)) {

                                sender.highlightedName = TextFunctions.getHighlightedString(sender.displayName, stringToSearch, Color.YELLOW);
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
                            recyclerItemClicked(data.get(getAdapterPosition()));
                        else
                            Toast.makeText(requireContext(), "This beneficiary has already been added", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }
}
