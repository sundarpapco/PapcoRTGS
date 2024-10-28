package com.papco.sundar.papcortgs.ui.screens.transaction

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.papco.sundar.papcortgs.database.transaction.Transaction
import com.papco.sundar.papcortgs.database.transaction.TransactionForList
import com.papco.sundar.papcortgs.ui.components.MenuAction
import com.papco.sundar.papcortgs.ui.components.RTGSAppBar
import com.papco.sundar.papcortgs.ui.theme.RTGSTheme
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.ui.components.OptionsMenu
import com.papco.sundar.papcortgs.ui.dialogs.DeleteConfirmationDialog
import com.papco.sundar.papcortgs.ui.screens.transaction.TransactionListScreenState.Dialog

@Composable
fun TransactionListScreen(
    title: String,
    screenState: TransactionListScreenState,
    onBackPressed: () -> Unit,
    onClick: (TransactionForList) -> Unit,
    onAddTransaction: () -> Unit,
    onDelete: (Int) -> Unit,
    onExportManualRTGSFile: (String) -> Unit,
    onExportAutoRTGSFile: (Long) -> Unit,
    onDispatchMessages: () -> Unit,
    onDispatchMails: () -> Unit,
    onShareFile: (String) -> Unit
) {

    val context = LocalContext.current
    val optionsMenuItems = remember {
        prepareOptionsMenu(context)
    }

    Scaffold(topBar = {
        RTGSAppBar(
            title = title,
            isBackEnabled = true,
            onBackPressed = onBackPressed,
            optionsMenu = {
                TransactionListOptionsMenu(options = optionsMenuItems,
                    onAutoExcelFileExport = { screenState.dialog=Dialog.DatePicker },
                    onSendMessages = onDispatchMessages,
                    onSendEmail = onDispatchMails,
                    onExcelFileExport = { screenState.dialog = Dialog.ChequeNumberDialog })
            })
    }, floatingActionButton = {
        FloatingActionButton(onClick = onAddTransaction) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = "Add Transaction")
        }
    }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            val totalAmount = remember(screenState.transactions) {
                val tot = screenState.transactions.sumOf { it.amount }
                Transaction.formatAmountAsString(tot)
            }

            Spacer(modifier = Modifier.height(12.dp))
            TotalField(amount = totalAmount)
            TransactionList(
                transactions = screenState.transactions,
                onClick = onClick,
                onLongClick = {
                    screenState.dialog = Dialog.DeleteConfirmation(it.id)
                })

        }
    }

    screenState.dialog?.let {

        when (it) {
            is Dialog.DeleteConfirmation -> {
                DeleteConfirmationDialog(title = stringResource(id = R.string.delete_transaction),
                    message = stringResource(id = R.string.delete_transaction_msg),
                    onDelete = {
                        screenState.dialog = null
                        onDelete(it.id)
                    },
                    onDismiss = { screenState.dialog = null })
            }

            is Dialog.ReportGenerated -> {
                ReportGeneratedDialog(filename = it.fileName, onShare = { fileName ->
                    screenState.dialog = null
                    onShareFile(fileName)
                }, onDismiss = { screenState.dialog = null })
            }

            is Dialog.ChequeNumberDialog -> {
                ChequeNumberDialog(onOk = { chequeNumber ->
                    screenState.dialog = null
                    onExportManualRTGSFile(chequeNumber)
                }, onDismiss = { screenState.dialog = null })
            }

            is Dialog.DatePicker->{
                RTGSDatePickerDialog(
                    onDateSelected = {date->
                        screenState.dialog=null
                        onExportAutoRTGSFile(date)
                                     } ,
                    onDismiss = {screenState.dialog=null}
                )
            }
        }
    }

}

@Composable
private fun TransactionListOptionsMenu(
    options: List<MenuAction>,
    onAutoExcelFileExport: () -> Unit,
    onSendMessages: () -> Unit,
    onSendEmail: () -> Unit,
    onExcelFileExport: () -> Unit
) {
    val context = LocalContext.current

    OptionsMenu(menuItems = options) {
        when (it) {
            context.getString(R.string.export_auto_excel_file) -> {
                onAutoExcelFileExport()
            }

            context.getString(R.string.send_messages) -> {
                onSendMessages()
            }

            context.getString(R.string.send_emails) -> {
                onSendEmail()
            }

            context.getString(R.string.export_excel_file) -> {
                onExcelFileExport()
            }
        }
    }
}


@Composable
private fun TransactionList(
    transactions: List<TransactionForList>,
    onClick: (TransactionForList) -> Unit,
    onLongClick: (TransactionForList) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        items(transactions, key = {
            it.id
        }) {transaction->
            TransactionListItem(transaction, onClick = { onClick(transaction) }, onLongClick = { onLongClick(transaction) })
        }
    }
}

@Composable
private fun TotalField(
    amount: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(12.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Text(
            text = amount,
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

private fun prepareOptionsMenu(context: Context): List<MenuAction> {

    return listOf(
        MenuAction(
            iconId = R.drawable.ic_export_file,
            label = context.getString(R.string.export_auto_excel_file)
        ),

        MenuAction(
            iconId = R.drawable.ic_sms, label = context.getString(R.string.send_messages)
        ),

        MenuAction(
            label = context.getString(R.string.send_emails)
        ),

        MenuAction(
            label = context.getString(R.string.export_excel_file)
        )
    )

}

@Preview
@Composable
private fun PreviewTotalField() {

    RTGSTheme {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            TotalField(amount = "₹1, 52,605")
        }
    }
}


@Preview
@Composable
private fun PreviewTransactionListScreen() {

    val transactions = remember {
        listOf(

            TransactionForList().apply {
                id = 1
                receiver = "SRI VANI AGENCIES"
                sender = "PAPCO OFFSET PRIVATE LIMITED"
                amount = 12300
            },

            TransactionForList().apply {
                id = 2
                receiver = "MADHANA"
                sender = "Papco offset private limited"
                amount = 5000
            },

            TransactionForList().apply {
                id = 3
                receiver = "RITHANYA"
                sender = "Papco offset private limited"
                amount = 7450
            },

            TransactionForList().apply {
                id = 4
                receiver = "SAATVIK"
                sender = "Papco offset private limited"
                amount = 14240
            }

        )


    }

    val screenState = remember {
        TransactionListScreenState().apply {
            this.transactions = transactions
        }
    }

    RTGSTheme {
        TransactionListScreen(title = "Papco Excel File",
            screenState = screenState,
            onBackPressed = { },
            onClick = {},
            onAddTransaction = { },
            onDelete = {},
            onExportManualRTGSFile = { },
            onExportAutoRTGSFile = { },
            onDispatchMessages = { },
            onDispatchMails = {},
            onShareFile = {})
    }
}
