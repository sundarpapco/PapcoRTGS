package com.papco.sundar.papcortgs.ui.screens.transaction

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.papco.sundar.papcortgs.database.transaction.TransactionForList
import com.papco.sundar.papcortgs.ui.theme.RTGSTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionListItem(
    transaction:TransactionForList,
    onClick:(TransactionForList)->Unit,
    onLongClick:(TransactionForList)->Unit,
    modifier: Modifier = Modifier
){
    Surface(
        modifier= modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .clip(MaterialTheme.shapes.medium)
            .combinedClickable(onClick = {
                onClick(transaction)
            }, onLongClick = {
                onLongClick(transaction)
            })

    ) {

        val amount:String= remember(transaction.amount){
            transaction.amountAsString
        }

        Row(
            modifier=Modifier.fillMaxWidth().height(IntrinsicSize.Max).padding(12.dp)
        ) {
            Column(Modifier.weight(1f)) {
                Text(text = transaction.receiver, style = MaterialTheme.typography.titleMedium  )
                Text(text = transaction.sender, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Box(
                modifier=Modifier.fillMaxHeight(),
                contentAlignment = Alignment.BottomEnd
            ){
                Text(
                    text = amount,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Preview
@Composable
private fun TestMoneyListItem(){

    val transaction = remember{

        TransactionForList().apply {
            id=1
            receiver = "SRI VANI AGENCIES"
            sender = "PAPCO OFFSET PRIVATE LIMITED"
            amount=1234
        }

    }


    RTGSTheme {

        Box(
            modifier=Modifier.background(MaterialTheme.colorScheme.surface).padding(16.dp)
        ){
            TransactionListItem(
                transaction = transaction,
                onClick = {},
                onLongClick = {}
            )
        }
    }

}