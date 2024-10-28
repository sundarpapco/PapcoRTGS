package com.papco.sundar.papcortgs.ui.screens.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.papco.sundar.papcortgs.database.transaction.TransactionForList
import com.papco.sundar.papcortgs.ui.theme.RTGSTheme

@Composable
fun TransactionList(
    modifier:Modifier=Modifier,
    data:List<TransactionForList>,
    onClick:(TransactionForList)->Unit,
    onLongClick:(TransactionForList)->Unit
){
    LazyColumn(
        modifier=modifier
    ) {
        items(data, key = {it.id}){
            TransactionListItem(
                transaction=it,
                onClick={ onClick(it)},
                onLongClick={onLongClick(it)}
            )
        }
    }
}

@Preview
@Composable
private fun TestList(){


    val data:List<TransactionForList> = remember{
        listOf(

            TransactionForList().apply {
                id=1
                receiver="SRI VANI AGENCIES"
                sender="PAPCO OFFSET PRIVATE LIMITED"
                amount=12300
            },

            TransactionForList().apply {
                id=2
                receiver="MADHANA"
                sender="Papco offset private limited"
                amount=5000
            },

            TransactionForList().apply {
                id=3
                receiver="RITHANYA"
                sender="Papco offset private limited"
                amount=7450
            },

            TransactionForList().apply {
                id=4
                receiver="SAATVIK"
                sender="Papco offset private limited"
                amount=14240
            }

        )
    }

    RTGSTheme {
        Box(
            modifier= Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ){
            TransactionList(
                 modifier = Modifier.fillMaxWidth(),
                data = data,
                onClick = {},
                onLongClick = {}
            )
        }
    }

}