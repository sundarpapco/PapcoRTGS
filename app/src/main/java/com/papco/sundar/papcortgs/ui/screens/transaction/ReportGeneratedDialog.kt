package com.papco.sundar.papcortgs.ui.screens.transaction

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.papco.sundar.papcortgs.R

@Composable
fun ReportGeneratedDialog(
    filename:String,
    onShare: (String) -> Unit,
    onDismiss: () -> Unit
) {

    AlertDialog(title = {
        Text(
            text = stringResource(id = R.string.report_generated),
            style = MaterialTheme.typography.titleMedium
        )
    }, text = {
        Text(
            text = stringResource(id = R.string.report_generated_msg,filename),
            style = MaterialTheme.typography.titleSmall
        )
    }, onDismissRequest = onDismiss, confirmButton = {
        TextButton(onClick = {onShare(filename)}) {
            Text(text = stringResource(id = R.string.share))
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss) {
            Text(text = stringResource(id = R.string.cancel))
        }
    })
}