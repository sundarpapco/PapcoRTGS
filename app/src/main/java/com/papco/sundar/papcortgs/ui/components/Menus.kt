package com.papco.sundar.papcortgs.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp


data class MenuAction(
    var iconId:Int?=null,
    var imageVector: ImageVector?=null,
    var label:String=""
){
    override fun equals(other: Any?): Boolean {
        return label==(other as MenuAction).label
    }

    override fun hashCode(): Int {
        var result = iconId ?: 0
        result = 31 * result + (imageVector?.hashCode() ?: 0)
        result = 31 * result + label.hashCode()
        return result
    }
}

@Composable
fun OptionsMenu(
    menuItems: List<MenuAction>,
    onItemClick: (String) -> Unit
) {

    val actions = remember(menuItems) { menuItems.filter { it.iconId != null || it.imageVector != null } }
    val overFlowItems =
        remember(menuItems) { menuItems.filter { it.iconId == null && it.imageVector == null } }

    var expanded by rememberSaveable(Unit) { mutableStateOf(false) }

    actions.forEach {
        IconButton(onClick = {
            onItemClick(it.label)
            expanded = false
        }) {
            if (it.imageVector != null)
                Icon(
                    imageVector = it.imageVector!!,
                    contentDescription = it.label,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            else
                Icon(
                    painterResource(id = it.iconId!!),
                    contentDescription = it.label,
                    tint = MaterialTheme.colorScheme.onSurface
                )
        }
    }

    if (overFlowItems.isNotEmpty()) {
        Box(
            modifier = Modifier.wrapContentSize(Alignment.TopEnd)
        ) {
            IconButton(onClick = { expanded = true }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Overflow menu",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            DropdownMenu(
                modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {

                overFlowItems.forEach {
                    MenuItem(text = it.label) {
                        expanded = false
                        onItemClick(it.label)
                    }
                }
            }
        }
    }
}

@Composable
fun<T> ContextMenu(
    identifier:T,
    menuItems: List<MenuAction>,
    onItemClick: (String,T) -> Unit
) {

    val actions = remember { menuItems.filter { it.iconId != null || it.imageVector != null } }
    val overFlowItems =
        remember { menuItems.filter { it.iconId == null && it.imageVector == null } }

    var expanded by rememberSaveable(Unit) { mutableStateOf(false) }

    actions.forEach {
        IconButton(onClick = {
            onItemClick(it.label,identifier)
            expanded = false
        }) {
            if (it.imageVector != null)
                Icon(
                    imageVector = it.imageVector!!,
                    contentDescription = it.label,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            else
                Icon(
                    painterResource(id = it.iconId!!),
                    contentDescription = it.label,
                    tint = MaterialTheme.colorScheme.onSurface
                )
        }
    }

    if (overFlowItems.isNotEmpty()) {
        Box(
            modifier = Modifier.wrapContentSize(Alignment.TopEnd)
        ) {
            IconButton(onClick = { expanded = true }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Overflow menu",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            DropdownMenu(
                modifier=Modifier.background(MaterialTheme.colorScheme.error),
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {

                overFlowItems.forEach {
                    MenuItem(text = it.label) {
                        expanded = false
                        onItemClick(it.label,identifier)
                    }
                }
            }
        }
    }

}

@Composable
fun MenuItem(text: String, onClick: () -> Unit) {

    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .height(48.dp)
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}