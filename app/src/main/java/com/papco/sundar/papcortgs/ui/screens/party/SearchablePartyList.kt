package com.papco.sundar.papcortgs.ui.screens.party

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.papco.sundar.papcortgs.database.pojo.Party
import com.papco.sundar.papcortgs.ui.components.RTGSSearchBar
import com.papco.sundar.papcortgs.ui.theme.RTGSTheme

@Composable
fun SearchablePartyList(
    state:SearchablePartyListState,
    searchHint:String,
    onPartyClicked:(Party)->Unit,
    modifier: Modifier = Modifier,
    onPartyLongClicked:(Party)->Unit={}
){

    val query by state.query.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 16.dp)
    ) {

        Spacer(modifier = Modifier.height(16.dp))

        RTGSSearchBar(modifier = Modifier.fillMaxWidth(),
            query = query,
            placeHolder = searchHint,
            onQueryChange = { state.query.value = it },
            onQueryClear = { state.query.value = "" })

        Spacer(modifier = Modifier.height(16.dp))

        PartyList(list = state.data ?: emptyList(), onClick = onPartyClicked, onLongClick =onPartyLongClicked)

    }
}

@Composable
private fun PartyList(
    list: List<Party>,
    onClick: (Party) -> Unit,
    modifier: Modifier = Modifier,
    onLongClick:(Party)->Unit={}
) {
    LazyColumn(
        modifier = modifier
    ) {
        items(list, key = {
            it.id
        }) {
            PartyListItem(party = it,
                onClick=onClick,
                modifier = Modifier.animateItem(),
                onLongClick=onLongClick
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PartyListItem(
    party: Party,
    onClick:(Party)->Unit,
    modifier: Modifier = Modifier,
    onLongClick: (Party) -> Unit={}
){

    val highlightColor = MaterialTheme.colorScheme.primaryContainer
    val displayName = remember(party){
        party.highlightedName(highlightColor)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .combinedClickable(
                onClick = {
                    onClick(party)
                },
                onLongClick={
                    onLongClick(party)
                }
            ),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium
    ) {

        Text(
            modifier= Modifier.padding(12.dp),
            text = displayName,
            style = MaterialTheme.typography.titleMedium,
            color = if(party.disabled)
            MaterialTheme.colorScheme.outline
                else
            MaterialTheme.colorScheme.onSurface
        )
    }
}


@Preview
@Composable
private fun PreviewPartyListItem(){

    val party = remember{
        Party(1,"Sundaravel","")
    }

    RTGSTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(8.dp)
        ){
            PartyListItem(party = party, onClick = {})
        }
    }

}