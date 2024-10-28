package com.papco.sundar.papcortgs.screens.receiver

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
import com.papco.sundar.papcortgs.screens.sender.CreateSenderFragment
import com.papco.sundar.papcortgs.ui.screens.party.receiver.ManageReceiversScreen
import com.papco.sundar.papcortgs.ui.theme.RTGSTheme

class ReceiverListFragment : Fragment() {

    private val viewModel: ReceiverListVM by lazy {
        ViewModelProvider(this)[ReceiverListVM::class.java]
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        getActionBar()?.hide()
        return ComposeView(requireContext()).apply {
            setContent {
                RTGSTheme {
                    ManageReceiversScreen(state = viewModel.screenState,
                        onReceiverClicked = { navigateToEditReceiverFragment(it.id) },
                        onBackPressed = { findNavController().popBackStack() },
                        onAddNewReceiver = { navigateToCreateReceiverFragment() },
                        onDeleteReceiver = {viewModel.deleteReceiver(it)}
                    )
                }
            }
        }
    }

    private fun navigateToCreateReceiverFragment() {
        val args = CreateReceiverFragment.getArgumentBundle(-1)
        findNavController().navigate(
            R.id.action_receiverListFragment_to_createReceiverFragment, args
        )
    }

    private fun navigateToEditReceiverFragment(id:Int){
        val args = CreateReceiverFragment.getArgumentBundle(id)
        findNavController().navigate(
            R.id.action_receiverListFragment_to_createReceiverFragment, args
        )
    }
}
