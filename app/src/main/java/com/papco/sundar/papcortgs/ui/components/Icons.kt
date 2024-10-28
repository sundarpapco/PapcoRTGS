package com.papco.sundar.papcortgs.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.papco.sundar.papcortgs.ui.theme.RTGSTheme

@Composable
fun SuccessIcon(
    modifier:Modifier = Modifier,
    text:String,
    roundColor:Color = Color.Green,
    iconColor:Color=Color.White
){
    Column(
        modifier=modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier= Modifier
                .size(25.dp)
                .clip(RoundedCornerShape(100))
                .background(roundColor)
                .padding(4.dp)
        ){
            Icon(
                modifier=Modifier.fillMaxSize(),
                imageVector = Icons.Filled.Done,
                contentDescription = "Success",
                tint = iconColor
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = roundColor
        )
    }

}

@Composable
fun ErrorIcon(
    modifier:Modifier = Modifier,
    text:String,
    roundColor:Color = MaterialTheme.colorScheme.error,
    iconColor:Color=Color.White
){
    Column(
        modifier=modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier= Modifier
                .size(25.dp)
                .clip(RoundedCornerShape(100))
                .background(roundColor)
                .padding(4.dp)
        ){
            Icon(
                modifier=Modifier.fillMaxSize(),
                imageVector = Icons.Filled.Close,
                contentDescription = "Success",
                tint = iconColor
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = roundColor
        )
    }

}



@Preview
@Composable
private fun PreviewSuccessIcon(){
    RTGSTheme {
        SuccessIcon(
            text = "MAIL SENT"
        )
    }
}

@Preview
@Composable
private fun PreviewErrorIcon(){
    RTGSTheme {
        ErrorIcon(
            text = "ERROR"
        )
    }
}

