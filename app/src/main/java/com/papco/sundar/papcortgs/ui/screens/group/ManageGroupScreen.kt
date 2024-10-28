package com.papco.sundar.papcortgs.ui.screens.group

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.database.pojo.Party
import com.papco.sundar.papcortgs.ui.components.RTGSAppBar
import com.papco.sundar.papcortgs.ui.components.TextInputField
import com.papco.sundar.papcortgs.ui.dialogs.DeleteConfirmationDialog
import com.papco.sundar.papcortgs.ui.dialogs.WaitDialog
import com.papco.sundar.papcortgs.ui.theme.RTGSTheme

@Composable
fun ManageGroupScreen(
    title: String,
    state: ManageGroupScreenState,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onBackPressed: () -> Unit,
    onDelete: () -> Unit
) {
    Scaffold(
        topBar = {
            RTGSAppBar(
                title ="",
                isBackEnabled = true,
                onBackPressed = onBackPressed
            )
        },
    ) {
        ScreenContent(
            title = title,
            modifier = Modifier.padding(paddingValues = it),
            state = state,
            onSave = onSave,
            onCancel = onCancel,
            onDelete = { state.showDeleteConfirmationDialog() }
        )
    }

    state.dialog?.let{
        RenderDialog(
            dialogsState = it,
            onDelete = onDelete,
            onDismiss = {state.dialog=null}
        )
    }
}

@Composable
private fun RenderDialog(
    dialogsState:ManageGroupScreenState.Dialog,
    onDelete: () -> Unit,
    onDismiss:()->Unit
){
    when(dialogsState){

        is ManageGroupScreenState.Dialog.WaitDialog ->{
            WaitDialog()
        }

        is ManageGroupScreenState.Dialog.DeleteConfirmation->{
            DeleteConfirmationDialog(
                title = stringResource(id = R.string.delete_xl_file),
                message = stringResource(id = R.string.delete_xl_file_message),
                onDelete = onDelete,
                onDismiss =onDismiss
            )
        }

    }
}

@Composable
private fun ScreenContent(
    title: String,
    state: ManageGroupScreenState,
    modifier: Modifier = Modifier,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        TextInputField(
            modifier = Modifier.fillMaxWidth(),
            text = state.groupName,
            label = stringResource(id = R.string.name)
        ) {
            state.groupName = it
        }

        Spacer(modifier = Modifier.height(24.dp))

        SendersSpinner(selectedSender = state.selectedSender,
            senders = state.sendersList,
            onSenderClicked = { state.selectedSender = it })

        Spacer(modifier = Modifier.height(36.dp))

        Buttons(
            saveEnabled = state.groupName.isNotBlank() && state.selectedSender != null,
            onSave = onSave,
            onCancel = onCancel,
            onDelete = onDelete,
            deletable = state.isEditingMode
        )
    }
}

@Composable
private fun Buttons(
    saveEnabled: Boolean,
    deletable: Boolean = false,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Absolute.Center
    ) {

        if (deletable)
            Box(
            modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                modifier = Modifier.fillMaxWidth(), onClick = onDelete
                ) {
                    Text(text = stringResource(id = R.string.delete))
                }
        }

        Spacer(Modifier.weight(2f))
        Box(
            modifier = Modifier.weight(1f), contentAlignment = Alignment.Center
        ) {
            TextButton(
                modifier = Modifier.fillMaxWidth(), onClick = onCancel
            ) {
                Text(text = stringResource(id = R.string.cancel))
            }
        }

        Spacer(Modifier.width(12.dp))

        Box(
            modifier = Modifier.weight(1f), contentAlignment = Alignment.Center
        ) {
            TextButton(
                modifier = Modifier.fillMaxWidth(), enabled = saveEnabled, onClick = onSave
            ) {
                Text(text = stringResource(id = R.string.save))
            }
        }
    }
}


@Preview
@Composable
private fun PreviewManageGroupScreen() {

    val state = remember {
        ManageGroupScreenState().apply {
            loadSendersList(
                listOf(
                    Party(1, "Papco offset private limited", ""),
                    Party(2, "Papco offset printing works", "")
                )
            )
        }
    }

    RTGSTheme {
        ManageGroupScreen(title = stringResource(id = R.string.create_xl_file),
            state = state,
            onSave = {},
            onCancel = {},
            onBackPressed = {},
            onDelete = {})
    }


}