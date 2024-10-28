package com.papco.sundar.papcortgs.ui.screens.party.receiver

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.database.pojo.Party
import com.papco.sundar.papcortgs.ui.components.RTGSAppBar
import com.papco.sundar.papcortgs.ui.dialogs.WaitDialog
import com.papco.sundar.papcortgs.ui.screens.LoadingScreen
import com.papco.sundar.papcortgs.ui.screens.party.ManagePartyScreenDialogs
import com.papco.sundar.papcortgs.ui.screens.party.ManagePartyScreenState
import com.papco.sundar.papcortgs.ui.screens.party.SearchablePartyList
import com.papco.sundar.papcortgs.ui.theme.RTGSTheme

@Composable
fun ManageReceiversScreen(
    state: ManagePartyScreenState,
    onReceiverClicked: (Party) -> Unit,
    onBackPressed: () -> Unit,
    onAddNewReceiver: () -> Unit,
    onDeleteReceiver: (Party) -> Unit
) {
    Scaffold(topBar = {

        RTGSAppBar(
            title = stringResource(id = R.string.manage_receivers),
            isBackEnabled = true,
            onBackPressed = onBackPressed,
            subtitle = state.data?.let {
                stringResource(id = R.string.xx_receivers, it.size)
            })
    }, floatingActionButton = {
        FloatingActionButton(onClick = onAddNewReceiver) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Receiver")
        }
    }) { paddingValues ->

        if (state.data == null) LoadingScreen()
        else SearchablePartyList(modifier = Modifier.padding(paddingValues),
            state = state.listState,
            onPartyClicked = onReceiverClicked,
            searchHint = stringResource(id = R.string.search_receivers),
            onPartyLongClicked = {
                state.showDeleteConfirmationDialog(it)
            })
    }

    state.dialogState?.let {
        RenderDialog(
            dialog = it,
            onDelete = onDeleteReceiver,
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
                party = dialog.party, onDelete = onDelete, onDismiss = onDismiss
            )
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    party: Party, onDelete: (Party) -> Unit, onDismiss: () -> Unit
) {

    AlertDialog(title = {
        Text(
            text = stringResource(id = R.string.receiver_delete_confirmation_title),
            style = MaterialTheme.typography.titleMedium
        )
    }, text = {
        Text(
            text = stringResource(id = R.string.receiver_delete_confirmation_msg, party.name),
            style = MaterialTheme.typography.titleSmall
        )
    }, onDismissRequest = onDismiss, confirmButton = {
        TextButton(onClick = { onDelete(party) }) {
            Text(text = stringResource(id = R.string.delete))
        }
    }, dismissButton = {
        TextButton(onClick = { onDismiss() }) {
            Text(text = stringResource(id = R.string.cancel))
        }
    })
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
        ManageReceiversScreen(state = state,
            onReceiverClicked = {},
            onBackPressed = {},
            onAddNewReceiver = {},
            onDeleteReceiver = {})
    }

}



