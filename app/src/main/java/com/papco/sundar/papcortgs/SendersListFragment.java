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

public class SendersListFragment extends Fragment {

    final String TAG = "sundar";
    RecyclerView recycler;
    FloatingActionButton fab;
    SearchView searchView;
    SenderActivityVM viewModel;
    SenderAdapter adapter=null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = ViewModelProviders.of(getActivity()).get(SenderActivityVM.class);
        viewModel.senders.observe(this, new Observer<List<Sender>>() {
            @Override
            public void onChanged(@Nullable List<Sender> senders) {
                if (senders != null)
                    adapter.setData(senders);
            }
        });
        setHasOptionsMenu(true);


    }

    @Override
    public void onStart() {
        super.onStart();
        ((SenderActivity)getActivity()).getSupportActionBar().setTitle("Manage senders");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View ui = inflater.inflate(R.layout.senders_list_fragment, container, false);

        recycler = ui.findViewById(R.id.sender_fragment_recycler);
        fab = ui.findViewById(R.id.sender_fragment_fab);
        searchView = ui.findViewById(R.id.sender_fragment_search_view);
        fab.setImageResource(R.drawable.ic_sender);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                viewModel.editingSender = null; //Indicating rhe dialog that new item to create
                ((SenderActivity) getActivity()).showAddSenderFragment();
            }
        });

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
            adapter = new SenderAdapter(new ArrayList<Sender>());
        recycler.setAdapter(adapter);

        return ui;
    }


    private void deleteSender(final Sender delSender) {

        //deleteSender here
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle("Are you sure want to delete?");
        builder.setMessage("All transactions sent from this sender will be deleted!");
        builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                viewModel.deleteSender(delSender);
            }
        });
        builder.setNegativeButton("CANCEL",null);
        builder.create().show();
    }


    // RecyclerView item click and long click callbacks which will be called from the viewholder
    private void recyclerItemClicked(View view, Sender sender, int adapterPosition) {

        viewModel.editingSender = sender;
        ((SenderActivity) getActivity()).showAddSenderFragment();

    }

    private void recyclerItemLongClicked(View view, final Sender sender) {

        PopupMenu popup=new PopupMenu(getActivity(),view);
        popup.getMenuInflater().inflate(R.menu.menu_delete,popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                if(item.getItemId()==R.id.action_delete){
                    deleteSender(sender);
                    return true;
                }

                return false;
            }
        });
        popup.show();

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
