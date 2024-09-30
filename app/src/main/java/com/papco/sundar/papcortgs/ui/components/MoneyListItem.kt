package com.papco.sundar.papcortgs.ui.components

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.papco.sundar.papcortgs.ui.theme.RTGSTheme

@Composable
fun MoneyListItem(modifier: Modifier = Modifier,title:String, subtitle:String, amount: String){
    Surface(
        modifier= modifier
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier=Modifier.fillMaxWidth().height(IntrinsicSize.Max).padding(16.dp)
        ) {
            Column(Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(text = subtitle, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Box(
                modifier=Modifier.fillMaxHeight(),
                contentAlignment = Alignment.Center
            ){
                Text(
                    text = amount,
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}

@Preview
@Composable
private fun TestMoneyListItem(){

    RTGSTheme {
        MoneyListItem(title = "SUNDARAVEL", subtitle = "Papco offset printing works", amount = "Rs.12,345")
    }

}