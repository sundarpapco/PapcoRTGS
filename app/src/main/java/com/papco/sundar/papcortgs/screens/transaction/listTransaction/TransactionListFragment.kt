package com.papco.sundar.papcortgs.screens.transaction.listTransaction

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.common.dayId
import com.papco.sundar.papcortgs.common.getCalendarInstance
import com.papco.sundar.papcortgs.database.transactionGroup.TransactionGroup
import com.papco.sundar.papcortgs.screens.mail.FragmentGoogleSignIn
import com.papco.sundar.papcortgs.screens.sms.SMSFragment
import com.papco.sundar.papcortgs.screens.transaction.createTransaction.CreateTransactionFragment
import com.papco.sundar.papcortgs.ui.screens.transaction.TransactionListScreen
import com.papco.sundar.papcortgs.ui.screens.transaction.TransactionListScreenState
import com.papco.sundar.papcortgs.ui.theme.RTGSTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

class TransactionListFragment : Fragment() {

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


    private val viewModel: TransactionListVM by lazy {
        ViewModelProvider(this)[TransactionListVM::class.java]
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadTransactions(transactionGroup.id)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {

            setContent {
                RTGSTheme {
                    TransactionListScreen(title = transactionGroup.name,
                        screenState = viewModel.screenState,
                        onBackPressed = { findNavController().popBackStack() },
                        onClick = { navigateToEditTransactionFragment(it.id) },
                        onAddTransaction = { navigateToAddTransactionFragment() },
                        onDelete = { viewModel.deleteTransaction(it) },
                        onExportManualRTGSFile = {
                            viewModel.createManualExportFile(transactionGroup, it)
                        },
                        onExportAutoRTGSFile = {
                            onDateSelected(it)
                        },
                        onDispatchMessages = { navigateToSMSFragment() },
                        onDispatchMails = { navigateToGMailSignInFragment() },
                        onShareFile = { shareFile(it) })
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
            viewModel.reportGenerated.flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collectLatest {
                    it?.let { event ->
                        if (event.isAlreadyHandled) return@collectLatest

                        val filename = event.handleEvent()
                        if (filename.isEmpty()) Toast.makeText(
                            requireContext(),
                            getString(R.string.error_in_creating_the_excel_file),
                            Toast.LENGTH_LONG
                        ).show()
                        else viewModel.screenState.dialog =
                            TransactionListScreenState.Dialog.ReportGenerated(filename)
                    }
                }
        }
    }


    private fun navigateToSMSFragment() {
        val args = SMSFragment.getArgumentBundle(transactionGroup.id, transactionGroup.name)
        findNavController().navigate(
            R.id.action_transactionListFragment_to_SMSFragment, args
        )
    }

    private fun navigateToGMailSignInFragment() {
        val args = FragmentGoogleSignIn.getArgumentBundle(transactionGroup)
        findNavController().navigate(
            R.id.action_transactionListFragment_to_fragmentGoogleSignIn, args
        )
    }

    private fun navigateToAddTransactionFragment() {

        val arg = CreateTransactionFragment.getArgumentBundle(
            transactionGroup.id, -1, transactionGroup.defaultSenderId
        )
        findNavController().navigate(
            R.id.action_transactionListFragment_to_createTransactionFragment, arg
        )
    }

    private fun navigateToEditTransactionFragment(transactionId: Int) {
        val arg = CreateTransactionFragment.getArgumentBundle(
            transactionGroup.id, transactionId, transactionGroup.defaultSenderId
        )
        findNavController().navigate(
            R.id.action_transactionListFragment_to_createTransactionFragment, arg
        )
    }

    private fun shareFile(filename: String) {

        val sd = requireContext().cacheDir
        val fileLocation = File(sd, filename)
        val path = FileProvider.getUriForFile(
            requireContext(), getString(R.string.file_provider_authority), fileLocation
        )
        val emailIntent = Intent(Intent.ACTION_SEND)
        //emailIntent.setDataAndType(path,"file/*");
        //emailIntent.setType("vnd.android.cursor.dir/email");
        emailIntent.setType("file/*")
        emailIntent.putExtra(Intent.EXTRA_STREAM, path)
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject_line))
        startActivity(
            Intent.createChooser(
                emailIntent,
                getString(R.string.share_report_chooser_heading)
            )
        )

    }

    private fun onDateSelected(selectedDayId: Long) {
        if (viewModel.screenState.transactions.isNotEmpty()) viewModel.createAutoXlFile(
            transactionGroup,
            selectedDayId
        )
        else Toast.makeText(
            activity, getString(R.string.add_at_least_one_transaction_to_export), Toast.LENGTH_SHORT
        ).show()
    }
}
