package com.papco.sundar.papcortgs.screens.receiver;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
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
import com.papco.sundar.papcortgs.database.receiver.Receiver;

import java.util.ArrayList;
import java.util.List;

public class ReceiverListFragment extends Fragment {

    RecyclerView recycler;
    FloatingActionButton fab;
    SearchView searchView;
    ReceiverListVM viewModel;
    ReceiverAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ReceiverListVM.class);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null)
            actionBar.setTitle("Manage receivers");
        setSubtitle(adapter.getItemCount());
    }

    @Override
    public void onStop() {
        super.onStop();
        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null)
            actionBar.setTitle("");
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
        fab = ui.findViewById(R.id.sender_fragment_fab);
        searchView = ui.findViewById(R.id.sender_fragment_search_view);
        fab.setImageResource(R.drawable.ic_receiver);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ReceiverActivity) requireActivity()).showAddReceiverFragment(-1);
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

        //Recycler view
        recycler.setLayoutManager(new LinearLayoutManager(requireActivity()));
        recycler.addItemDecoration(new DividerDecoration(requireActivity()));
        if (adapter == null)
            adapter = new ReceiverAdapter(new ArrayList<Receiver>());
        recycler.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getReceiver().observe(getViewLifecycleOwner(), new Observer<List<Receiver>>() {
            @Override
            public void onChanged(List<Receiver> receivers) {
                if (receivers != null) {
                    adapter.setData(receivers);
                    setSubtitle(receivers.size());
                }
            }
        });
    }


    private void setSubtitle(int count) {

        String subtitle;
        if (count > 0)
            subtitle = count + " Receivers";
        else
            subtitle = "";

        ActionBar actionBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null)
            actionBar.setSubtitle(subtitle);


    }

    private void deleteReceiver(final Receiver delReceiver) {

        //deleteReceiver here
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle("Are you sure want to delete?");
        builder.setMessage("All transactions sent to this receiver will be deleted!");
        builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                viewModel.deleteReceiver(delReceiver);
            }
        });
        builder.setNegativeButton("CANCEL", null);
        builder.create().show();
    }


    // RecyclerView item click and long click callbacks which will be called from the viewHolder
    private void recyclerItemClicked(Receiver receiver) {

        ((ReceiverActivity) requireActivity()).showAddReceiverFragment(receiver.id);

    }

    private void recyclerItemLongClicked(View view, final Receiver sender) {

        PopupMenu popup = new PopupMenu(requireActivity(), view);
        popup.getMenuInflater().inflate(R.menu.menu_delete, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                if (item.getItemId() == R.id.action_delete) {
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
            if (!TextUtils.isEmpty(searchView.getQuery()))
                holder.txtViewName.setText(data.get(holder.getAdapterPosition()).highlightedName);
            else
                holder.txtViewName.setText(data.get(holder.getAdapterPosition()).displayName);
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

                        recyclerItemClicked(data.get(getAdapterPosition()));
                    }
                });
                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {

                        recyclerItemLongClicked(view, data.get(getAdapterPosition()));
                        return true;

                    }
                });
            }
        }
    }
}
