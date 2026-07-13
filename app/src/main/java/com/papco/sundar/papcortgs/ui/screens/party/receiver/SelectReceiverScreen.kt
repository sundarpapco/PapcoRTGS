package com.papco.sundar.papcortgs.ui.screens.party.receiver

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.database.pojo.Party
import com.papco.sundar.papcortgs.extentions.toast
import com.papco.sundar.papcortgs.screens.transaction.createTransaction.ReceiverSelectionVM
import com.papco.sundar.papcortgs.ui.SelectReceiver
import com.papco.sundar.papcortgs.ui.components.RTGSAppBar
import com.papco.sundar.papcortgs.ui.screens.LoadingScreen
import com.papco.sundar.papcortgs.ui.screens.party.SearchablePartyList
import com.papco.sundar.papcortgs.ui.screens.party.SearchablePartyListState
import com.papco.sundar.papcortgs.ui.util.ResultEventBus

fun EntryProviderScope<NavKey>.selectReceiverEntry(
    backStack: NavBackStack<NavKey>,
    resultEventBus: ResultEventBus
) {

    entry<SelectReceiver> {key->

        val viewModel : ReceiverSelectionVM = viewModel()
        var isAlreadyLoaded = rememberSaveable { false }
        val context = LocalContext.current

        SelectReceiverScreen(
            state = viewModel.screenState,
            onReceiverClicked = {
                if (it.disabled) {
                    context.toast(R.string.this_beneficiary_already_added)
                } else {
                    resultEventBus.send("selectedReceiver",it.id)
                    backStack.removeLastOrNull()
                }
            },
            onBackPressed = { backStack.removeLastOrNull()}
        )

        LaunchedEffect(key1 = true) {
            if (!isAlreadyLoaded)
                viewModel.loadReceivers(key.groupId)
            isAlreadyLoaded = true
        }
    }
}

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