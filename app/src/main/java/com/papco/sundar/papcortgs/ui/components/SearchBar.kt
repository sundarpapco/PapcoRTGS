package com.papco.sundar.papcortgs.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.ui.theme.RTGSTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RTGSSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeHolder: String? = null,
    onQueryClear: () -> Unit = {}
) {
    SearchBar(
        modifier = modifier,
        inputField = {
            SearchBarDefaults.InputField(query = query,
                onQueryChange = onQueryChange,
                onSearch = {},
                expanded = false,
                onExpandedChange = {},
                leadingIcon = {
                    Icon(painter = painterResource(R.drawable.ic_search), contentDescription = "Search")
                },
                trailingIcon = {
                    if (query.isNotBlank()) IconButton(onClick = onQueryClear) {
                        Icon(painter = painterResource(R.drawable.ic_close), contentDescription = "Close")
                    }
                },
                placeholder = if (placeHolder != null) {
                    {
                        Text(text = placeHolder)
                    }
                } else null)
        },
        colors = SearchBarDefaults.colors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
        ),
        expanded = false,
        windowInsets = WindowInsets(0,0,0,0),
        onExpandedChange = {}) {

    }
}

@Preview
@Composable
private fun PreviewSearchBar() {

    var query: String by remember { mutableStateOf("") }

    RTGSTheme {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopStart
        ) {
            RTGSSearchBar(modifier = Modifier.fillMaxWidth(), query = query, onQueryChange = {
                query = it
            }, onQueryClear = {
                query = ""
            }, placeHolder = "Search Receiver"
            )
        }
    }
}