package com.papco.sundar.papcortgs.ui.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun ConfirmationDialog(
    title:String,
    message:String,
    positiveButtonText:String,
    negativeButtonText:String,
    onPositiveClick: () -> Unit,
    onNegativeClick: () -> Unit
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
    }, onDismissRequest = onNegativeClick,
        confirmButton = {
        TextButton(onClick = onPositiveClick) {
            Text(text = positiveButtonText)
        }
    }, dismissButton = {
        TextButton(onClick = onNegativeClick) {
            Text(negativeButtonText)
        }
    })
}