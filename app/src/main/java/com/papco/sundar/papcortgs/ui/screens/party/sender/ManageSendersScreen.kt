package com.papco.sundar.papcortgs.ui.screens.party.sender

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.database.pojo.Party
import com.papco.sundar.papcortgs.ui.components.RTGSAppBar
import com.papco.sundar.papcortgs.ui.dialogs.DeleteConfirmationDialog
import com.papco.sundar.papcortgs.ui.dialogs.WaitDialog
import com.papco.sundar.papcortgs.ui.screens.LoadingScreen
import com.papco.sundar.papcortgs.ui.screens.party.ManagePartyScreenDialogs
import com.papco.sundar.papcortgs.ui.screens.party.ManagePartyScreenState
import com.papco.sundar.papcortgs.ui.screens.party.SearchablePartyList
import com.papco.sundar.papcortgs.ui.theme.RTGSTheme

@Composable
fun ManageSendersScreen(
    state: ManagePartyScreenState,
    onSenderClicked: (Party) -> Unit,
    onBackPressed: () -> Unit,
    onAddNewSender: () -> Unit,
    onDeleteSender: (Party) -> Unit
) {
    Scaffold(topBar = {

        RTGSAppBar(
            title = stringResource(id = R.string.manage_senders),
            isBackEnabled = true,
            onBackPressed = onBackPressed,
            subtitle = state.data?.let {
                stringResource(id = R.string.xx_senders, it.size)
            })
    }, floatingActionButton = {
        FloatingActionButton(onClick = onAddNewSender) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Receiver")
        }
    }) { paddingValues ->

        if (state.data == null)
            LoadingScreen()
        else
            SearchablePartyList(modifier = Modifier.padding(paddingValues),
            state = state.listState,
            onPartyClicked = onSenderClicked,
            searchHint = stringResource(id = R.string.search_senders),
            onPartyLongClicked = {
                state.showDeleteConfirmationDialog(it)
            })
    }

    state.dialogState?.let {
        RenderDialog(
            dialog = it,
            onDelete = onDeleteSender,
            onDismiss = { state.dismissDialog() })
    }

}

@Composable
private fun RenderDialog(
    dialog: ManagePartyScreenDialogs, onDelete: (Party) -> Unit, onDismiss: () -> Unit
) {

    when (dialog) {
        is ManagePartyScreenDialogs.WaitDialog -> {
            WaitDialog()
        }

        is ManagePartyScreenDialogs.DeletePartyDialog -> {

            DeleteConfirmationDialog(
                title = stringResource(id = R.string.sender_delete_confirmation_title),
                message = stringResource(id = R.string.sender_delete_confirmation_msg, dialog.party.name),
                onDelete = { onDelete(dialog.party) },
                onDismiss=onDismiss
            )
        }
    }
}


@Preview
@Composable
private fun PreviewManageReceiversScreen() {

    val state = remember {

        ManagePartyScreenState().apply {
            val list = listOf(
                Party(1, "Sundaravel", ""),
                Party(2, "Madhana", ""),
                Party(3, "Rithanya", ""),
                Party(4, "Saatvik", "")
            )
            loadData(list)
        }
    }

    RTGSTheme {
        ManageSendersScreen(state = state,
            onSenderClicked = {},
            onBackPressed = {},
            onAddNewSender = {},
            onDeleteSender = {})
    }

}



