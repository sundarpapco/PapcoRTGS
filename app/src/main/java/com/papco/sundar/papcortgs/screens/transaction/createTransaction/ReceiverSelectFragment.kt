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
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
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

class ReceiverSelectFragment() : Fragment() {

    companion object {
        private const val KEY_GROUP_ID = "key_group_id"
        const val KEY_RECEIVER_SELECTION = "key_receiver_selection"


        fun getArgumentBundle(groupId: Int): Bundle {
            return Bundle().also {
                it.putInt(KEY_GROUP_ID, groupId)
            }
        }
    }

    private val viewModel: ReceiverSelectionVM by lazy{
        ViewModelProvider(this)[ReceiverSelectionVM::class.java]
    }

    private val adapter: ReceiverAdapter by lazy{
        ReceiverAdapter(ArrayList())
    }

    private var _viewBinding:SendersListFragmentBinding?=null
    private val viewBinding:SendersListFragmentBinding
        get() = _viewBinding!!

    private val groupId: Int
        get() {
            return arguments?.getInt(KEY_GROUP_ID,-1) ?: -1
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (groupId == -1) throw IllegalStateException("Group ID argument not found")
        viewModel.loadReceivers(groupId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
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
        updateTitle("Select Receiver")
    }

    override fun onDestroyView() {
        super.onDestroyView()

        viewBinding.senderFragmentRecycler.adapter=null
        _viewBinding=null
    }

    private fun initViews() {

        with(viewBinding){
             senderFragmentFab.hide()
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

            //Recycler view initializing
            senderFragmentRecycler.setLayoutManager(LinearLayoutManager(requireActivity()))
            senderFragmentRecycler.addItemDecoration(DividerDecoration(requireActivity()))
            senderFragmentRecycler.setAdapter(adapter)
        }

    }

    private fun observeViewModel() {

        viewModel.receivers.observe(viewLifecycleOwner){receivers->
            receivers?.let{
                adapter.setData(it)
            }
        }
    }

    // RecyclerView item click and long click callbacks which will be called from the view holder
    private fun recyclerItemClicked(receiver: Receiver) {

        findNavController().previousBackStackEntry?.savedStateHandle?.set(KEY_RECEIVER_SELECTION,receiver.id)
        findNavController().popBackStack()
    }



    // Adapter class for the recyclerview
    internal inner class ReceiverAdapter(private var unfilteredData: MutableList<Receiver>) :
        RecyclerView.Adapter<ReceiverAdapter.TagViewHolder?>(), Filterable {

        @ColorInt
        var black: Int = ContextCompat.getColor(requireContext(), android.R.color.black)

        @ColorInt
        var grey: Int = ContextCompat.getColor(requireContext(), android.R.color.darker_gray)

        private var filteredData:List<Receiver> =unfilteredData
        init {
            setHasStableIds(true)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
            return TagViewHolder(getLayoutInflater().inflate(R.layout.list_item_tag, parent, false))
        }

        override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
            val receiver = filteredData[holder.bindingAdapterPosition]
            if (!TextUtils.isEmpty(viewBinding.senderFragmentSearchView.query))
                holder.txtViewName.text = receiver.highlightedName
            else
                holder.txtViewName.text = receiver.displayName

            if ((receiver.accountNumber == "-1"))
                holder.txtViewName.setTextColor(grey)
            else
                holder.txtViewName.setTextColor(black)
        }

        override fun getItemId(position: Int): Long {
            return filteredData[position].id.toLong()
        }

        override fun getItemCount(): Int {
           return filteredData.size
        }

        fun setData(senders: List<Receiver>) {
            unfilteredData = senders.toMutableList()
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
                        for (sender: Receiver in unfilteredData) {
                            if (sender.displayName.lowercase(Locale.getDefault())
                                    .contains(stringToSearch)
                            ) {
                                sender.highlightedName = TextFunctions.getHighlightedString(
                                    sender.displayName,
                                    stringToSearch,
                                    Color.YELLOW
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
                    charSequence: CharSequence,
                    filterResults: FilterResults
                ) {
                    filteredData = (filterResults.values as List<Receiver>).toMutableList()
                    notifyDataSetChanged()
                }
            }
        }

        internal inner class TagViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var txtViewName: TextView

            init {
                txtViewName = itemView.findViewById(R.id.tag_name)
                itemView.setOnClickListener {
                    if (filteredData[bindingAdapterPosition].accountNumber != "-1")
                        recyclerItemClicked(filteredData[bindingAdapterPosition]
                    ) else Toast.makeText(
                        requireContext(),
                        "This beneficiary has already been added",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }


}
