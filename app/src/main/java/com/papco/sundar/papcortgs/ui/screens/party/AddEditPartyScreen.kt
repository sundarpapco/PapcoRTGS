package com.papco.sundar.papcortgs.ui.screens.party

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.ui.components.RTGSAppBar
import com.papco.sundar.papcortgs.ui.components.TextInputField
import com.papco.sundar.papcortgs.ui.dialogs.WaitDialog
import com.papco.sundar.papcortgs.ui.theme.RTGSTheme
import com.papco.sundar.papcortgs.ui.components.MenuAction
import com.papco.sundar.papcortgs.ui.components.OptionsMenu

@Composable
fun AddEditPartyScreen(
    title: String, state: AddEditPartyState, onBackPressed: () -> Unit, onFormSubmit: () -> Unit
) {


    Scaffold(topBar = {
        RTGSAppBar(
            title = title,
            isBackEnabled = true,
            onBackPressed = onBackPressed
        ) {
            OptionsMenu(
                menuItems = listOf(
                    MenuAction(
                        imageVector = Icons.Filled.Done, label = stringResource(id = R.string.done)
                    )
                )
            ) {
                if (state.validateState()) onFormSubmit()
            }
        }
    }) { paddingValues ->

        PartyDetailsForm(modifier = Modifier.padding(paddingValues),
            state = state,
            onFormSubmit = {
                if(state.validateState())
                    onFormSubmit()
            })

    }

    if(state.isWaiting)
        WaitDialog()
}


@Composable
private fun PartyDetailsForm(
    state: AddEditPartyState, onFormSubmit: () -> Unit, modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val focusRequester = remember {
        FocusRequester()
    }

    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TextInputField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            text = state.displayName,
            label = stringResource(id = R.string.display_name),
            error = state.displayNameError,
            keyboardActions = KeyboardActions(onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            }),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            )
        ) {
            state.loadDisplayName(it)
        }

        TextInputField(
            modifier = Modifier.fillMaxWidth(),
            text = state.accountName,
            label = stringResource(id = R.string.account_name),
            error = state.accountNameError,
            keyboardActions = KeyboardActions(onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            }),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Next
            )
        ) {
            state.loadAccountName(it)
        }

        TextInputField(
            modifier = Modifier.fillMaxWidth(),
            text = state.accountNumber,
            label = stringResource(id = R.string.account_number),
            error = state.accountNumberError,
            keyboardActions = KeyboardActions(onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            }),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Next
            ),
            visualTransformation = PasswordVisualTransformation()
        ) {
            state.loadAccountNumber(it)
        }

        TextInputField(
            modifier = Modifier.fillMaxWidth(),
            text = state.confirmAccountNumber,
            label = stringResource(id = R.string.confirm_account_number),
            error = state.confirmAccountNumberError,
            keyboardActions = KeyboardActions(onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            }),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                capitalization = KeyboardCapitalization.Characters,
                imeAction = ImeAction.Next
            )
        ) {
            state.loadConfirmAccountNumber(it)
        }

        TextInputField(
            modifier = Modifier.fillMaxWidth(),
            text = state.accountType,
            label = stringResource(id = R.string.account_type),
            error = state.accountTypeError,
            keyboardActions = KeyboardActions(onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            }),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Next
            )
        ) {
            state.loadAccountType(it)
        }

        TextInputField(
            modifier = Modifier.fillMaxWidth(),
            text = state.ifsCode,
            label = stringResource(id = R.string.ifs_code),
            error = state.ifsCodeError,
            keyboardActions = KeyboardActions(onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            }),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Next
            )
        ) {
            state.loadIfsCode(it)
        }

        TextInputField(
            modifier = Modifier.fillMaxWidth(),
            text = state.bankAndBranch,
            label = stringResource(id = R.string.bank_and_branch),
            error = state.bankAndBranchError,
            keyboardActions = KeyboardActions(onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            }),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Next
            )
        ) {
            state.loadBankAndBranch(it)
        }

        TextInputField(
            modifier = Modifier.fillMaxWidth(),
            text = state.mobileNumber,
            label = stringResource(id = R.string.mobile_number),
            error = state.mobileNumberError,
            keyboardActions = KeyboardActions(onNext = {
                focusManager.moveFocus(FocusDirection.Down)
            }),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
            )
        ) {
            state.loadMobileNumber(it)
        }

        TextInputField(
            modifier = Modifier.fillMaxWidth(),
            text = state.email,
            label = stringResource(id = R.string.email),
            error = state.emailError,
            keyboardActions = KeyboardActions(onDone = {
                if (state.validateState()) onFormSubmit()
            }),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email, imeAction = ImeAction.Done
            )
        ) {
            state.loadEmail(it)
        }

    }
}


@Preview
@Composable
private fun PreviewAddEditSenderScreen() {

    val context = LocalContext.current

    val state = remember {
        AddEditPartyState(context)
    }

    RTGSTheme {
        AddEditPartyScreen(title = "Create Sender", state = state, onBackPressed = {}) {

        }
    }

}