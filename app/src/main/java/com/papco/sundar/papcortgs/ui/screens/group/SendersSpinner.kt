package com.papco.sundar.papcortgs.ui.screens.group

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.database.pojo.Party
import com.papco.sundar.papcortgs.ui.components.MenuItem
import com.papco.sundar.papcortgs.ui.theme.RTGSTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendersSpinner(
    selectedSender: Party?,
    senders: List<Party>,
    onSenderClicked: (Party) -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember {
        mutableStateOf(false)
    }

    Box(modifier = modifier) {

        var dropDownWidth by remember { mutableStateOf(0) }
        var rippleColour = MaterialTheme.colorScheme.primary
        val myRipple = remember {
            RippleConfiguration(
                rippleColour,
                RippleAlpha(0.1f, 0.1f, 0.08f, 0.1f)
            )
        }

        CompositionLocalProvider(LocalRippleConfiguration provides myRipple ) {
            OutlinedTextField(modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { dropDownWidth = it.width }
                .clickable {
                    menuExpanded = !menuExpanded
                },
                value = selectedSender?.name ?: stringResource(id = R.string.please_add_a_sender_first),
                onValueChange = {},
                label = {
                    Text(text = stringResource(id = R.string.sender))
                },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = LocalContentColor.current,
                    disabledBorderColor = MaterialTheme.colorScheme.primary,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurface,
                    disabledLabelColor = MaterialTheme.colorScheme.primary
                ),
                readOnly = true,
                singleLine = true,
                enabled = false,
                trailingIcon = {
                    if (menuExpanded) Icon(
                        imageVector = Icons.Filled.KeyboardArrowUp,
                        contentDescription = "Close drop down menu"
                    )
                    else Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Open drop down menu"
                    )
                })
        }

        DropdownMenu(
            modifier = Modifier
                .width(with(LocalDensity.current) { dropDownWidth.toDp() })
                .background(MaterialTheme.colorScheme.surface),
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false }
        ) {
            senders.forEach {
                MenuItem(text = it.name) {
                    menuExpanded=false
                    onSenderClicked(it)
                }
            }
        }

    }
}

@Preview
@Composable
private fun PreviewSpinner(){

    val senders = remember {
        listOf(
            Party(
                id=1,
                name = "Papco offset private limited",
                highlightWord = ""
            ),

            Party(
                id=2,
                name = "Papco offset printing works",
                highlightWord = ""
            )
        )
    }

    var selectedSender:Party? by remember {
        mutableStateOf(null)
    }


    RTGSTheme {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ){

            SendersSpinner(
                selectedSender = selectedSender ,
                senders = senders,
                onSenderClicked = {selectedSender=it}
            )

        }

    }

}