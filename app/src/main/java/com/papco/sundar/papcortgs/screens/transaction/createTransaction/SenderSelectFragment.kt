package com.papco.sundar.papcortgs.screens.transaction.createTransaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.papco.sundar.papcortgs.database.pojo.Party
import com.papco.sundar.papcortgs.ui.screens.party.sender.SelectSenderScreen
import com.papco.sundar.papcortgs.ui.theme.RTGSTheme

class SenderSelectFragment : Fragment() {

    companion object {
        const val KEY_SENDER_SELECTION = "key_sender_selection"
    }

    private val viewModel: SenderSelectionVM by lazy {
        ViewModelProvider(this)[SenderSelectionVM::class.java]
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
       return ComposeView(requireContext()).apply {
           setContent {
               RTGSTheme {
                   SelectSenderScreen(
                       state = viewModel.screenState,
                       onSenderClicked = {selectSenderAndClose(it)},
                       onBackPressed = {findNavController().popBackStack()}
                   )
               }
           }
       }
    }

    private fun selectSenderAndClose(sender: Party) {
        findNavController().previousBackStackEntry?.savedStateHandle?.set(KEY_SENDER_SELECTION, sender.id)
        findNavController().popBackStack()
    }

}
