package com.papco.sundar.papcortgs.ui.screens.party.receiver

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.database.pojo.Party
import com.papco.sundar.papcortgs.ui.components.RTGSAppBar
import com.papco.sundar.papcortgs.ui.screens.LoadingScreen
import com.papco.sundar.papcortgs.ui.screens.party.SearchablePartyList
import com.papco.sundar.papcortgs.ui.screens.party.SearchablePartyListState

@Composable
fun SelectReceiverScreen(
    state: SearchablePartyListState,
    onReceiverClicked: (Party) -> Unit,
    onBackPressed: () -> Unit
) {
    Scaffold(topBar = {

        RTGSAppBar(
            title = stringResource(id = R.string.select_receiver),
            isBackEnabled = true,
            onBackPressed = onBackPressed,
            subtitle = state.data?.let {
                stringResource(id = R.string.xx_receivers, it.size)
            })
    }) { paddingValues ->

        if (state.data == null) LoadingScreen()
        else SearchablePartyList(modifier = Modifier.padding(paddingValues),
            state = state,
            onPartyClicked = onReceiverClicked,
            searchHint = stringResource(id = R.string.search_receivers),
            onPartyLongClicked = {}
        )
    }

}