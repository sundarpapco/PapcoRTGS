package com.papco.sundar.papcortgs.screens.transaction.createTransaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.ui.screens.transaction.ManageTransactionScreen
import com.papco.sundar.papcortgs.ui.theme.RTGSTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CreateTransactionFragment : Fragment() {

    companion object {

        const val KEY_GROUP_ID = "key_groupId"
        const val KEY_LOAD_TRANSACTION_ID = "key_loadTransactionId"
        private const val KEY_DEFAULT_SENDER_ID = "key_default_sender"
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


    // region override Methods ---------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isEditingTransaction)
            viewModel.loadTransaction(transactionId)
        else
            viewModel.createBlankTransaction(groupId, defaultSenderId)
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
       return ComposeView(requireContext()).apply {
           setContent {
               RTGSTheme {
                   ManageTransactionScreen(
                       screenState = viewModel.screenState,
                       title = getTitle(),
                       onSenderClicked = { navigateToSelectSenderFragment() },
                       onReceiverClicked = { navigateToReceiverSelectFragment() },
                       onSave = { validateAndSave() },
                       onDismiss = {findNavController().popBackStack()}
                   )
               }
           }
       }
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
    }

    private fun observeViewModel() {

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.navigateBack.flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collectLatest {needToGoBack->
                    if(needToGoBack)
                        findNavController().popBackStack()
                }
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


    private fun validateAndSave() {

        if(!viewModel.screenState.validate(requireContext()))
            return

        if (isEditingTransaction)
            viewModel.updateTransaction(groupId, transactionId)
        else
            viewModel.saveNewTransaction(groupId)

        findNavController().popBackStack()
    }

    private fun getTitle():String{
        return  if(isEditingTransaction)
            requireContext().getString(R.string.update_transaction)
        else
            requireContext().getString(R.string.create_transaction)
    }

    private fun navigateToSelectSenderFragment(){
        if(viewModel.screenState.selectedSender==null)
            return

        findNavController().navigate(
            R.id.action_createTransactionFragment_to_senderSelectFragment
        )
    }

    private fun navigateToReceiverSelectFragment(){
        if(viewModel.screenState.selectedReceiver==null)
            return

        val args=ReceiverSelectFragment.getArgumentBundle(groupId)
        findNavController().navigate(
            R.id.action_createTransactionFragment_to_receiverSelectFragment,args
        )
    }
}
