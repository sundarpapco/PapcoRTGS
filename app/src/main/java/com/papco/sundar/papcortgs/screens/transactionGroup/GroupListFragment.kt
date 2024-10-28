package com.papco.sundar.papcortgs.screens.transactionGroup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.database.transactionGroup.TransactionGroupListItem
import com.papco.sundar.papcortgs.extentions.getActionBar
import com.papco.sundar.papcortgs.screens.transaction.listTransaction.TransactionListFragment
import com.papco.sundar.papcortgs.screens.transactionGroup.manage.ManageGroupFragment
import com.papco.sundar.papcortgs.ui.screens.group.ExcelFileListScreen
import com.papco.sundar.papcortgs.ui.theme.RTGSTheme

class GroupListFragment : Fragment() {


    private val viewModel: GroupActivityVM by lazy {
        ViewModelProvider(this)[GroupActivityVM::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        getActionBar()?.hide()
       return ComposeView(requireContext()).apply {

           setContent {
               RTGSTheme {
                   ExcelFileListScreen(state = viewModel.screenState,
                       onExcelFileClicked = { navigateToTransactionListFragment(it)},
                       onExcelFileLongClicked = {navigateToEditGroupFragment(it)},
                       onAddExcelFileClicked = { navigateToCreateGroupFragment() },
                       navigateToSendersScreen = { navigateToSendersListFragment() },
                       navigateToReceiversScreen = { navigateToReceiversListFragment() },
                       navigateToMessageFormatScreen = { showMessageFormatActivity() },
                       navigateToDropBaxBackupScreen = { navigateToDropBoxFragment()})
               }
           }
       }
    }

    private fun navigateToTransactionListFragment(group:TransactionGroupListItem){
        val args=TransactionListFragment.getArgumentBundle(group.transactionGroup)
        findNavController().navigate(
            R.id.action_groupListFragment_to_transactionListFragment,args
        )
    }

    private fun showMessageFormatActivity() {
        findNavController().navigate(R.id.action_groupListFragment_to_composeMessageFragment)
    }

    private fun navigateToDropBoxFragment(){
        findNavController().navigate(
            R.id.action_groupListFragment_to_dropBoxFragment
        )
    }

    private fun navigateToCreateGroupFragment(){
        val args=ManageGroupFragment.getArguments(-1)
        findNavController().navigate(
            R.id.action_groupListFragment_to_manageGroupFragment,args
        )
    }

    private fun navigateToEditGroupFragment(group:TransactionGroupListItem){
        val args=ManageGroupFragment.getArguments(group.transactionGroup.id)
        findNavController().navigate(
            R.id.action_groupListFragment_to_manageGroupFragment,args
        )
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
}
