package com.papco.sundar.papcortgs.screens.transactionGroup

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.common.DividerDecoration
import com.papco.sundar.papcortgs.common.ResultDialogFragment
import com.papco.sundar.papcortgs.database.transactionGroup.TransactionGroupListItem
import com.papco.sundar.papcortgs.databinding.FragmentGroupListBinding
import com.papco.sundar.papcortgs.extentions.disableBackArrow
import com.papco.sundar.papcortgs.extentions.updateSubTitle
import com.papco.sundar.papcortgs.extentions.updateTitle
import com.papco.sundar.papcortgs.screens.password.PasswordDialog
import com.papco.sundar.papcortgs.screens.transaction.listTransaction.TransactionListFragment
import com.papco.sundar.papcortgs.screens.transactionGroup.manage.ManageTransactionGroupDialog
import com.papco.sundar.papcortgs.screens.transactionGroup.manage.ManageTransactionGroupDialog.Companion.getEditModeInstance

class GroupListFragment : Fragment(), ResultDialogFragment.ResultDialogListener {


    companion object {
        const val PASSWORD_DIALOG_SENDERS = 1
        const val PASSWORD_DIALOG_RECEIVERS = 2
    }

    private val viewModel: GroupActivityVM by lazy {
        ViewModelProvider(this)[GroupActivityVM::class.java]
    }

    private val adapter: GroupAdapter by lazy{
        GroupAdapter(ArrayList())
    }

    private var _viewBinding: FragmentGroupListBinding? = null
    private val viewBinding: FragmentGroupListBinding
        get() = _viewBinding!!


    private fun showMessageFormatActivity() {
       findNavController().navigate(R.id.action_groupListFragment_to_composeMessageFragment)
    }

    private fun askForPassword(code: Int) {
        PasswordDialog.getInstance(code).show(
                childFragmentManager, PasswordDialog.TAG
            )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _viewBinding = FragmentGroupListBinding.inflate(layoutInflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        updateTitle("RTGS XL Files")
        updateSubTitle("")
        disableBackArrow()
        registerOptionsMenu()
        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //must to avoid leaking memory
        viewBinding.transactionRecycler.adapter = null
        _viewBinding = null
    }

    private fun initViews() {

        viewBinding.transactionFab.setImageResource(R.drawable.ic_xl_sheet)
        viewBinding.transactionFab.setOnClickListener {
            showManageTransactionDialog()
        }

        with(viewBinding.transactionRecycler) {
            setLayoutManager(LinearLayoutManager(requireActivity()))
            addItemDecoration(DividerDecoration(requireActivity()))
            adapter=this@GroupListFragment.adapter
        }
    }

    private fun observeViewModel() {

        viewModel.groups.observe(viewLifecycleOwner) {
            it?.let {
                adapter.setData(it)
            }

        }
    }

    private fun recyclerItemLongClicked(group: TransactionGroupListItem) {
        showEditTransactionDialog(group.transactionGroup.id)
    }

    private fun recyclerItemClicked(group: TransactionGroupListItem) {
        val args=TransactionListFragment.getArgumentBundle(group.transactionGroup)
        findNavController().navigate(
            R.id.action_groupListFragment_to_transactionListFragment,
            args
        )
    }

    private fun showManageTransactionDialog() {
        ManageTransactionGroupDialog().show(
            getChildFragmentManager(), ManageTransactionGroupDialog.TAG
        )
    }

    private fun showEditTransactionDialog(groupId: Int) {
        getEditModeInstance(groupId).show(
            getChildFragmentManager(), ManageTransactionGroupDialog.TAG
        )
    }

    private fun registerOptionsMenu() {

        val menuProvider = object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.main_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.action_senders -> askForPassword(PASSWORD_DIALOG_SENDERS)
                    R.id.action_receivers ->                 //showReceiversActivity();
                        askForPassword(PASSWORD_DIALOG_RECEIVERS)

                    R.id.action_message_format -> showMessageFormatActivity()
                    R.id.action_dropbox -> {
                       findNavController().navigate(R.id.action_groupListFragment_to_dropBoxFragment)
                    }
                }
                return true
            }
        }

        requireActivity().addMenuProvider(menuProvider, viewLifecycleOwner)

    }

    override fun onDialogResult(dialogResult: Any, code: Int) {
        //This will be called when the user enters the correct password and click OK
        Log.d("SAATVIK","Password dialog ended with code $code")
        if (code == PASSWORD_DIALOG_RECEIVERS) navigateToReceiversListFragment()

        if (code == PASSWORD_DIALOG_SENDERS) navigateToSendersListFragment()
    }

    private fun navigateToSendersListFragment() {
        findNavController().navigate(
            R.id.action_groupListFragment_to_sendersListFragment
        )
    }

    private fun navigateToReceiversListFragment() {
        findNavController().navigate(
            R.id.action_groupListFragment_to_receiverListFragment
        )
    }

    private inner class GroupAdapter(private var data: List<TransactionGroupListItem>) :
        RecyclerView.Adapter<GroupAdapter.GroupVH>() {
        init {
            setHasStableIds(true)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupVH {
            return GroupVH(getLayoutInflater().inflate(R.layout.excel_list_item, parent, false))
        }

        override fun onBindViewHolder(holder: GroupVH, position: Int) {
            val listItem = data[holder.bindingAdapterPosition]
            holder.groupName.text = listItem.transactionGroup.name
            if (listItem.sender == null) {
                holder.defaultSender.text = " "
                holder.defaultSender.visibility = View.GONE
            } else {
                holder.defaultSender.text = listItem.sender.displayName
                holder.defaultSender.visibility = View.VISIBLE
            }
        }

        override fun getItemCount(): Int {
            return data.size
        }

        override fun getItemId(position: Int): Long {
            return data[position].transactionGroup.id.toLong()
        }

        fun setData(data: List<TransactionGroupListItem>) {
            this.data = data
            notifyDataSetChanged()
        }

        inner class GroupVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var groupName: TextView
            var defaultSender: TextView

            init {
                groupName = itemView.findViewById(R.id.excel_list_item_name)
                defaultSender = itemView.findViewById(R.id.default_sender)
                itemView.setOnLongClickListener {
                    recyclerItemLongClicked(data[bindingAdapterPosition])
                    true
                }
                itemView.setOnClickListener { recyclerItemClicked(data[bindingAdapterPosition]) }
            }
        }
    }
}
