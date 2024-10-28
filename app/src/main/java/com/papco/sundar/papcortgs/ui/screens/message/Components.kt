package com.papco.sundar.papcortgs.ui.screens.message


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.database.pojo.CohesiveTransaction
import com.papco.sundar.papcortgs.database.receiver.Receiver
import com.papco.sundar.papcortgs.database.sender.Sender
import com.papco.sundar.papcortgs.database.transaction.Transaction
import com.papco.sundar.papcortgs.screens.mail.MailDispatcher
import com.papco.sundar.papcortgs.screens.sms.MessageDispatcher
import com.papco.sundar.papcortgs.ui.components.ErrorIcon
import com.papco.sundar.papcortgs.ui.components.SuccessIcon
import com.papco.sundar.papcortgs.ui.theme.RTGSTheme

@Composable
fun MessageScheduledInfo(
    onCancelJob:()->Unit
){
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = MaterialTheme.shapes.medium,
        onClick = onCancelJob
    ) {
        Row(
            modifier=Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Icon(
                modifier=Modifier.size(25.dp),
                imageVector = Icons.Outlined.Info,
                contentDescription = "Scheduled Information"
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                modifier=Modifier.weight(1f),
                text = stringResource(id = R.string.message_dispatcher_scheduled_info),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun MessageListItem(
    transaction:CohesiveTransaction,
    modifier: Modifier = Modifier
){
    Surface(
        modifier= modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .clip(MaterialTheme.shapes.medium)
    ) {

        Row(
            modifier= Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(text = transaction.receiver.displayName, style = MaterialTheme.typography.titleMedium  )
                Text(text = transaction.receiver.mobileNumber, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

           Box(
                modifier=Modifier.fillMaxHeight(),
                contentAlignment = Alignment.Center
            ){
                when(transaction.transaction.messageSent){
                    MailDispatcher.SENT->{
                        SuccessIcon(text = stringResource(id = R.string.sent))
                    }
                    MailDispatcher.QUEUED->{
                        CircularProgressIndicator(modifier=Modifier.size(30.dp))
                    }
                    MailDispatcher.ERROR->{
                        ErrorIcon(text = stringResource(id = R.string.error))
                    }
                }
            }
        }
    }
}


@Preview
@Composable
private fun TestMessageListItem(){

    val transaction = remember{

        val sender = Sender().apply{
            id=1
            displayName="Papco offset private limited"
        }

        val receiver = Receiver().apply {
            id=1
            displayName="SRI VANI AGENCIES"
            mobileNumber = "9047013696"
        }

        val transaction = Transaction().apply {
            id=1
            messageSent = MessageDispatcher.QUEUED
        }

        CohesiveTransaction(transaction,sender,receiver)

    }


    RTGSTheme {

        Box(
            modifier= Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ){
            MessageListItem(transaction = transaction)
        }
    }
}

@Preview
@Composable
private fun PreviewScheduledInfo(){
    RTGSTheme {
        Box(
            modifier=Modifier.padding(12.dp)
        ){
           MessageScheduledInfo {
           }
        }
    }
}
