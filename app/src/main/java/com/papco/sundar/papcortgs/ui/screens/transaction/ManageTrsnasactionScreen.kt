package com.papco.sundar.papcortgs.ui.screens.transaction

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.ui.components.ClickableTextField
import com.papco.sundar.papcortgs.ui.components.MenuAction
import com.papco.sundar.papcortgs.ui.components.OptionsMenu
import com.papco.sundar.papcortgs.ui.components.RTGSAppBar
import com.papco.sundar.papcortgs.ui.components.TextInputField
import com.papco.sundar.papcortgs.ui.screens.LoadingScreen
import com.papco.sundar.papcortgs.ui.theme.RTGSTheme

@Composable
fun ManageTransactionScreen(
 screenState: ManageTransactionScreenState,
 title:String,
 onSenderClicked:()->Unit,
 onReceiverClicked:()->Unit,
 onSave:()->Unit,
 onDismiss:()->Unit
){
    val context = LocalContext.current

    Scaffold(
        topBar = {
            RTGSAppBar(
                title = title,
                isBackEnabled = true,
                onBackPressed = onDismiss,
                optionsMenu = {
                    TransactionOptionsMenu {
                        if(it== context.getString(R.string.save))
                            onSave()
                    }
                }
            )
        }
    ) {
        if(screenState.isLoading)
            LoadingScreen()
        else
            ScreenContent(
                modifier = Modifier.padding(it),
                screenState = screenState,
                onSenderClicked = onSenderClicked,
                onReceiverClicked =onReceiverClicked)
    }
}

@Composable
private fun ScreenContent(
    screenState: ManageTransactionScreenState,
    onSenderClicked: () -> Unit,
    onReceiverClicked: () -> Unit,
    modifier:Modifier = Modifier
){
    Column(
        modifier= modifier
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
            onValueChange = {screenState.setAmountAs(it)}
        )

        TextInputField(
            modifier=Modifier.fillMaxWidth(),
            text = screenState.remarks ?: stringResource(id = R.string.on_account),
            label = stringResource(id = R.string.remarks),
            onChange = {screenState.loadRemarks(it)}
        )
    }
}

@Composable
private fun TransactionOptionsMenu(
    onMenuSelected:(String)->Unit
){

    val context = LocalContext.current
    val menu = remember {
        listOf(
            MenuAction(
                imageVector = Icons.Filled.Done,
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
    amount:String,
    onValueChange:(String)->Unit
){

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
                modifier= Modifier
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
                    modifier = Modifier.weight(1f),
                    value = amount,
                    onValueChange = onValueChange,
                    textStyle = MaterialTheme.typography.headlineLarge.copy(fontWeight= FontWeight.SemiBold),
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
private fun PreviewScreen(){

    val screenState = remember {
        ManageTransactionScreenState()
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
private fun PreviewAmountField(){


    var amount by remember{mutableStateOf("")}

    RTGSTheme {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ){
            AmountField(
                amount = amount,
                onValueChange = {amount=it}
            )
        }
    }
}