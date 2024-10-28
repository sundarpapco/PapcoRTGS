package com.papco.sundar.papcortgs.screens.sender

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
import com.papco.sundar.papcortgs.database.sender.Sender
import com.papco.sundar.papcortgs.extentions.getActionBar
import com.papco.sundar.papcortgs.ui.screens.party.AddEditPartyScreen
import com.papco.sundar.papcortgs.ui.theme.RTGSTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CreateSenderFragment : Fragment() {

    companion object {
        private const val KEY_SENDER_ID = "key_editing_sender_id"
        const val EVENT_SUCCESS = "EVENT_SUCCESS"
        fun getArgumentBundle(editingSenderId: Int): Bundle {
            val args = Bundle()
            args.putInt(KEY_SENDER_ID, editingSenderId)
            return args
        }
    }

    private val isEditMode: Boolean
        get() = editingSenderId() != -1

    val viewModel: CreateSenderVM by lazy {
        ViewModelProvider(this)[CreateSenderVM::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getActionBar()?.hide()
        if (isEditMode) viewModel.loadSender(editingSenderId())
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                RTGSTheme {
                    AddEditPartyScreen(
                        title = if (isEditMode)
                            getString(R.string.update_sender)
                    else
                        getString(R.string.create_sender),
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

    override fun onDestroy() {
        super.onDestroy()
        getActionBar()?.show()
    }

    private fun observeViewModel() {

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.eventStatus.flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collectLatest {event->
                    event?.let{
                        if (it.isAlreadyHandled) return@collectLatest
                        val result = it.handleEvent()
                        if (result == EVENT_SUCCESS) findNavController().popBackStack()
                        else Toast.makeText(
                            requireContext(), result, Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

    }

    private fun validateAndSave() {

        val sender = Sender()
        with(viewModel.screenState) {
            sender.displayName = displayName
            sender.name = accountName
            sender.accountNumber = accountNumber
            sender.accountType = accountType
            sender.ifsc = ifsCode
            sender.bank = bankAndBranch
            sender.mobileNumber = mobileNumber
            sender.email = email

        }

        if (!isEditMode) { //add as new sender
            viewModel.addSender(sender)
        } else {
            sender.id = editingSenderId()
            viewModel.updateSender(sender)
        }

    }
    private fun editingSenderId(): Int = arguments?.getInt(KEY_SENDER_ID, -1) ?: -1

}
