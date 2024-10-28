package com.papco.sundar.papcortgs.screens.receiver

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.database.receiver.Receiver
import com.papco.sundar.papcortgs.extentions.getActionBar
import com.papco.sundar.papcortgs.screens.sender.CreateSenderFragment
import com.papco.sundar.papcortgs.ui.screens.party.AddEditPartyScreen
import com.papco.sundar.papcortgs.ui.theme.RTGSTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CreateReceiverFragment : Fragment() {

    companion object {
        private const val KEY_RECEIVER_ID = "key_for_editing_receiver_id"
        const val EVENT_SUCCESS = "EVENT_SUCCESS"

        fun getArgumentBundle(editingReceiverId: Int): Bundle {
            val args = Bundle()
            args.putInt(KEY_RECEIVER_ID, editingReceiverId)
            return args
        }
    }

    val viewModel: CreateReceiverVM by lazy {
        ViewModelProvider(this)[CreateReceiverVM::class.java]
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isEditingMode) viewModel.loadReceiver(editingReceiverId())
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        getActionBar()?.hide()
        return ComposeView(requireContext()).apply {
            setContent {
                RTGSTheme {
                    AddEditPartyScreen(
                        title = if (isEditingMode)
                            getString(R.string.update_receiver)
                        else
                            getString(R.string.create_receiver),
                        state = viewModel.screenState,
                        onBackPressed = {
                            findNavController().popBackStack()
                        }) {
                        validateAndSave()
                    }
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
            viewModel.eventStatus.flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collectLatest {event->
                    event?.let{
                        if (it.isAlreadyHandled) return@collectLatest
                        val result = it.handleEvent()
                        if (result == CreateSenderFragment.EVENT_SUCCESS) findNavController().popBackStack()
                        else Toast.makeText(
                            requireContext(), result, Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun validateAndSave() {

        val receiver = Receiver()
        with(viewModel.screenState) {
            receiver.displayName = displayName
            receiver.name = accountName
            receiver.accountNumber = accountNumber
            receiver.accountType = accountType
            receiver.ifsc = ifsCode
            receiver.bank = bankAndBranch
            receiver.mobileNumber = mobileNumber
            receiver.email = email
        }

            if (!isEditingMode) { //add as new receiver
                viewModel.addReceiver(receiver)
            } else {
                receiver.id = editingReceiverId()
                viewModel.updateReceiver(receiver)
            }

    }
    private fun editingReceiverId(): Int = arguments?.getInt(KEY_RECEIVER_ID, -1) ?: -1

    private val isEditingMode: Boolean
        get() = editingReceiverId() != -1

}
