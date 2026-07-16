package com.papco.sundar.papcortgs.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.ui.theme.RTGSTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RTGSAppBar(
    title:String,
    modifier:Modifier=Modifier,
    subtitle:String?=null,
    isBackEnabled:Boolean=false,
    titleStyle: TextStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    onBackPressed:()->Unit={},
    optionsMenu:(@Composable ()->Unit)={}
){
    TopAppBar(
        modifier = modifier
            .shadow(3.dp),
        title = {
            Column {
                Text(
                    text = title,
                    style = titleStyle
                )

                subtitle?.let{
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

        },
        navigationIcon = {
            if(isBackEnabled)
                IconButton(onClick = onBackPressed) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back),
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
            painter = painterResource(R.drawable.ic_done),
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
            subtitle = "34 Receivers",
            isBackEnabled = true
        ) {
            OptionsMenu(menuItems = menuActions) {

            }
        }
    }

}