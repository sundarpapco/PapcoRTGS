package com.papco.sundar.papcortgs.screens.sender

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.extentions.getActionBar
import com.papco.sundar.papcortgs.ui.screens.party.sender.ManageSendersScreen
import com.papco.sundar.papcortgs.ui.theme.RTGSTheme

class SendersListFragment : Fragment() {

    private val viewModel: SendersListVM by lazy {
        ViewModelProvider(this)[SendersListVM::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        getActionBar()?.hide()
       return ComposeView(requireContext()).apply {
           setContent {
               RTGSTheme {
                   ManageSendersScreen(
                       state = viewModel.screenState,
                       onSenderClicked = {navigateToEditSenderScreen(it.id)},
                       onBackPressed = { findNavController().popBackStack() },
                       onAddNewSender = { navigateToCreateSenderScreen() },
                       onDeleteSender = {viewModel.deleteSender(it)})
               }
           }
       }
    }

    private fun navigateToCreateSenderScreen(){
        val args=CreateSenderFragment.getArgumentBundle(-1)
        findNavController().navigate(
            R.id.action_sendersListFragment_to_createSenderFragment,
            args
        )
    }

    private fun navigateToEditSenderScreen(id:Int) {
        val args = CreateSenderFragment.getArgumentBundle(id)
        findNavController().navigate(
            R.id.action_sendersListFragment_to_createSenderFragment, args
        )
    }
}
