package com.papco.sundar.papcortgs.screens.transaction.createTransaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.papco.sundar.papcortgs.database.pojo.Party
import com.papco.sundar.papcortgs.ui.screens.party.receiver.SelectReceiverScreen
import com.papco.sundar.papcortgs.ui.theme.RTGSTheme
import com.papco.sundar.papcortgs.R

class ReceiverSelectFragment : Fragment() {

    companion object {
        private const val KEY_GROUP_ID = "key_group_id"
        const val KEY_RECEIVER_SELECTION = "key_receiver_selection"


        fun getArgumentBundle(groupId: Int): Bundle {
            return Bundle().also {
                it.putInt(KEY_GROUP_ID, groupId)
            }
        }
    }

    private val viewModel: ReceiverSelectionVM by lazy{
        ViewModelProvider(this)[ReceiverSelectionVM::class.java]
    }

    private val groupId: Int
        get() {
            return arguments?.getInt(KEY_GROUP_ID,-1) ?: -1
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadReceivers(groupId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                RTGSTheme {
                    SelectReceiverScreen(
                        state = viewModel.screenState,
                        onReceiverClicked ={selectReceiverAndGoBack(it)},
                        onBackPressed = {findNavController()}
                    )
                }
            }
        }
    }


    private fun selectReceiverAndGoBack(party:Party) {

        if(party.disabled) {
            Toast.makeText(
                requireContext(),
                getString(R.string.this_beneficiary_already_added),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        findNavController().previousBackStackEntry?.savedStateHandle?.set(KEY_RECEIVER_SELECTION,party.id)
        findNavController().popBackStack()
    }

}
