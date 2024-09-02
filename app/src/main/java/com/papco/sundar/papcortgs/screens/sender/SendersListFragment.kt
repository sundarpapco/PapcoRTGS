package com.papco.sundar.papcortgs.screens.sender

import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
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

class SendersListFragment : Fragment() {

    private val adapter: SenderAdapter by lazy {
        SenderAdapter(ArrayList())
    }

    private val viewModel by lazy {
        ViewModelProvider(this)[SendersListVM::class.java]
    }

    private var _viewBinding: SendersListFragmentBinding? = null
    private val viewBinding: SendersListFragmentBinding
        get() = _viewBinding!!


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
        updateTitle("Manage Senders")
        updateSubTitle("")
        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding.senderFragmentRecycler.adapter = null
        _viewBinding = null
    }

    private fun initViews() {

        viewBinding.senderFragmentFab.setImageResource(R.drawable.ic_sender)
        viewBinding.senderFragmentFab.setOnClickListener {
           val args=CreateSenderFragment.getArgumentBundle(-1)
            findNavController().navigate(
                R.id.action_sendersListFragment_to_createSenderFragment,
                args
            )
        }
        viewBinding.senderFragmentSearchView.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                adapter.filter.filter(newText)
                return false
            }
        })

        //RecyclerView initialization
        with(viewBinding.senderFragmentRecycler){
            setLayoutManager(LinearLayoutManager(requireActivity()))
            addItemDecoration(DividerDecoration(requireActivity()))
            adapter=this@SendersListFragment.adapter
        }
    }

    private fun observeViewModel() {

        viewModel.sendersList.observe(viewLifecycleOwner){senders->
            if (senders != null) adapter.setData(senders)
        }
    }

    private fun deleteSender(delSender: Sender) {

        //deleteSender here
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle("Are you sure want to delete?")
        builder.setMessage("All transactions sent from this sender will be deleted!")
        builder.setPositiveButton("DELETE") { _, _ ->
            viewModel.deleteSender(
                delSender
            )
        }
        builder.setNegativeButton("CANCEL", null)
        builder.create().show()
    }


    // RecyclerView item click and long click callbacks which will be called from the viewholder
    private fun recyclerItemClicked(sender: Sender) {
        val args=CreateSenderFragment.getArgumentBundle(sender.id)
        findNavController().navigate(
            R.id.action_sendersListFragment_to_createSenderFragment,
            args
        )
    }

    private fun recyclerItemLongClicked(view: View, sender: Sender) {
        val popup = PopupMenu(requireActivity(), view)
        popup.menuInflater.inflate(R.menu.menu_delete, popup.menu)
        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_delete) {
                deleteSender(sender)
                return@OnMenuItemClickListener true
            }
            false
        })
        popup.show()
    }

    // Adapter class for the recyclerview
    private inner class SenderAdapter(private var unfilteredData: List<Sender>) :
        RecyclerView.Adapter<SenderAdapter.TagViewHolder>(), Filterable {

        private var filteredData:List<Sender> =unfilteredData
        init {
            setHasStableIds(true)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
            return TagViewHolder(getLayoutInflater().inflate(R.layout.list_item_tag, parent, false))
        }

        override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
            if (!TextUtils.isEmpty(viewBinding.senderFragmentSearchView.query)) holder.txtViewName.text =
                filteredData[holder.bindingAdapterPosition].highlightedName else holder.txtViewName.text =
                filteredData[holder.bindingAdapterPosition].displayName
        }

        override fun getItemCount(): Int {
            return filteredData.size
        }

        override fun getItemId(position: Int): Long {
            return filteredData[position].id.toLong()
        }

        fun setData(senders: List<Sender>) {
            unfilteredData = senders
            filter.filter((viewBinding.senderFragmentSearchView.query))
        }

        override fun getFilter(): Filter {
            return object : Filter() {
                var filteredList: MutableList<Sender> = ArrayList()
                override fun performFiltering(charSequence: CharSequence): FilterResults {
                    val stringToSearch = charSequence.toString().lowercase(Locale.getDefault())
                    if (stringToSearch.isEmpty()) {
                        Log.d("SAATVIK","No Filtering")
                        filteredList = unfilteredData.toMutableList()
                    } else {
                        for (sender in unfilteredData) {
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
                    filteredData = filterResults.values as List<Sender>
                    notifyDataSetChanged()
                }
            }
        }

        inner class TagViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var txtViewName: TextView

            init {
                txtViewName = itemView.findViewById(R.id.tag_name)
                itemView.setOnClickListener { recyclerItemClicked(filteredData[bindingAdapterPosition]) }
                itemView.setOnLongClickListener { view ->
                    recyclerItemLongClicked(view, filteredData[bindingAdapterPosition])
                    true
                }
            }
        }
    }
}
