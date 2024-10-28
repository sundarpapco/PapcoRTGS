package com.papco.sundar.papcortgs.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.ui.components.TextInputField
import com.papco.sundar.papcortgs.ui.theme.RTGSTheme

@Composable
fun PasswordDialog(
    onCorrectPassword: () -> Unit,
    onDismiss: () -> Unit
) {

    var text by rememberSaveable {
        mutableStateOf("")
    }

    var error: String? by rememberSaveable {
        mutableStateOf(null)
    }

    val errorText = stringResource(id = R.string.incorrect_password)

    AlertDialog(title = {
        Text(
            text = stringResource(id = R.string.enter_password),
            style = MaterialTheme.typography.titleMedium
        )
    }, onDismissRequest = onDismiss, confirmButton = {
        TextButton(onClick = {
            if (isPasswordCorrect(text)) onCorrectPassword() else error = errorText
        }) {
            Text(text = stringResource(id = R.string.ok))
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss) {
            Text(text = stringResource(id = R.string.cancel))
        }
    }, text = {
        TextInputField(text = text,
            label = stringResource(id = R.string.password),
            onChange = {
                error = null
                text = it
            },
            error = error,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                if (isPasswordCorrect(text)) onCorrectPassword()
                else error = errorText
            },
            )
        )
    })
}

private fun isPasswordCorrect(password: String): Boolean {
    return password == "papco1954"
}

@Preview
@Composable
private fun PreviewPasswordDialog() {

    RTGSTheme {

        var dialogShowing by remember{
            mutableStateOf(false)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            TextButton(onClick = { dialogShowing=true }) {
                Text(text = "SHOW PASSWORD DIALOG")
            }
        }

        if(dialogShowing)
            PasswordDialog(
                onCorrectPassword = {  },
                onDismiss = {dialogShowing=false}
                )
    }

}