package com.papco.sundar.papcortgs.ui

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.common.GMailUtil
import com.papco.sundar.papcortgs.database.transactionGroup.TransactionGroup
import com.papco.sundar.papcortgs.extentions.toast
import com.papco.sundar.papcortgs.screens.backup.DropBoxFragmentVM
import com.papco.sundar.papcortgs.screens.mail.FragmentEmailVM
import com.papco.sundar.papcortgs.screens.mail.MailWorker
import com.papco.sundar.papcortgs.screens.receiver.CreateReceiverVM
import com.papco.sundar.papcortgs.screens.receiver.ReceiverListVM
import com.papco.sundar.papcortgs.screens.sender.CreateSenderVM
import com.papco.sundar.papcortgs.screens.sender.SendersListVM
import com.papco.sundar.papcortgs.screens.sms.FragmentSMSVM
import com.papco.sundar.papcortgs.screens.sms.MessageWorker
import com.papco.sundar.papcortgs.screens.transaction.createTransaction.CreateTransactionVM
import com.papco.sundar.papcortgs.screens.transaction.createTransaction.ReceiverSelectionVM
import com.papco.sundar.papcortgs.screens.transaction.createTransaction.SenderSelectionVM
import com.papco.sundar.papcortgs.screens.transaction.listTransaction.TransactionListVM
import com.papco.sundar.papcortgs.screens.transactionGroup.GroupActivityVM
import com.papco.sundar.papcortgs.screens.transactionGroup.manage.ManageTransactionGroupVM
import com.papco.sundar.papcortgs.ui.backup.BackupScreen
import com.papco.sundar.papcortgs.ui.screens.group.ExcelFileListScreen
import com.papco.sundar.papcortgs.ui.screens.group.ManageGroupScreen
import com.papco.sundar.papcortgs.ui.screens.mail.GmailSignInScreen
import com.papco.sundar.papcortgs.ui.screens.mail.MailScreen
import com.papco.sundar.papcortgs.ui.screens.message.MessageScreen
import com.papco.sundar.papcortgs.ui.screens.party.AddEditPartyScreen
import com.papco.sundar.papcortgs.ui.screens.party.receiver.ManageReceiversScreen
import com.papco.sundar.papcortgs.ui.screens.party.receiver.SelectReceiverScreen
import com.papco.sundar.papcortgs.ui.screens.party.sender.SelectSenderScreen
import com.papco.sundar.papcortgs.ui.screens.party.sender.SenderListScreen
import com.papco.sundar.papcortgs.ui.screens.transaction.ManageTransactionScreen
import com.papco.sundar.papcortgs.ui.screens.transaction.TransactionListScreen
import kotlinx.coroutines.launch

@Composable
fun AppUI() {

    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = ExcelFileList) {

        composable<ExcelFileList> {
            val viewModel = remember { ViewModelProvider(it)[GroupActivityVM::class.java] }
            ExcelFileListScreen(state = viewModel.screenState,
                onExcelFileClicked = {
                    navController.navigate(
                        TransactionList(
                            it.transactionGroup.id,
                            it.transactionGroup.name,
                            it.transactionGroup.defaultSenderId
                        )
                    )
                },
                onExcelFileLongClicked = { navController.navigate(ManageGroup(it.transactionGroup.id)) },
                onAddExcelFileClicked = { navController.navigate(ManageGroup(-1)) },
                navigateToSendersScreen = { navController.navigate(SendersList) },
                navigateToReceiversScreen = { navController.navigate(ReceiversList) },
                navigateToMessageFormatScreen = { },
                navigateToDropBaxBackupScreen = {navController.navigate(DropBox)}
            )
        }

        composable<ManageGroup> { backstackEntry ->
            val args = backstackEntry.toRoute<ManageGroup>()
            val title = remember {
                if (args.groupId != -1) context.getString(R.string.update_xl_file)
                else context.getString(R.string.create_xl_file)
            }
            val viewModel =
                remember { ViewModelProvider(backstackEntry)[ManageTransactionGroupVM::class.java] }
            var isAlreadyLoaded = rememberSaveable { false }

            ManageGroupScreen(title = title,
                state = viewModel.screenState,
                onSave = { if (args.groupId != -1) viewModel.updateGroup() else viewModel.addGroup() },
                onCancel = { navController.popBackStack() },
                onBackPressed = { navController.popBackStack() },
                onDelete = { viewModel.deleteGroup(args.groupId) })

            if(args.groupId != -1)
                LaunchedEffect(key1 = true) {
                    if(!isAlreadyLoaded) {
                        viewModel.loadTransactionGroup(args.groupId)
                        isAlreadyLoaded = true
                    }
                }


            LaunchedEffect(key1 = true) {
                viewModel.event.collect{
                    it?.let{event ->
                        if(!event.isAlreadyHandled){
                            event.handleEvent()
                            navController.popBackStack()
                        }
                    }
                }
            }
        }

        composable<TransactionList> { backStackEntry ->
            val viewModel =
                remember { ViewModelProvider(backStackEntry)[TransactionListVM::class.java] }
            val args = backStackEntry.toRoute<TransactionList>()
            var dataLoaded = rememberSaveable { false }
            val transactionGroup = remember {
                TransactionGroup().apply {
                    id = args.groupId
                    name = args.groupName
                    defaultSenderId = args.defaultSenderId
                }
            }
            val gmailUtil = remember{GMailUtil(context)}

            TransactionListScreen(title = args.groupName,
                screenState = viewModel.screenState,
                onBackPressed = { navController.popBackStack() },
                onClick = {
                    navController.navigate(
                        ManageTransaction(
                            args.groupId, it.id, args.defaultSenderId
                        )
                    )
                },
                onAddTransaction = {
                    navController.navigate(
                        ManageTransaction(
                            args.groupId, -1, args.defaultSenderId
                        )
                    )
                },
                onDelete = { viewModel.deleteTransaction(it) },
                onExportManualRTGSFile = {
                    viewModel.createManualExportFile(transactionGroup, it)
                },
                onExportAutoRTGSFile = {
                    if (viewModel.screenState.transactions.isNotEmpty()) viewModel.createAutoXlFile(
                        transactionGroup, it
                    )
                    else Toast.makeText(
                        context,
                        context.getString(R.string.add_at_least_one_transaction_to_export),
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onDispatchMessages = {
                    navController.navigate(MessageList(transactionGroup.id))
                },
                onDispatchMails = {
                    if(!gmailUtil.isConnected())
                        navController.navigate(
                            GoogleSignIn(
                                transactionGroup.id,
                                transactionGroup.name,
                                transactionGroup.defaultSenderId
                            )
                        )
                    else
                        navController.navigate(
                            EmailList(
                                transactionGroup.id,
                                transactionGroup.name,
                                transactionGroup.defaultSenderId
                            )
                        )
                },
                onShareFile = { viewModel.shareFile(context, it) })

            LaunchedEffect(true) {
                if (!dataLoaded)
                    viewModel.loadTransactions(args.groupId)
                dataLoaded = true
            }

            LaunchedEffect(key1 = true) {
                viewModel.reportGenerated.collect { it ->
                    it?.let { event ->
                        if (event.isAlreadyHandled) return@collect
                        val fileName = event.handleEvent()
                        if (fileName.isEmpty()) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.error_in_creating_the_excel_file),
                                Toast.LENGTH_LONG
                            ).show()
                        }else {
                            viewModel.screenState.showReportGeneratedDialog(fileName)
                        }
                    }
                }

            }
        }

        composable<ManageTransaction> {backStackEntry->
            val args = backStackEntry.toRoute<ManageTransaction>()
            val viewModel = remember {ViewModelProvider(backStackEntry)[CreateTransactionVM::class.java] }
            val isEditingMode = remember { args.transactionId != -1 }
            val title = remember {
                if(isEditingMode) context.getString(R.string.update_transaction) else context.getString(R.string.create_transaction)
            }
            var isAlreadyLoaded = rememberSaveable { false}

            ManageTransactionScreen(
                screenState = viewModel.screenState,
                title = title,
                onSenderClicked = {
                      if(viewModel.screenState.selectedSender!=null)
                          navController.navigate(SelectSender)
                },
                onReceiverClicked = {
                    if(viewModel.screenState.selectedReceiver!=null)
                        navController.navigate(SelectReceiver(args.groupId))
                },
                onSave = {
                     if(viewModel.screenState.validate(context)){
                         if(isEditingMode)
                             viewModel.updateTransaction(args.groupId,args.transactionId)
                         else
                             viewModel.saveNewTransaction(args.groupId)
                     }
                },
                onDismiss = {navController.popBackStack()}
            )

            LaunchedEffect(key1 = true) {

                backStackEntry.savedStateHandle.getStateFlow("selectedSender",-1)
                    .collect{
                        if(it!=-1){
                            viewModel.selectSender(it)
                            backStackEntry.savedStateHandle["selectedSender"] = -1
                        }
                    }
            }

            LaunchedEffect(key1 = true) {
                backStackEntry.savedStateHandle.getStateFlow("selectedReceiver",-1)
                    .collect{
                        if(it!=-1){
                            viewModel.selectReceiver(it)
                            backStackEntry.savedStateHandle["selectedReceiver"] = -1
                        }
                    }
            }

            LaunchedEffect(key1 = true) {
                viewModel.navigateBack.collect{needToGoBack->
                    if(needToGoBack)
                        navController.popBackStack()
                }
            }

            LaunchedEffect(key1 = true) {
                if(!isAlreadyLoaded)
                    if(isEditingMode)
                        viewModel.loadTransaction(args.transactionId)
                    else
                        viewModel.createBlankTransaction(args.groupId,args.defaultSenderId)

                isAlreadyLoaded=true
            }

        }

        composable<SendersList> { backstackEntry ->
            val viewModel = remember {
                ViewModelProvider(backstackEntry)[SendersListVM::class.java]
            }

            SenderListScreen(state = viewModel.screenState,
                onSenderClicked ={navController.navigate(ManageSender(it.id))},
                onBackPressed = { navController.popBackStack() },
                onAddNewSender = { navController.navigate(ManageSender(-1)) },
                onDeleteSender = {viewModel.deleteSender(it)}
            )
        }

        composable<ManageSender> {backStackEntry->
            val args=backStackEntry.toRoute<ManageSender>()
            val viewModel = remember{ViewModelProvider(backStackEntry)[CreateSenderVM::class.java]}
            val isEditingMode = remember{args.senderId != -1}
            var isAlreadyLoaded = remember{ false }

            AddEditPartyScreen(
                title = if(isEditingMode)
                    context.getString(R.string.update_sender)
                else
                    context.getString(R.string.create_sender),
                state = viewModel.screenState,
                onBackPressed = { navController.popBackStack() },
                onFormSubmit = {
                    if(isEditingMode) {
                        val sender = viewModel.screenState.asSender(args.senderId)
                        viewModel.updateSender(sender)
                    }
                    else {
                        val sender = viewModel.screenState.asSender()
                        viewModel.addSender(sender)
                    }
                }
            )

            if(isEditingMode)
                LaunchedEffect(key1 = true) {
                    if(!isAlreadyLoaded)
                        viewModel.loadSender(args.senderId)
                    isAlreadyLoaded=true
                }

            LaunchedEffect(key1 = true) {
                viewModel.eventStatus.collect{
                    it?.let{event->
                        if(!event.isAlreadyHandled){
                            val msg=event.handleEvent()
                            if(msg==CreateSenderVM.EVENT_SUCCESS){
                                navController.popBackStack()
                            }else{
                                context.toast(msg)
                            }
                        }
                    }
                }
            }
        }

        composable<SelectSender> {backstackEntry->
            val viewModel = remember{ViewModelProvider(backstackEntry)[SenderSelectionVM::class.java]}

            SelectSenderScreen(
                state = viewModel.screenState,
                onSenderClicked = {
                    navController.previousBackStackEntry?.savedStateHandle?.set("selectedSender",it.id)
                    navController.popBackStack()
                },
                onBackPressed = {navController.popBackStack()}
            )
        }

        composable<ReceiversList> { backstackEntry ->
            val viewModel = remember {
                ViewModelProvider(backstackEntry)[ReceiverListVM::class.java]
            }

            ManageReceiversScreen(state = viewModel.screenState,
                onReceiverClicked ={navController.navigate(ManageReceiver(it.id))},
                onBackPressed = { navController.popBackStack() },
                onAddNewReceiver = { navController.navigate(ManageReceiver(-1)) },
                onDeleteReceiver = {viewModel.deleteReceiver(it)}
            )
        }

        composable<ManageReceiver> {backStackEntry->
            val args=backStackEntry.toRoute<ManageReceiver>()
            val viewModel = remember{ViewModelProvider(backStackEntry)[CreateReceiverVM::class.java]}
            val isEditingMode = remember{args.receiverId != -1}
            var isAlreadyLoaded = rememberSaveable {false}

            AddEditPartyScreen(
                title = if(isEditingMode)
                    context.getString(R.string.update_receiver)
                else
                    context.getString(R.string.create_receiver),
                state = viewModel.screenState,
                onBackPressed = { navController.popBackStack() },
                onFormSubmit = {
                    if(isEditingMode) {
                        val receiver = viewModel.screenState.asReceiver(args.receiverId)
                        viewModel.updateReceiver(receiver)
                    }
                    else {
                        val receiver = viewModel.screenState.asReceiver()
                        viewModel.addReceiver(receiver)
                    }
                }
            )

            if(isEditingMode)
                LaunchedEffect(key1 = true) {
                    if(!isAlreadyLoaded)
                        viewModel.loadReceiver(args.receiverId)
                    isAlreadyLoaded=true
                }

            LaunchedEffect(key1 = true) {
                viewModel.eventStatus.collect{
                    it?.let{event->
                        if(!event.isAlreadyHandled){
                            val msg=event.handleEvent()
                            if(msg==CreateSenderVM.EVENT_SUCCESS){
                                navController.popBackStack()
                            }else{
                                context.toast(msg)
                            }
                        }
                    }
                }
            }
        }

        composable<SelectReceiver> {navBackStackEntry ->
            val args = navBackStackEntry.toRoute<SelectReceiver>()
            val viewModel = remember{ViewModelProvider(navBackStackEntry)[ReceiverSelectionVM::class.java]}
            var isAlreadyLoaded = rememberSaveable{false}

            SelectReceiverScreen(state = viewModel.screenState,
                onReceiverClicked = {
                    if(it.disabled){
                        context.toast(R.string.this_beneficiary_already_added)
                    }else{
                        navController.previousBackStackEntry?.savedStateHandle?.set("selectedReceiver",it.id)
                        navController.popBackStack()
                    }
                },
                onBackPressed = {navController.popBackStack()}
            )

            LaunchedEffect(key1 = true) {
                if(!isAlreadyLoaded)
                    viewModel.loadReceivers(args.groupId)
                isAlreadyLoaded=true
            }

        }

        composable<DropBox> {backstackEntry->
            val viewmodel = remember{ViewModelProvider(backstackEntry)[DropBoxFragmentVM::class.java]}

           BackupScreen(
               screenState = viewmodel.screenState,
               onLink = { viewmodel.linkToDropBox()},
               onUnlink = { viewmodel.unlinkFromDropBox() },
               onBackup = { viewmodel.backupFile() },
               onRestore = { viewmodel.restoreBackup() },
               onBackPressed = {navController.popBackStack()}
           )

            LifecycleResumeEffect(Unit) {
                viewmodel.refreshDropBoxConnection()
                onPauseOrDispose {}
            }
        }

        composable<GoogleSignIn> {backstackEntry->

            val args = backstackEntry.toRoute<GoogleSignIn>()
            GmailSignInScreen(
                onConnected = { navController.navigate(EmailList(args.groupId,args.groupName,args.defaultSenderId)) },
                onBackPressed = {navController.popBackStack()}
            )
        }

        composable<EmailList> {backstackEntry->
            val args=backstackEntry.toRoute<EmailList>()
            val viewModel = remember {
                ViewModelProvider(backstackEntry)[FragmentEmailVM::class.java]
            }
            val coroutineScope = rememberCoroutineScope()
            val gmail = remember { GMailUtil(context) }
            var isAlreadyLoaded = rememberSaveable { false }

            MailScreen(
                screenState = viewModel.screenState,
                onSendMails = { MailWorker.startWith(context,args.groupId) },
                onBackPressed = { navController.popBackStack(TransactionList(args.groupId,args.groupName,args.defaultSenderId),false) },
                onSignOut = {
                    coroutineScope.launch{
                        if(gmail.signOut())
                            navController.popBackStack(
                                TransactionList(
                                    args.groupId,
                                    args.groupName,
                                    args.defaultSenderId
                                ),false
                            )
                    }
                }
            )

            LaunchedEffect(key1 = true) {
                if(!isAlreadyLoaded)
                    viewModel.loadEmailList(args.groupId)

                isAlreadyLoaded = true
            }

        }

        composable<MessageList> {backstackEntry->
            val args=backstackEntry.toRoute<MessageList>()
            val viewModel = remember{ViewModelProvider(backstackEntry)[FragmentSMSVM::class.java]}
            var isAlreadyLoaded = rememberSaveable { false }

            MessageScreen(
                screenState = viewModel.screenState,
                onSendMessages = { MessageWorker.startWith(context,args.groupId) },
                onBackPressed = {navController.popBackStack()}
            )

            LaunchedEffect(true){
                if(!isAlreadyLoaded)
                    viewModel.loadMessagingList(args.groupId)

                isAlreadyLoaded = true
            }

        }

    }
}