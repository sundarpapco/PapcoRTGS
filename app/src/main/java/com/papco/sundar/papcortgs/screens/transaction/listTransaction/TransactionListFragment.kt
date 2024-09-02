package com.papco.sundar.papcortgs.screens.transaction.listTransaction

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.FileProvider
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.common.DatePickerFragment
import com.papco.sundar.papcortgs.common.DatePickerFragment.Companion.getInstance
import com.papco.sundar.papcortgs.common.DatePickerFragment.OnDatePickedListener
import com.papco.sundar.papcortgs.common.DividerDecoration
import com.papco.sundar.papcortgs.common.TextInputDialogFragment
import com.papco.sundar.papcortgs.database.transaction.Transaction.Companion.formatAmountAsString
import com.papco.sundar.papcortgs.database.transaction.TransactionForList
import com.papco.sundar.papcortgs.database.transactionGroup.TransactionGroup
import com.papco.sundar.papcortgs.databinding.TransactionListFragmentBinding
import com.papco.sundar.papcortgs.extentions.enableBackArrow
import com.papco.sundar.papcortgs.extentions.updateTitle
import com.papco.sundar.papcortgs.screens.mail.FragmentGoogleSignIn
import com.papco.sundar.papcortgs.screens.sms.SMSFragment
import com.papco.sundar.papcortgs.screens.transaction.createTransaction.CreateTransactionFragment
import java.io.File

class TransactionListFragment : Fragment(), OnDatePickedListener,
    TextInputDialogFragment.TextInputListener {

    companion object {
        const val KEY_GROUP_ID = "groupId"
        const val KEY_GROUP_NAME = "groupName"
        const val KEY_DEFAULT_SENDER_ID = "defaultSenderId"

        fun getArgumentBundle(transactionGroup: TransactionGroup): Bundle {
            val args = Bundle()
            args.apply {
                putInt(KEY_GROUP_ID, transactionGroup.id)
                putString(KEY_GROUP_NAME, transactionGroup.name)
                putInt(KEY_DEFAULT_SENDER_ID, transactionGroup.defaultSenderId)
                return args
            }
        }
    }

    private var _viewBinding: TransactionListFragmentBinding? = null
    private val viewBinding: TransactionListFragmentBinding
        get() = _viewBinding!!

    private val viewModel: TransactionListVM by lazy {
        ViewModelProvider(this)[TransactionListVM::class.java]
    }

    private val adapter: TransactionAdapter by lazy {
        TransactionAdapter(ArrayList())
    }

    private val transactionGroup: TransactionGroup by lazy {
        val group = TransactionGroup()
        arguments?.let {
            group.id = it.getInt(KEY_GROUP_ID)
            group.name = it.getString(KEY_GROUP_NAME) ?: error("Group name argument not set")
            group.defaultSenderId = it.getInt(KEY_DEFAULT_SENDER_ID)
        }
        group
    }

    var transactionListCount = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadTransactions(transactionGroup.id)
    }

    override fun onStart() {
        super.onStart()
        updateTitle(transactionGroup.name)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _viewBinding = TransactionListFragmentBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        enableBackArrow()
        registerOptionsMenu()
        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding.transactionRecycler.adapter = null
        _viewBinding = null
    }

    private fun registerOptionsMenu() {

        val provider = object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.file_export, menu)
            }

            override fun onMenuItemSelected(item: MenuItem): Boolean {

                if(item.itemId==android.R.id.home){
                    findNavController().popBackStack()
                    return true
                }

                if (item.itemId == R.id.action_export) {
                    if (transactionListCount > 0) showChequeNumberInputDialog()
                    return true
                }

                if (item.itemId == R.id.action_export_auto) {
                    showDatePickerDialog()
                }

                if (item.itemId == R.id.action_sms_all) {
                    if (transactionListCount > 0)
                            navigateToSMSFragment()
                    else
                        Toast.makeText(activity,
                        "Please add at least one transaction to send SMS",
                        Toast.LENGTH_SHORT
                    ).show()
                    return true
                }

                if (item.itemId == R.id.action_email_all) {

                    if (transactionListCount > 0)
                           navigateToGMailSignInFragment()
                    else
                        Toast.makeText(
                        activity,
                        "Please add at least one transaction to send Email",
                        Toast.LENGTH_SHORT
                    ).show()
                    return true
                }

                return false
            }

        }

        requireActivity().addMenuProvider(provider, viewLifecycleOwner)

    }

    private fun initViews() {

        viewBinding.transactionFab.setOnClickListener {
            showAddTransactionFragment()
        }

        //Recycler view initialization
        with(viewBinding.transactionRecycler) {
            setLayoutManager(LinearLayoutManager(requireContext()))
            addItemDecoration(
                DividerDecoration(
                    requireActivity(), requireActivity().getColor(R.color.selectionGrey)
                )
            )
            adapter=this@TransactionListFragment.adapter
        }


    }

    private fun observeViewModel() {

        viewModel.transactions.observe(viewLifecycleOwner){list->
            transactionListCount=list.size
            val tot = list.sumOf { it.amount }

            adapter.setTotal(tot)
            adapter.setData(list)
        }

        viewModel.reportGenerated.observe(viewLifecycleOwner){
            if(it.isAlreadyHandled)
                return@observe

            val fileName=it.handleEvent()
            shareFile(fileName)
        }
    }


    private fun recyclerViewClicked(transactionForList: TransactionForList) {
        showEditTransactionFragment(transactionForList.id)
    }

    private fun recyclerItemLongClicked(view: View, transactionForList: TransactionForList) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.menu_delete, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_delete) {
                showAlertDialogForTransactionDeletion(transactionForList.id)
            }
            false
        }
        popup.show()
    }

    private fun showAlertDialogForTransactionDeletion(id: Int) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Are you sure you want to delete this transaction?")
        builder.setPositiveButton("DELETE") { _, _ -> viewModel.deleteTransaction(id) }
        builder.setNegativeButton("CANCEL", null)
        builder.create().show()
    }

    fun showDatePickerDialog() {
        getInstance().show(
            getChildFragmentManager(), DatePickerFragment.TAG
        )
    }

    fun navigateToSMSFragment(){
        val args=SMSFragment.getArgumentBundle(transactionGroup.id,transactionGroup.name)
        findNavController().navigate(
            R.id.action_transactionListFragment_to_SMSFragment,
            args
        )
    }

    fun navigateToGMailSignInFragment(){
        val args=FragmentGoogleSignIn.getArgumentBundle(transactionGroup)
        findNavController().navigate(
            R.id.action_transactionListFragment_to_fragmentGoogleSignIn,
            args
        )
    }

    private fun showAddTransactionFragment(){

        val arg=CreateTransactionFragment.getArgumentBundle(
            transactionGroup.id,-1,transactionGroup.defaultSenderId
        )
        findNavController().navigate(
            R.id.action_transactionListFragment_to_createTransactionFragment,
            arg
        )
    }

    private fun showEditTransactionFragment(transactionId:Int){
        val arg=CreateTransactionFragment.getArgumentBundle(
            transactionGroup.id,transactionId,transactionGroup.defaultSenderId
        )
        findNavController().navigate(
            R.id.action_transactionListFragment_to_createTransactionFragment,
            arg
        )
    }

    private fun shareFile(filename: String) {
        val fullPath: String = if (TextUtils.isEmpty(filename)) {
            "File creation failed for some reason"
        } else {
            "File created \n$filename"
        }

        val snackBar = Snackbar.make(viewBinding.root, fullPath, Snackbar.LENGTH_LONG)
        if (!TextUtils.isEmpty(filename)) {
            snackBar.setAction("SHARE") {
                val sd = requireContext().cacheDir
                val fileLocation = File(sd, filename)
                val path = FileProvider.getUriForFile(
                    requireContext(), "com.papco.sundar.papcortgs.fileprovider", fileLocation
                )
                val emailIntent = Intent(Intent.ACTION_SEND)
                //emailIntent.setDataAndType(path,"file/*");
                //emailIntent.setType("vnd.android.cursor.dir/email");
                emailIntent.setType("file/*")
                emailIntent.putExtra(Intent.EXTRA_STREAM, path)
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "RTGS file from PAPCO")
                startActivity(Intent.createChooser(emailIntent, "Share XL file via..."))
            }
        }
        snackBar.show()
    }

    override fun onDatePicked(selectedDayId: Long) {

        if (transactionListCount > 0)
            viewModel.createAutoXlFile(transactionGroup, selectedDayId)
        else Toast.makeText(
            activity, "Please add at least one transaction to export", Toast.LENGTH_SHORT
        ).show()
    }

    private fun showChequeNumberInputDialog() {
        val builder = TextInputDialogFragment.Builder()
        with(builder) {
            title = "Enter cheque number"
            hint = "Cheque number"
            responseCode = 1
        }

        val textInputDialog = builder.build()

        textInputDialog.show(
            childFragmentManager, TextInputDialogFragment.TAG
        )
    }

    override fun onValidate(enteredText: String): Boolean {
        if (enteredText.trim { it <= ' ' } == "") {
            Toast.makeText(requireContext(), "Enter valid cheque number", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    override fun onTextEntered(text: String, responseCode: Int) {
        viewModel.createManualExportFile(transactionGroup, text)
    }

    inner class TransactionAdapter(private var data: List<TransactionForList>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val viewTypeTotal = 1
        private val viewTypeTransaction = 2
        private var total = 0

        init {
            setHasStableIds(true)
        }

        override fun getItemId(position: Int): Long {
            return if (position == 0) -1 else data[position - 1].id.toLong()
        }

        override fun getItemViewType(position: Int): Int {
            return if (position == 0) viewTypeTotal else viewTypeTransaction
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == viewTypeTotal) TotalVH(
                getLayoutInflater().inflate(
                    R.layout.list_item_transaction_total_heading, parent, false
                )
            ) else TransactionVH(
                getLayoutInflater().inflate(
                    R.layout.transaction_list_item, parent, false
                )
            )
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is TotalVH) holder.total.text = formatAmountAsString(total) else {
                (holder as TransactionVH).from.text = data[holder.bindingAdapterPosition - 1].sender
                holder.to.text = data[holder.bindingAdapterPosition - 1].receiver
                holder.amount.text = data[holder.bindingAdapterPosition - 1].getAmountAsString()
            }
        }

        override fun getItemCount(): Int {
            return data.size + 1
        }

        fun setData(data: List<TransactionForList>) {
            this.data = data
            notifyDataSetChanged()
        }

        fun setTotal(total: Int) {
            this.total = total
            notifyItemChanged(0)
        }

        internal inner class TransactionVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var from: TextView
            var to: TextView
            var amount: TextView

            init {
                from = itemView.findViewById(R.id.transaction_from)
                to = itemView.findViewById(R.id.transaction_to)
                amount = itemView.findViewById(R.id.transaction_amount)
                itemView.setOnClickListener { recyclerViewClicked(data[bindingAdapterPosition - 1]) }
                itemView.setOnLongClickListener { view ->
                    recyclerItemLongClicked(view, data[bindingAdapterPosition - 1])
                    true
                }
            }
        }

        internal inner class TotalVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var total: TextView

            init {
                total = itemView.findViewById(R.id.transaction_list_total)
            }
        }

    }
}
