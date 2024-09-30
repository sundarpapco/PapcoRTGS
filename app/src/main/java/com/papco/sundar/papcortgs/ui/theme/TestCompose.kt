package com.papco.sundar.papcortgs.ui.theme

import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun TestComposable(){
    Button(onClick = { /*TODO*/ }) {
        Text(
            text = "Test Button"
        )
    }
}

@Preview
@Composable
fun TestButton(){
    RTGSTheme {
       TestComposable()
    }
}