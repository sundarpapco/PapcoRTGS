package com.papco.sundar.papcortgs.ui.screens.transaction

import android.annotation.SuppressLint
import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.screens.transaction.createTransaction.CreateTransactionVM
import com.papco.sundar.papcortgs.ui.ManageTransaction
import com.papco.sundar.papcortgs.ui.SelectReceiver
import com.papco.sundar.papcortgs.ui.SelectSender
import com.papco.sundar.papcortgs.ui.components.ClickableTextField
import com.papco.sundar.papcortgs.ui.components.MenuAction
import com.papco.sundar.papcortgs.ui.components.OptionsMenu
import com.papco.sundar.papcortgs.ui.components.RTGSAppBar
import com.papco.sundar.papcortgs.ui.components.TextInputField
import com.papco.sundar.papcortgs.ui.screens.LoadingScreen
import com.papco.sundar.papcortgs.ui.theme.RTGSTheme
import com.papco.sundar.papcortgs.ui.util.ResultEffect
import com.papco.sundar.papcortgs.ui.util.ResultEventBus
import com.papco.sundar.papcortgs.ui.util.Toaster
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

fun EntryProviderScope<NavKey>.manageTransactionScreenEntry(
    backStack: NavBackStack<NavKey>,
    resultBus: ResultEventBus
) {
    entry<ManageTransaction> { key ->

        val context = LocalContext.current
        val viewModel: CreateTransactionVM = viewModel(
            factory = CreateTransactionVM.factory(
                application = context.applicationContext as Application,
                transactionId = key.transactionId,
                groupId = key.groupId,
                defaultSenderId = key.defaultSenderId
            )
        )
        val screenState = viewModel.screen
        //val isEditingMode = remember { key.transactionId != -1 }

        ManageTransactionScreen(
            screenState = screenState,
            title = stringResource(screenState.titleResource),
            onSenderClicked = {
                if (screenState.selectedSender != null)
                    backStack.add(SelectSender)
            },
            onReceiverClicked = {
                if (screenState.selectedReceiver != null)
                    backStack.add(SelectReceiver(key.groupId))
            },
            onSave = {
                if (screenState.validate())
                    viewModel.onSaveTransaction()

            },
            onDismiss = { backStack.removeLastOrNull() }
        )

        ResultEffect<Int>(bus = resultBus, key = "selectedSender") {
            if (it != -1) {
                viewModel.selectSender(it)
            }
        }

        ResultEffect<Int>(bus = resultBus, key = "selectedReceiver") {
            if (it != -1) {
                viewModel.selectReceiver(it)
            }
        }

        LaunchedEffect(key1 = true) {
            screenState.popUpBackStack.collect { needToGoBack ->
                if (needToGoBack)
                    backStack.removeLastOrNull()
            }
        }
    }
}

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun ManageTransactionScreen(
    screenState: ManageTransactionScreenState,
    title: String,
    onSenderClicked: () -> Unit,
    onReceiverClicked: () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            RTGSAppBar(
                title = title,
                isBackEnabled = true,
                onBackPressed = onDismiss,
                optionsMenu = {
                    TransactionOptionsMenu {
                        if (it == context.getString(R.string.save))
                            onSave()
                    }
                }
            )
        }
    ) {
        if (screenState.isLoading)
            LoadingScreen()
        else
            ScreenContent(
                modifier = Modifier.padding(it),
                screenState = screenState,
                onSenderClicked = onSenderClicked,
                onReceiverClicked = onReceiverClicked
            )
    }

    Toaster(context,screenState)
}

@Composable
private fun ScreenContent(
    screenState: ManageTransactionScreenState,
    onSenderClicked: () -> Unit,
    onReceiverClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        ClickableTextField(
            label = stringResource(id = R.string.sender),
            text = screenState.selectedSender?.displayName
                ?: stringResource(id = R.string.please_add_a_sender_first),
            onClick = onSenderClicked
        )

        ClickableTextField(
            label = stringResource(id = R.string.receiver),
            text = screenState.selectedReceiver?.displayName
                ?: stringResource(id = R.string.please_add_a_receiver_first),
            onClick = onReceiverClicked
        )

        AmountField(
            amount = screenState.amount,
            onValueChange = { screenState.setAmountAs(it) }
        )

        TextInputField(
            modifier = Modifier.fillMaxWidth(),
            text = screenState.remarks ?: stringResource(id = R.string.on_account),
            label = stringResource(id = R.string.remarks),
            onChange = { screenState.loadRemarks(it) }
        )
    }
}

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
private fun TransactionOptionsMenu(
    onMenuSelected: (String) -> Unit
) {

    val context = LocalContext.current
    val painter = painterResource(R.drawable.ic_done)
    val menu = remember {
        listOf(
            MenuAction(
                painter = painter,
                label = context.getString(R.string.save)
            )
        )
    }

    OptionsMenu(
        menuItems = menu,
        onItemClick = onMenuSelected

    )
}

@Composable
private fun AmountField(
    amount: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit
) {

    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = stringResource(id = R.string.amount),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                style = MaterialTheme.typography.labelMedium
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max)
            ) {

                Icon(
                    painterResource(id = R.drawable.ic_rupee),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    contentDescription = "Rupee Symbol",
                    modifier = Modifier
                        .width(24.dp)
                        .fillMaxHeight()
                )

                TextField(
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                // Select everything from index 0 to the end of the text
                                scope.launch {
                                    delay(100.milliseconds)
                                    onValueChange(
                                        amount.copy(
                                            selection = TextRange(amount.text.length, 0)
                                        )
                                    )
                                }

                            }
                        },
                    value = amount,
                    onValueChange = onValueChange,
                    textStyle = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.SemiBold),
                    colors = TextFieldDefaults.colors().copy(
                        focusedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        cursorColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        focusedLeadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),

                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }
    }

}

@Preview
@Composable
private fun PreviewScreen() {

    val screenState = remember {
        ManageTransactionScreenState(R.string.update_transaction)
    }

    RTGSTheme {
        ManageTransactionScreen(
            screenState = screenState,
            title = "Create Transaction",
            onSenderClicked = {},
            onReceiverClicked = { },
            onSave = { }) {

        }
    }

}

@Preview
@Composable
private fun PreviewAmountField() {


    var amount by remember { mutableStateOf(TextFieldValue("")) }

    RTGSTheme {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            AmountField(
                amount = amount,
                onValueChange = { amount = it }
            )
        }
    }
}