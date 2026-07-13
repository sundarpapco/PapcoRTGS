package com.papco.sundar.papcortgs.ui

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.papco.sundar.papcortgs.ui.backup.dropBoxScreenEntry
import com.papco.sundar.papcortgs.ui.screens.group.excelFileListEntry
import com.papco.sundar.papcortgs.ui.screens.group.manageGroupEntry
import com.papco.sundar.papcortgs.ui.screens.mail.emailListEntry
import com.papco.sundar.papcortgs.ui.screens.mail.googleSignInEntry
import com.papco.sundar.papcortgs.ui.screens.message.messageListEntry
import com.papco.sundar.papcortgs.ui.screens.party.manageReceiverEntry
import com.papco.sundar.papcortgs.ui.screens.party.manageSenderEntry
import com.papco.sundar.papcortgs.ui.screens.party.receiver.receiversListScreenEntry
import com.papco.sundar.papcortgs.ui.screens.party.receiver.selectReceiverEntry
import com.papco.sundar.papcortgs.ui.screens.party.sender.selectSenderEntry
import com.papco.sundar.papcortgs.ui.screens.party.sender.sendersListScreenEntry
import com.papco.sundar.papcortgs.ui.screens.transaction.manageTransactionScreenEntry
import com.papco.sundar.papcortgs.ui.screens.transaction.transactionListEntry
import com.papco.sundar.papcortgs.ui.util.rememberResultEventBus

@Composable
fun MainScreen() {

    val backStack = rememberNavBackStack(ExcelFileList)
    val resultBus = rememberResultEventBus()

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        predictivePopTransitionSpec = {
            slideInHorizontally { -it } + fadeIn() togetherWith
                    slideOutHorizontally { it } + fadeOut()
        },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            excelFileListEntry(backStack)
            manageGroupEntry(backStack)
            transactionListEntry(backStack)
            manageTransactionScreenEntry(backStack,resultBus)
            sendersListScreenEntry(backStack)
            receiversListScreenEntry(backStack)
            manageSenderEntry(backStack)
            manageReceiverEntry(backStack)
            selectSenderEntry(backStack,resultBus)
            selectReceiverEntry(backStack,resultBus)
            googleSignInEntry(backStack)
            emailListEntry(backStack)
            messageListEntry(backStack)
            dropBoxScreenEntry(backStack)
        }
    )

}

fun NavBackStack<NavKey>.popUntil(inclusive: Boolean = false, predicate: (NavKey) -> Boolean) {
    val index = this.indexOfFirst(predicate)

    if (index == -1)
        return

    if (inclusive) {
        this.subList(index, this.size).clear()
    } else {
        if (index == this.size - 1)
            return
        else
            this.subList(index + 1, this.size).clear()
    }

}
