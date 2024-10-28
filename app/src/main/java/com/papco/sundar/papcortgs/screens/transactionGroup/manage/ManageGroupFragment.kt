package com.papco.sundar.papcortgs.screens.transactionGroup.manage

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
import com.papco.sundar.papcortgs.ui.screens.group.ManageGroupScreen
import com.papco.sundar.papcortgs.ui.theme.RTGSTheme
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.extentions.getActionBar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ManageGroupFragment: Fragment() {

    companion object {

        private const val KEY_GROUP_ID = "key_for_group_id"
        const val EVENT_SUCCESS = "Success"
        fun getArguments(groupId: Int): Bundle {
            return Bundle().apply {
                putInt(KEY_GROUP_ID, groupId)
            }
        }
    }

    private val viewModel: ManageTransactionGroupVM by lazy {
        ViewModelProvider(this)[ManageTransactionGroupVM::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(isEditMode())
            viewModel.loadTransactionGroup(getGroupId())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        getActionBar()?.hide()
        return ComposeView(requireContext()).apply {
            setContent {
                RTGSTheme {
                    ManageGroupScreen(
                        title = if(!isEditMode()) getString(R.string.create_xl_file)
                        else
                            getString(R.string.update_xl_file),
                        state = viewModel.screenState,
                        onSave = {if(isEditMode()) viewModel.updateGroup() else viewModel.addGroup() },
                        onCancel = { findNavController().popBackStack()},
                        onBackPressed = {findNavController().popBackStack()},
                        onDelete = {viewModel.deleteGroup(getGroupId())}
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
    }

    private fun observeViewModel(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.event.flowWithLifecycle(viewLifecycleOwner.lifecycle)
                .collectLatest {event->
                    event?.let{
                        if(it.isAlreadyHandled)
                            return@let

                        val result=it.handleEvent()
                        if(result== EVENT_SUCCESS)
                            findNavController().popBackStack()
                    }
                }
        }
    }

    private fun getGroupId(): Int =
        arguments?.getInt(KEY_GROUP_ID, -1) ?: -1


    private fun isEditMode() = getGroupId() != -1

}