package com.papco.sundar.papcortgs.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.papco.sundar.papcortgs.ui.theme.RTGSTheme
import com.sivakasi.papco.jobflow.ui.MenuAction
import com.sivakasi.papco.jobflow.ui.OptionsMenu

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RTGSAppBar(
    title:String,
    modifier:Modifier=Modifier,
    isBackEnabled:Boolean=false,
    onBackPressed:()->Unit={},
    optionsMenu:(@Composable ()->Unit)={}
){
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
            )
        },
        navigationIcon = {
            if(isBackEnabled)
                IconButton(onClick = onBackPressed) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription ="Back"
                    )
                }
        },
        actions ={ optionsMenu()}

    )
}


@Preview
@Composable
private fun TopAppBarPreview(){

    val menuActions=listOf(
        MenuAction(
            imageVector = Icons.Filled.Done,
            label = "Done"
        ),
        MenuAction(
            label = "Cancel"
        ),
        MenuAction(
            label = "Delete"
        )
    )

    RTGSTheme {
        RTGSAppBar(
            title = "Create Sender",
            isBackEnabled = true,
            optionsMenu = {
                OptionsMenu(menuItems = menuActions) {
                    
                }
            }
        )
    }

}