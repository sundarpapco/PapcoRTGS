package com.papco.sundar.papcortgs.screens.transaction.createTransaction

import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.common.DividerDecoration
import com.papco.sundar.papcortgs.common.TextFunctions
import com.papco.sundar.papcortgs.database.sender.Sender
import com.papco.sundar.papcortgs.databinding.SendersListFragmentBinding
import com.papco.sundar.papcortgs.extentions.enableBackArrow
import com.papco.sundar.papcortgs.extentions.registerBackArrowMenu
import com.papco.sundar.papcortgs.extentions.updateSubTitle
import com.papco.sundar.papcortgs.extentions.updateTitle
import java.util.Locale

class SenderSelectFragment() : Fragment() {

    companion object {
        const val KEY_SENDER_SELECTION = "key_sender_selection"
    }

    private val viewModel: SenderSelectionVM by lazy {
        ViewModelProvider(this)[SenderSelectionVM::class.java]
    }

    private var _viewBinding: SendersListFragmentBinding? = null
    private val viewBinding: SendersListFragmentBinding
        get() = _viewBinding!!

    private val adapter: SenderAdapter by lazy {
        SenderAdapter(ArrayList())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _viewBinding = SendersListFragmentBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        enableBackArrow()
        registerBackArrowMenu()
        observeViewModel()
        updateTitle("Select Sender")
        updateSubTitle("")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding.senderFragmentRecycler.adapter = null
        _viewBinding = null
    }

    private fun initViews() {

        with(viewBinding) {
            senderFragmentFab.hide()
            senderFragmentSearchView.setOnQueryTextListener(object :
                SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    adapter.filter.filter(newText)
                    return false
                }
            })
            senderFragmentRecycler.setLayoutManager(LinearLayoutManager(requireActivity()))
            senderFragmentRecycler.addItemDecoration(DividerDecoration(requireActivity()))
            senderFragmentRecycler.setAdapter(adapter)
        }

    }

    private fun observeViewModel() {

        viewModel.senders.observe(viewLifecycleOwner) { senders ->
            senders?.let {
                adapter.setData(senders)
            }
        }
    }

    // RecyclerView item click and long click callbacks which will be called from the viewholder
    private fun recyclerItemClicked(sender: Sender) {

        findNavController().previousBackStackEntry?.savedStateHandle?.set(
            KEY_SENDER_SELECTION,
            sender.id
        )
        findNavController().popBackStack()
    }

    // Adapter class for the recyclerview
    internal inner class SenderAdapter(private var unfilteredData: MutableList<Sender>) :
        RecyclerView.Adapter<SenderAdapter.TagViewHolder>(), Filterable {
        init {
            setHasStableIds(true)
        }

        private var filteredData:List<Sender> =unfilteredData

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
            return TagViewHolder(getLayoutInflater().inflate(R.layout.list_item_tag, parent, false))
        }

        override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
            if (!TextUtils.isEmpty(viewBinding.senderFragmentSearchView.query)) holder.txtViewName.text =
                filteredData.get(holder.bindingAdapterPosition).highlightedName else holder.txtViewName.text =
                filteredData.get(holder.bindingAdapterPosition).displayName
        }

        override fun getItemCount(): Int {
            return filteredData.size
        }

        override fun getItemId(position: Int): Long {
            return filteredData[position].id.toLong()
        }

        fun setData(senders: List<Sender>) {
            unfilteredData = senders.toMutableList()
            filter.filter(viewBinding.senderFragmentSearchView.query)
        }

        override fun getFilter(): Filter {
            return object : Filter() {
                var filteredList: MutableList<Sender> = ArrayList()
                override fun performFiltering(charSequence: CharSequence): FilterResults {
                    val stringToSearch = charSequence.toString().lowercase(Locale.getDefault())
                    if (stringToSearch.isEmpty()) {
                        filteredList = unfilteredData
                    } else {
                        for (sender: Sender in unfilteredData) {
                            if (sender.displayName.lowercase(Locale.getDefault())
                                    .contains(stringToSearch)
                            ) {
                                sender.highlightedName = TextFunctions.getHighlightedString(
                                    sender.displayName, stringToSearch, Color.YELLOW
                                )
                                filteredList.add(sender)
                            }
                        }
                    }
                    val results = FilterResults()
                    results.values = filteredList
                    return results
                }

                override fun publishResults(
                    charSequence: CharSequence, filterResults: FilterResults
                ) {
                    filteredData = (filterResults.values as List<Sender>).toMutableList()
                    notifyDataSetChanged()
                }
            }
        }

        internal inner class TagViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var txtViewName: TextView

            init {
                txtViewName = itemView.findViewById(R.id.tag_name)
                itemView.setOnClickListener {
                    recyclerItemClicked(
                        filteredData[bindingAdapterPosition]
                    )
                }
            }
        }
    }


}
