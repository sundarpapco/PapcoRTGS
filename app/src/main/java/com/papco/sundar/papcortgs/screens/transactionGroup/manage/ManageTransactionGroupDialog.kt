package com.papco.sundar.papcortgs.screens.transactionGroup.manage

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.common.ConfirmationDialog
import com.papco.sundar.papcortgs.common.Event
import com.papco.sundar.papcortgs.common.NoFilterArrayAdapter
import com.papco.sundar.papcortgs.database.sender.Sender
import com.papco.sundar.papcortgs.database.transactionGroup.TransactionGroup
import com.papco.sundar.papcortgs.databinding.DialogManageGroupBinding

class ManageTransactionGroupDialog : DialogFragment(), ConfirmationDialog.ConfirmationDialogListener {

    companion object {

        const val TAG = "tag:Manage:Transaction:Group"
        private const val KEY_SELECTED_SENDER_ID = "selectedSenderId"
        private const val KEY_SELECTED_SENDER_NAME = "selectedSenderName"
        private const val KEY_GROUP_ID = "key_for_group_id"

        fun getEditModeInstance(groupId: Int): ManageTransactionGroupDialog {

            val args = Bundle().apply {
                putInt(KEY_GROUP_ID, groupId)
            }
            return ManageTransactionGroupDialog().also {
                it.arguments = args
            }

        }

    }

    private var _viewBinding:DialogManageGroupBinding?=null
    private val viewBinding:DialogManageGroupBinding
        get() = _viewBinding!!


    private val viewModel: ManageTransactionGroupVM by lazy {
        ViewModelProvider(this)[ManageTransactionGroupVM::class.java]
    }
    private val adapter: NoFilterArrayAdapter<Sender> by lazy {
        NoFilterArrayAdapter(requireContext(), R.layout.spinner_drop_down, R.id.text1, mutableListOf())
    }

    //These values are restored across config changes using saved instance state
    private var selectedSenderId: Int = 0
    private lateinit var selectedSenderName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null)
            restoreSenderDetails(savedInstanceState)
        else {
            selectedSenderId = 0
            selectedSenderName = getString(R.string.add_a_sender_first)
        }

        if (isEditMode())
            viewModel.loadTransactionGroup(getGroupId())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _viewBinding=DialogManageGroupBinding.inflate(layoutInflater,container,false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()

        if (isEditMode())
            updateTextsForEditMode()

        observeViewModel()
        dialog?.setCanceledOnTouchOutside(false)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveSenderDetails(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding=null
    }

    private fun initView() {

        with(viewBinding){

            spinnerSenders.setAdapter(adapter)
            spinnerSenders.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                selectSender(adapter.getItem(position))
            }
            selectSender(null)

            xlFileName.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    enableSaveButtonIfNecessary()
                }
            })

            btnSave.setOnClickListener {
                onButtonSaveClicked()
            }

            btnCancel.setOnClickListener {
                dismiss()
            }

            btnDelete.setOnClickListener {
                showDeleteGroupConfirmationDialog()
            }
        }
        enableSaveButtonIfNecessary()

    }

    private fun observeViewModel() {

        viewModel.senders.observe(viewLifecycleOwner) {
            adapter.addAll(it)
            adapter.notifyDataSetChanged()
            if (selectedSenderId == 0 && it.isNotEmpty() && !isEditMode()) {
                selectSender(it[0])
            }
        }

        viewModel.eventStatus.observe(viewLifecycleOwner) {

            if (it.isAlreadyHandled)
                return@observe

            val result = it.handleEvent()
            if (result == Event.SUCCESS)
                dismiss()
            else
                Toast.makeText(requireContext(), result, Toast.LENGTH_SHORT).show()

        }

        viewModel.transactionGroup.observe(viewLifecycleOwner) {

            if (it.isAlreadyHandled)
                return@observe

            val result = it.handleEvent()
            viewBinding.xlFileName.setText(result.transactionGroup.name)
            selectSender(result.sender)

        }

    }


    private fun restoreSenderDetails(bundle: Bundle) {
        selectedSenderId = bundle.getInt(KEY_SELECTED_SENDER_ID, 0)
        selectedSenderName = bundle.getString(KEY_SELECTED_SENDER_NAME, getString(R.string.add_a_sender_first))

    }

    private fun saveSenderDetails(bundle: Bundle) {
        bundle.putInt(KEY_SELECTED_SENDER_ID, selectedSenderId)
        bundle.putString(KEY_SELECTED_SENDER_NAME, selectedSenderName)
    }

    private fun selectSender(sender: Sender?) {
        if (sender == null) {
            viewBinding.spinnerSenders.setText(selectedSenderName)
        } else {
            viewBinding.spinnerSenders.setText(sender.displayName)
            selectedSenderId = sender.id
            selectedSenderName = sender.displayName
        }

        enableSaveButtonIfNecessary()
    }

    private fun enableSaveButtonIfNecessary() {
        with(viewBinding){
            btnSave.isEnabled = !(xlFileName.text?.isBlank() ?: true || selectedSenderId == 0)
        }
    }

    private fun onButtonSaveClicked() {


        if (selectedSenderId > 0 && viewBinding.xlFileName.text.toString().isNotBlank()) {

            val group = TransactionGroup()
            group.name = viewBinding.xlFileName.text.toString().trim()
            group.defaultSenderId = selectedSenderId

            if (isEditMode()) {
                group.id = getGroupId()
                viewModel.updateGroup(group)
            } else {
                viewModel.addGroup(group)
            }
        }

    }

    private fun updateTextsForEditMode() {

        with(viewBinding){
            btnSave.text = getString(R.string.save)
            title.text = getString(R.string.update_xl_file)
            btnDelete.visibility=View.VISIBLE
        }
    }

    private fun showDeleteGroupConfirmationDialog() {

        ConfirmationDialog.getInstance(
                getString(R.string.delete_group_confirmation),
                getString(R.string.delete),
                1,
        ).show(
                childFragmentManager,
                ConfirmationDialog.TAG
        )

    }

    override fun onConfirmationDialogConfirm(confirmationId: Int, extra: String) {
        viewModel.deleteGroup(getGroupId())
    }

    private fun getGroupId(): Int =
            arguments?.getInt(KEY_GROUP_ID, -1) ?: -1


    private fun isEditMode() = getGroupId() != -1

}