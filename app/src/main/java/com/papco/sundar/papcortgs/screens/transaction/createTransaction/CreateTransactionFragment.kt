package com.papco.sundar.papcortgs.screens.transaction.createTransaction

import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.common.ResultDialogFragment
import com.papco.sundar.papcortgs.databinding.CreateTransactionBinding
import com.papco.sundar.papcortgs.extentions.enableBackArrow
import com.papco.sundar.papcortgs.extentions.updateSubTitle
import com.papco.sundar.papcortgs.extentions.updateTitle
import com.papco.sundar.papcortgs.screens.password.PasswordDialog

class CreateTransactionFragment : Fragment(),ResultDialogFragment.ResultDialogListener {

    companion object {

        const val KEY_GROUP_ID = "key_groupId"
        const val KEY_LOAD_TRANSACTION_ID = "key_loadTransactionId"
        private const val KEY_DEFAULT_SENDER_ID = "key_default_sender"
        private const val DIALOG_CODE_SENDERS_PASSWORD = 1
        private const val DIALOG_CODE_RECEIVERS_PASSWORD = 2
        fun getArgumentBundle(
            groupId: Int, loadTransactionId: Int, defaultSenderId: Int
        ): Bundle {
            return Bundle().also {
                it.putInt(KEY_GROUP_ID, groupId)
                it.putInt(KEY_LOAD_TRANSACTION_ID, loadTransactionId)
                it.putInt(KEY_DEFAULT_SENDER_ID, defaultSenderId)
            }
        }


    }

    private val groupId: Int
        get() = arguments?.getInt(KEY_GROUP_ID, -1) ?: -1
    private val transactionId: Int
        get() = arguments?.getInt(KEY_LOAD_TRANSACTION_ID, -1) ?: -1
    private val defaultSenderId: Int
        get() = arguments?.getInt(KEY_DEFAULT_SENDER_ID, -1) ?: -1

    private val isEditingTransaction: Boolean
        get() = transactionId != -1

    private val viewModel: CreateTransactionVM by lazy {
        ViewModelProvider(this)[CreateTransactionVM::class.java]
    }

    private var _viewBinding: CreateTransactionBinding? = null
    private val viewBinding: CreateTransactionBinding
        get() = _viewBinding!!


    // region override Methods ---------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isEditingTransaction)
            viewModel.loadTransaction(transactionId)
        else
            viewModel.createBlankTransaction(groupId, defaultSenderId)
    }

    private fun registerOptionsMenu() {
        val menuProvider = object : MenuProvider {

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_done, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == R.id.action_done) {
                    validateAndSave()
                    return true
                }

                if (menuItem.itemId == android.R.id.home) {
                    findNavController().popBackStack()
                    return true
                }

                return false
            }
        }

        requireActivity().addMenuProvider(menuProvider, viewLifecycleOwner)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _viewBinding = CreateTransactionBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        enableBackArrow()
        registerOptionsMenu()
        observeViewModel()
        if (isEditingTransaction) updateTitle("Update Transaction")
        else updateTitle("Create Transaction")

        updateSubTitle("")
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val amountField = viewBinding.createTransactionAmount

        if (!TextUtils.isEmpty(
                amountField.text.toString().trim { it <= ' ' })
        ) viewModel.saveAmount(
            amountField.text.toString().toInt()
        )
        viewModel.saveRemarks(viewBinding.createTransactionRemarks.text.toString())

        _viewBinding = null
    }

    private fun initViews() {

        with(viewBinding) {
            createTransactionFrom.keyListener = null
            createTransactionFrom.setOnClickListener {

                if (viewModel.selectedSender.getValue() == null) {
                    askForPassword(DIALOG_CODE_SENDERS_PASSWORD)
                } else {
                    findNavController().navigate(
                        R.id.action_createTransactionFragment_to_senderSelectFragment
                    )
                }

            }

            createTransactionTo.keyListener = null

            createTransactionTo.setOnClickListener {
                if (viewModel.selectedReceiver.getValue() == null)
                    askForPassword(DIALOG_CODE_RECEIVERS_PASSWORD)
                else{
                    val args=ReceiverSelectFragment.getArgumentBundle(groupId)
                    findNavController().navigate(
                        R.id.action_createTransactionFragment_to_receiverSelectFragment,
                        args
                    )
                }

            }

            createTransactionSenderDots.setOnClickListener {
                askForPassword(DIALOG_CODE_SENDERS_PASSWORD)
            }

            createTransactionReceiverDots.setOnClickListener {
                askForPassword(
                    DIALOG_CODE_RECEIVERS_PASSWORD
                )
            }
        }

    }

    private fun observeViewModel() {

        viewModel.selectedSender.observe(getViewLifecycleOwner()) { sender ->
            if (sender == null) {
                viewBinding.createTransactionFrom.setText("Tap to add a Sender")
            } else {
                //Toast.makeText(requireContext(),"Selecting sender " + sender.name,Toast.LENGTH_SHORT).show();
                viewBinding.createTransactionFrom.setText(sender.displayName)
            }
        }
        viewModel.selectedReceiver.observe(getViewLifecycleOwner()) { receiver ->
            if (receiver == null) viewBinding.createTransactionTo.setText("Tap to add beneficiary") else {
                viewBinding.createTransactionTo.setText(receiver.displayName)
            }
        }
        viewModel.amount.observe(getViewLifecycleOwner()) { integer -> //If this call is due to config change, then ignore this call
            //because user might have edited the amount and we should not
            // reset with initial loading data
            if (integer == null || integer == 0) viewBinding.createTransactionAmount.setText("")
            else viewBinding.createTransactionAmount.setText(integer.toString())
        }
        viewModel.remarks.observe(getViewLifecycleOwner()) { remarks ->
            if (remarks == null) viewBinding.createTransactionRemarks.setText("ON ACCOUNT")
            else viewBinding.createTransactionRemarks.setText(remarks)
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Int>(
            SenderSelectFragment.KEY_SENDER_SELECTION
        )?.observe(viewLifecycleOwner) { selectedSenderId ->

            findNavController().currentBackStackEntry?.savedStateHandle?.remove<Int>(
                SenderSelectFragment.KEY_SENDER_SELECTION
            )

            viewModel.selectSender(selectedSenderId)
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Int>(
            ReceiverSelectFragment.KEY_RECEIVER_SELECTION
        )?.observe(viewLifecycleOwner) { selectedReceiverId ->

            findNavController().currentBackStackEntry?.savedStateHandle?.remove<Int>(
                ReceiverSelectFragment.KEY_RECEIVER_SELECTION
            )

            viewModel.selectReceiver(selectedReceiverId)
        }

    }

    // endregion override Methods ---------------------------------------


    private fun askForPassword(code: Int) {

        PasswordDialog.getInstance(code).show(
                childFragmentManager, PasswordDialog.TAG
            )
    }

    override fun onDialogResult(dialogResult: Any, code: Int) {
        if(code== DIALOG_CODE_RECEIVERS_PASSWORD)
            findNavController().navigate(R.id.action_createTransactionFragment_to_receiverListFragment)

        if(code== DIALOG_CODE_SENDERS_PASSWORD)
            findNavController().navigate(R.id.action_createTransactionFragment_to_sendersListFragment)
    }

    private fun validateAndSave() {

        with(viewBinding) {
            if (viewModel.selectedSender.getValue() == null) {
                Toast.makeText(requireActivity(), "Select a Sender", Toast.LENGTH_SHORT).show()
                return
            }
            if (viewModel.selectedReceiver.getValue() == null) {
                Toast.makeText(requireActivity(), "Select a Receiver", Toast.LENGTH_SHORT).show()
                return
            }
            if (TextUtils.isEmpty(createTransactionAmount.text) || createTransactionAmount.text.toString()
                    .toInt() == 0
            ) {
                Toast.makeText(requireActivity(), "Enter amount", Toast.LENGTH_SHORT).show()
                return
            }
            hideKeyboard()
            viewModel.setAmount(createTransactionAmount.text.toString().toInt())
            viewModel.setRemarks(createTransactionAmount.text.toString())
            if (isEditingTransaction) viewModel.updateTransaction(
                groupId, transactionId
            ) else viewModel.saveNewTransaction(
                groupId
            )

            findNavController().popBackStack()
        }


    }


    private fun hideKeyboard() {
        val imm =
            requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(viewBinding.createTransactionAmount.windowToken, 0)
    }


}
