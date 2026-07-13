package com.papco.sundar.papcortgs.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.papco.sundar.papcortgs.ui.MainScreen
import com.papco.sundar.papcortgs.ui.theme.RTGSTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.main_activity)
        setContent {
            RTGSTheme {
                MainScreen()
            }
        }
    }
}