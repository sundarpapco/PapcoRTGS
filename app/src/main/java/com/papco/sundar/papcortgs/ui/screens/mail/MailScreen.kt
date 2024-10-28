package com.papco.sundar.papcortgs.ui.screens.mail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.work.WorkInfo
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.database.pojo.CohesiveTransaction
import com.papco.sundar.papcortgs.database.receiver.Receiver
import com.papco.sundar.papcortgs.database.sender.Sender
import com.papco.sundar.papcortgs.database.transaction.Transaction
import com.papco.sundar.papcortgs.screens.mail.MailDispatcher
import com.papco.sundar.papcortgs.ui.components.RTGSAppBar
import com.papco.sundar.papcortgs.ui.dialogs.ConfirmationDialog
import com.papco.sundar.papcortgs.ui.screens.mail.MailScreenState.Dialog
import com.papco.sundar.papcortgs.ui.theme.RTGSTheme


@Composable
fun MailScreen(
    screenState: MailScreenState,
    onSendMails: () -> Unit,
    onBackPressed: () -> Unit,
    onSignOut: () -> Unit
) {
    Scaffold(topBar = {
        RTGSAppBar(
            title = stringResource(id = R.string.email_receivers),
            isBackEnabled = true,
            onBackPressed = onBackPressed
        )
    },

        floatingActionButton = {
            if(screenState.dispatcherState.isFinished){
                    FloatingActionButton(onClick = { screenState.showSendConfirmationDialog() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send mails"
                        )
                    }
                }

        }) {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            screenState.loggedInGmail?.let { email ->
                GmailInfo(
                    email = email, onSignOutClicked = { screenState.showSignOutConfirmationDialog() }
                )
            }

            if(screenState.dispatcherState==WorkInfo.State.ENQUEUED ||
                screenState.dispatcherState==WorkInfo.State.BLOCKED){
                MailScheduledInfo(
                    onCancelJob = {}
                )
            }

            MailTransactionsList(
                transactions = screenState.transactions ?: emptyList()
            )
        }
    }

    when (screenState.dialog) {
        is Dialog.SendConfirmation -> {
            ConfirmationDialog(title = stringResource(id = R.string.send_emails),
                message = stringResource(id = R.string.send_emails_confirmation),
                positiveButtonText = stringResource(id = R.string.send),
                negativeButtonText = stringResource(id = R.string.cancel),
                onPositiveClick = {
                    screenState.hideDialog()
                    onSendMails()
                },
                onNegativeClick = { screenState.hideDialog() })
        }

        is Dialog.SignOutConfirmation -> {
            ConfirmationDialog(title = stringResource(id = R.string.sign_out_dialog_title),
                message = stringResource(id = R.string.sign_out_dialog_message),
                positiveButtonText = stringResource(id = R.string.sign_out),
                negativeButtonText = stringResource(id = R.string.cancel),
                onPositiveClick = {
                    screenState.hideDialog()
                    onSignOut()
                },
                onNegativeClick = { screenState.hideDialog() })
        }

        null -> {}
    }
}


@Composable
fun MailTransactionsList(
    transactions: List<CohesiveTransaction>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(transactions, key = {
            it.transaction.id
        }) {
            MailListItem(transaction = it)
        }
    }
}

@Preview
@Composable
private fun MailScreenPreview() {

    val screenState = remember {

        val transactions = listOf(CohesiveTransaction(Transaction(
            id = 1, mailSent = MailDispatcher.SENT
        ), Sender().apply {
            id = 1
            displayName = "Papco offset private limited"
        }, Receiver().apply {
            id = 1
            displayName = "SRI VANI AGENCIES"
            email = "sreevani@gmail.com"
        }),

            CohesiveTransaction(Transaction(
                id = 2, mailSent = MailDispatcher.QUEUED
            ), Sender().apply {
                id = 2
                displayName = "Papco offset private limited"
            }, Receiver().apply {
                id = 2
                displayName = "SURI GRAPHICS"
                email = "suri@gmail.com"
            }),

            CohesiveTransaction(Transaction(
                id = 3, mailSent = MailDispatcher.ERROR
            ), Sender().apply {
                id = 3
                displayName = "Papco offset private limited"
            }, Receiver().apply {
                id = 3
                displayName = "RAGAVENDRA CALENDARS"
                email = "ragavendra@gmail.com"
            }))

        MailScreenState().apply {
            loggedInGmail = "papcodeveloper@gmail.com"
            this.transactions = transactions
            dispatcherState = WorkInfo.State.ENQUEUED
        }

    }

    RTGSTheme {
        MailScreen(screenState, onSendMails = {}, onBackPressed = {}, onSignOut = {})
    }

}