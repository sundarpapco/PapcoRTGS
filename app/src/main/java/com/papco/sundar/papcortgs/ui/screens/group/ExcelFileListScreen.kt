package com.papco.sundar.papcortgs.ui.screens.group

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.database.sender.Sender
import com.papco.sundar.papcortgs.database.transactionGroup.TransactionGroup
import com.papco.sundar.papcortgs.database.transactionGroup.TransactionGroupListItem
import com.papco.sundar.papcortgs.ui.components.MenuAction
import com.papco.sundar.papcortgs.ui.components.OptionsMenu
import com.papco.sundar.papcortgs.ui.components.RTGSAppBar
import com.papco.sundar.papcortgs.ui.dialogs.PasswordDialog
import com.papco.sundar.papcortgs.ui.theme.RTGSTheme

@Composable
fun ExcelFileListScreen(
    state: ExcelFileListScreenState,
    onExcelFileClicked: (TransactionGroupListItem) -> Unit,
    onExcelFileLongClicked: (TransactionGroupListItem) -> Unit,
    onAddExcelFileClicked: () -> Unit,
    navigateToSendersScreen: () -> Unit,
    navigateToReceiversScreen: () -> Unit,
    navigateToMessageFormatScreen: () -> Unit,
    navigateToDropBaxBackupScreen: () -> Unit
) {
    val context = LocalContext.current
    val optionsMenu = remember {
        prepareOptionsMenu(context)
    }

    Scaffold(topBar = {
        RTGSAppBar(
            title = stringResource(id = R.string.excel_files),
            titleStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
        ) {
            OptionsMenu(menuItems = optionsMenu) {
                when (it) {
                    context.getString(R.string.senders) -> {
                        state.dialogState = ExcelFileListScreenState.Dialog.SendersPasswordDialog
                    }

                    context.getString(R.string.receivers) -> {
                        state.dialogState = ExcelFileListScreenState.Dialog.ReceiversPasswordDialog
                    }

                    context.getString(R.string.dropbox_backup) -> {
                        navigateToDropBaxBackupScreen()
                    }

                    context.getString(R.string.message_format) -> {
                        navigateToMessageFormatScreen()
                    }
                }
            }
        }
    }, floatingActionButton = {
        FloatingActionButton(onClick = onAddExcelFileClicked) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Excel File")
        }
    }) {
        ExcelFileList(
            list = state.list,
            onClick = onExcelFileClicked,
            onLongClick = onExcelFileLongClicked,
            modifier = Modifier.padding(it)
        )
    }

    state.dialogState?.let {
        when (it) {

            is ExcelFileListScreenState.Dialog.SendersPasswordDialog -> {
                PasswordDialog(onCorrectPassword = {
                    state.dialogState = null
                    navigateToSendersScreen()
                }, onDismiss = { state.dialogState = null })
            }

            is ExcelFileListScreenState.Dialog.ReceiversPasswordDialog -> {
                PasswordDialog(onCorrectPassword = {
                    state.dialogState = null
                    navigateToReceiversScreen()
                },
                    onDismiss = { state.dialogState = null })
            }
        }
    }
}


@Composable
private fun ExcelFileList(
    list: List<TransactionGroupListItem>,
    onClick: (TransactionGroupListItem) -> Unit,
    onLongClick: (TransactionGroupListItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(list, key = { it.transactionGroup.id }) {
            ExcelFileListItem(group = it,
                onClick = { onClick(it) },
                onLongClick = { onLongClick(it) })
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ExcelFileListItem(
    group: TransactionGroupListItem,
    onClick: (TransactionGroupListItem) -> Unit,
    onLongClick: (TransactionGroupListItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .combinedClickable(onClick = {
                onClick(group)
            },

                onLongClick = {
                    onLongClick(group)
                })
            .padding(8.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            modifier = Modifier.size(50.dp),
            painter = painterResource(id = R.drawable.ic_excel_list),
            contentDescription = ""
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = group.transactionGroup.name, style = MaterialTheme.typography.titleMedium)
            Text(
                text = group.sender.displayName,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun prepareOptionsMenu(context: Context): List<MenuAction> {

    return listOf(
        MenuAction(
            label = context.getString(R.string.senders)
        ), MenuAction(
            label = context.getString(R.string.receivers)
        ), MenuAction(
            label = context.getString(R.string.dropbox_backup)
        )
    )

}

@Preview
@Composable
private fun PreviewExcelListItem() {

    val item = remember {
        TransactionGroupListItem().apply {
            transactionGroup = TransactionGroup().apply {
                name = "Excel File"
                id = 1
                defaultSenderId = 1
            }

            sender = Sender().apply {
                id = 1
                displayName = "POPL"
            }
        }
    }

    RTGSTheme {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            ExcelFileListItem(group = item, onClick = {}, onLongClick = {})
        }
    }

}

@Preview
@Composable
private fun PreviewExcelList() {

    val items = remember {

        listOf(TransactionGroupListItem().apply {
            transactionGroup = TransactionGroup().apply {
                name = "Excel File"
                id = 1
                defaultSenderId = 1
            }

            sender = Sender().apply {
                id = 1
                displayName = "POPL"
            }
        },

            TransactionGroupListItem().apply {
                transactionGroup = TransactionGroup().apply {
                    name = "Excel File 2"
                    id = 2
                    defaultSenderId = 2
                }

                sender = Sender().apply {
                    id = 2
                    displayName = "POPW"
                }
            },

            TransactionGroupListItem().apply {
                transactionGroup = TransactionGroup().apply {
                    name = "Excel File 3"
                    id = 3
                    defaultSenderId = 3
                }

                sender = Sender().apply {
                    id = 3
                    displayName = "Some Sender"
                }
            },

            TransactionGroupListItem().apply {
                transactionGroup = TransactionGroup().apply {
                    name = "Papco Excel File"
                    id = 4
                    defaultSenderId = 1
                }

                sender = Sender().apply {
                    id = 4
                    displayName = "Some new Sender"
                }
            })

    }

    RTGSTheme {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            ExcelFileList(list = items, onClick = {}, onLongClick = {})
        }
    }

}
