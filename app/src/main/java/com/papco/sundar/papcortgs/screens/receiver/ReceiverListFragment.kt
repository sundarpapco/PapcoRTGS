package com.papco.sundar.papcortgs.screens.receiver

import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.common.DividerDecoration
import com.papco.sundar.papcortgs.common.TextFunctions
import com.papco.sundar.papcortgs.database.receiver.Receiver
import com.papco.sundar.papcortgs.databinding.SendersListFragmentBinding
import com.papco.sundar.papcortgs.extentions.enableBackArrow
import com.papco.sundar.papcortgs.extentions.registerBackArrowMenu
import com.papco.sundar.papcortgs.extentions.updateSubTitle
import com.papco.sundar.papcortgs.extentions.updateTitle
import java.util.Locale

class ReceiverListFragment : Fragment() {

    private val viewModel:ReceiverListVM by lazy{
        ViewModelProvider(this)[ReceiverListVM::class.java]
    }

    private val adapter by lazy { ReceiverAdapter(mutableListOf()) }

    private var _viewBinding: SendersListFragmentBinding? = null
    private val viewBinding: SendersListFragmentBinding
        get() = _viewBinding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _viewBinding= SendersListFragmentBinding.inflate(inflater,container,false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        enableBackArrow()
        registerBackArrowMenu()
        observeViewModel()
        updateTitle("Manage Receivers")
        updateSubTitle("${adapter.itemCount} Receivers")

    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding.senderFragmentRecycler.adapter=null
        updateSubTitle("")
    }

    private fun initViews() {


        with(viewBinding){
            senderFragmentFab.setImageResource(R.drawable.ic_receiver)
            senderFragmentFab.setOnClickListener{
               val args=CreateReceiverFragment.getArgumentBundle(-1)
                findNavController().navigate(
                    R.id.action_receiverListFragment_to_createReceiverFragment,
                    args
                )
            }
            senderFragmentSearchView.setQueryHint("Search receivers")
            senderFragmentSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String): Boolean {
                    adapter.filter.filter(newText)
                    return false
                }
            })

            //Recycler view
            senderFragmentRecycler.setLayoutManager(LinearLayoutManager(requireActivity()))
            senderFragmentRecycler.addItemDecoration(DividerDecoration(requireActivity()))
            senderFragmentRecycler.adapter=this@ReceiverListFragment.adapter
        }
    }

    private fun observeViewModel() {

        viewModel.receiver.observe(viewLifecycleOwner){receivers->
            if (receivers != null) {
                adapter.setData(receivers.toMutableList())
                updateSubTitle("${receivers.size} Receivers")
            }
        }
    }

    private fun deleteReceiver(delReceiver: Receiver) {

        //deleteReceiver here
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle("Are you sure want to delete?")
        builder.setMessage("All transactions sent to this receiver will be deleted!")
        builder.setPositiveButton("DELETE") { dialogInterface, i ->
            viewModel.deleteReceiver(
                delReceiver
            )
        }
        builder.setNegativeButton("CANCEL", null)
        builder.create().show()
    }

    // RecyclerView item click and long click callbacks which will be called from the viewHolder
    private fun recyclerItemClicked(receiver: Receiver) {
        val args=CreateReceiverFragment.getArgumentBundle(receiver.id)
        findNavController().navigate(
            R.id.action_receiverListFragment_to_createReceiverFragment,
            args
        )
    }

    private fun recyclerItemLongClicked(view: View, sender: Receiver) {
        val popup = PopupMenu(requireActivity(), view)
        popup.menuInflater.inflate(R.menu.menu_delete, popup.menu)
        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_delete) {
                deleteReceiver(sender)
                return@OnMenuItemClickListener true
            }
            false
        })
        popup.show()
    }

    // Adapter class for the recyclerview
    private inner class ReceiverAdapter(private var unfilteredData: MutableList<Receiver>) :
        RecyclerView.Adapter<ReceiverAdapter.TagViewHolder>(), Filterable {

        init {
            setHasStableIds(true)
        }

        private var filteredData:List<Receiver> = unfilteredData

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

        fun setData(senders: MutableList<Receiver>) {
            unfilteredData = senders
            filter.filter(viewBinding.senderFragmentSearchView.query)
        }

        override fun getFilter(): Filter {
            return object : Filter() {
                var filteredList: MutableList<Receiver> = ArrayList()
                override fun performFiltering(charSequence: CharSequence): FilterResults {
                    val stringToSearch = charSequence.toString().lowercase(Locale.getDefault())
                    if (stringToSearch.isEmpty()) {
                        filteredList = unfilteredData
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
                    filteredData = (filterResults.values as List<Receiver>).toMutableList()
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
