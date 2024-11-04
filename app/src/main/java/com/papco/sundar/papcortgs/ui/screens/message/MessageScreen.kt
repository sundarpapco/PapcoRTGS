package com.papco.sundar.papcortgs.ui.screens.message

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.work.WorkInfo
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.database.pojo.CohesiveTransaction
import com.papco.sundar.papcortgs.database.receiver.Receiver
import com.papco.sundar.papcortgs.database.sender.Sender
import com.papco.sundar.papcortgs.database.transaction.Transaction
import com.papco.sundar.papcortgs.screens.sms.MessageDispatcher
import com.papco.sundar.papcortgs.ui.components.RTGSAppBar
import com.papco.sundar.papcortgs.ui.dialogs.ConfirmationDialog
import com.papco.sundar.papcortgs.ui.screens.message.MessageScreenState.Dialog
import com.papco.sundar.papcortgs.ui.theme.RTGSTheme


@Composable
fun MessageScreen(
    screenState: MessageScreenState,
    onSendMessages: () -> Unit,
    onBackPressed: () -> Unit
) {

    val permissionsRequestLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = {
            var granted = true
            for(pair in it){
                if(!pair.value)
                    granted=false
            }

            if(granted)
                screenState.showSendConfirmationDialog()
        }
    )
    val context = LocalContext.current

    Scaffold(topBar = {
        RTGSAppBar(
            title = stringResource(id = R.string.send_messages),
            isBackEnabled = true,
            onBackPressed = onBackPressed
        )
    },

        floatingActionButton = {
            if(screenState.dispatcherState.isFinished){
                    FloatingActionButton(onClick = {
                        if(weHaveSMSPermission(context))
                            screenState.showSendConfirmationDialog()
                        else
                            permissionsRequestLauncher.launch(requiredPermissionsArray())
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send messages"
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


            if(screenState.dispatcherState==WorkInfo.State.ENQUEUED ||
                screenState.dispatcherState==WorkInfo.State.BLOCKED){
                MessageScheduledInfo(
                    onCancelJob = {}
                )
            }

            MessageTransactionsList(
                transactions = screenState.transactions ?: emptyList()
            )
        }
    }

    when (screenState.dialog) {
        is Dialog.SendConfirmation -> {
            ConfirmationDialog(title = stringResource(id = R.string.send_messages),
                message = stringResource(id = R.string.send_messages_confirmation),
                positiveButtonText = stringResource(id = R.string.send),
                negativeButtonText = stringResource(id = R.string.cancel),
                onPositiveClick = {
                    screenState.hideDialog()
                    onSendMessages()
                },
                onNegativeClick = { screenState.hideDialog() })
        }

        null -> {}
    }
}


@Composable
fun MessageTransactionsList(
    transactions: List<CohesiveTransaction>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(transactions, key = {
            it.transaction.id
        }) {
            MessageListItem(transaction = it)
        }
    }
}

private fun weHaveSMSPermission(context: Context): Boolean {
    if (ContextCompat.checkSelfPermission(
            context, Manifest.permission.SEND_SMS
        ) != PackageManager.PERMISSION_GRANTED
    ) return false

    return ContextCompat.checkSelfPermission(
        context, Manifest.permission.READ_PHONE_STATE
    ) == PackageManager.PERMISSION_GRANTED
}

private fun requiredPermissionsArray():Array<String> {

    val requiredPermissions= mutableListOf(Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.SEND_SMS,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS)

    if(Build.VERSION.SDK_INT > 32)
        requiredPermissions.add(Manifest.permission.POST_NOTIFICATIONS)


    return requiredPermissions.toTypedArray()

}

@Preview
@Composable
private fun MessageScreenPreview() {

    val screenState = remember {

        val transactions = listOf(CohesiveTransaction(Transaction(
            id = 1, messageSent = MessageDispatcher.SENT
        ), Sender().apply {
            id = 1
            displayName = "Papco offset private limited"
        }, Receiver().apply {
            id = 1
            displayName = "SRI VANI AGENCIES"
            mobileNumber="9047013696"
        }),

            CohesiveTransaction(Transaction(
                id = 2, messageSent = MessageDispatcher.QUEUED
            ), Sender().apply {
                id = 2
                displayName = "Papco offset private limited"
            }, Receiver().apply {
                id = 2
                displayName = "SURI GRAPHICS"
                mobileNumber=""
            }),

            CohesiveTransaction(Transaction(
                id = 3, messageSent = MessageDispatcher.ERROR
            ), Sender().apply {
                id = 3
                displayName = "Papco offset private limited"
            }, Receiver().apply {
                id = 3
                displayName = "RAGAVENDRA CALENDARS"
                mobileNumber="9843838696"
            }))

        MessageScreenState().apply {
            this.transactions = transactions
            dispatcherState = WorkInfo.State.SUCCEEDED
        }

    }

    RTGSTheme {
        MessageScreen(screenState, onSendMessages = {}, onBackPressed = {})
    }

}