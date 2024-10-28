package com.papco.sundar.papcortgs.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.ui.theme.RTGSTheme

@Composable
fun WaitDialog(){
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(dismissOnBackPress = false,dismissOnClickOutside = false)
    ) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.small)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ){
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = stringResource(id = R.string.one_moment_please), style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Preview
@Composable
private fun PreviewWaitDialog(){
    RTGSTheme {
        WaitDialog()
    }
}