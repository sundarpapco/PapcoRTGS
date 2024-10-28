package com.papco.sundar.papcortgs.ui.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.papco.sundar.papcortgs.R

@Composable
fun DeleteConfirmationDialog(
    title:String,
    message:String,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {

    AlertDialog(title = {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )
    }, text = {
        Text(
            text = message,
            style = MaterialTheme.typography.titleSmall
        )
    }, onDismissRequest = onDismiss, confirmButton = {
        TextButton(onClick = onDelete) {
            Text(text = stringResource(id = R.string.delete))
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss) {
            Text(text = stringResource(id = R.string.cancel))
        }
    })
}