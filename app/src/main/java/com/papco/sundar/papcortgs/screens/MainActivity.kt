package com.papco.sundar.papcortgs.screens

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.ui.AppUI
import com.papco.sundar.papcortgs.ui.theme.RTGSTheme
import kotlinx.serialization.Serializable

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.main_activity)
        setContent {
            RTGSTheme {
                AppUI()
            }
        }
    }
}