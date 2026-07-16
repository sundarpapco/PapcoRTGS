package com.papco.sundar.papcortgs.ui.screens.party.sender

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.database.pojo.Party
import com.papco.sundar.papcortgs.screens.transaction.createTransaction.SenderSelectionVM
import com.papco.sundar.papcortgs.ui.SelectSender
import com.papco.sundar.papcortgs.ui.components.RTGSAppBar
import com.papco.sundar.papcortgs.ui.screens.LoadingScreen
import com.papco.sundar.papcortgs.ui.screens.party.SearchablePartyList
import com.papco.sundar.papcortgs.ui.screens.party.SearchablePartyListState
import com.papco.sundar.papcortgs.ui.theme.RTGSTheme
import com.papco.sundar.papcortgs.ui.util.ResultEventBus

fun EntryProviderScope<NavKey>.selectSenderEntry(
    backStack: NavBackStack<NavKey>,
    resultEventBus: ResultEventBus
) {

    entry<SelectSender> {
        val viewModel: SenderSelectionVM = viewModel()

        SelectSenderScreen(
            state = viewModel.screenState,
            onSenderClicked = {
                resultEventBus.send("selectedSender", it.id)
                backStack.removeLastOrNull()
            },
            onBackPressed = { backStack.removeLastOrNull() }
        )
    }
}

@Composable
fun SelectSenderScreen(
    state: SearchablePartyListState,
    onSenderClicked: (Party) -> Unit,
    onBackPressed: () -> Unit
) {
    Scaffold(topBar = {

        RTGSAppBar(
            title = stringResource(id = R.string.select_sender),
            isBackEnabled = true,
            onBackPressed = onBackPressed,
            subtitle = state.data?.let {
                stringResource(id = R.string.xx_senders, it.size)
            })
    }) { paddingValues ->

        if (state.data == null)
            LoadingScreen()
        else
            SearchablePartyList(
                modifier = Modifier.padding(paddingValues),
                state = state,
                onPartyClicked = onSenderClicked,
                searchHint = stringResource(id = R.string.search_senders),
                onPartyLongClicked = {}
            )
    }

}

@Preview
@Composable
private fun PreviewScreen(){

    val screenState = remember { SearchablePartyListState().apply {
        data = listOf(
            Party(id=1, name = "Sundaravel", highlightWord = "")
        )
    } }

    RTGSTheme {
        SelectSenderScreen(
            state = screenState,
            onSenderClicked = {},
            onBackPressed = {}
        )
    }

}