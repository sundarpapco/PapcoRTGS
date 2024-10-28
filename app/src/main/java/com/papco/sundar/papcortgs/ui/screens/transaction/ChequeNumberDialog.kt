package com.papco.sundar.papcortgs.ui.screens.transaction

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.ui.components.TextInputField

@Composable
fun ChequeNumberDialog(
    onOk: (String) -> Unit, onDismiss: () -> Unit
) {

    var text by rememberSaveable {
        mutableStateOf("")
    }

    AlertDialog(title = {
        Text(
            text = stringResource(id = R.string.enter_cheque_number),
            style = MaterialTheme.typography.titleMedium
        )
    }, onDismissRequest = onDismiss
        , confirmButton = {
        TextButton(onClick = { onOk(text) }, enabled = text.isNotBlank()) {
            Text(text = stringResource(id = R.string.ok))
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss) {
            Text(text = stringResource(id = R.string.cancel))
        }
    }, text = {
        TextInputField(text = text,
            label = stringResource(id = R.string.cheque_number),
            onChange = {
                text = it
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onOk(text) }))
    })
}
