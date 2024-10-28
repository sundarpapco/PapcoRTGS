package com.papco.sundar.papcortgs.screens.mail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.papco.sundar.papcortgs.database.common.MasterDatabase
import com.papco.sundar.papcortgs.ui.screens.mail.MailScreenState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FragmentEmailVM(application: Application) : AndroidViewModel(application) {

    private val db = MasterDatabase.getInstance(application)
    val screenState = MailScreenState()

    private var alreadyLoaded: Boolean = false

    fun loadEmailList(groupId: Int) {

        if (alreadyLoaded) return else alreadyLoaded = true

        val currentLogin = GoogleSignIn.getLastSignedInAccount(getApplication())
        screenState.loggedInGmail= currentLogin?.email

        viewModelScope.launch {
            db.transactionDao
                .getAllCohesiveTransactionsOfGroup(groupId)
                .collect {
                    screenState.transactions=it
                }
        }

        viewModelScope.launch(Dispatchers.IO) {
            MailWorker.getWorkStatusFlow(getApplication(),groupId)
               .collectLatest {workInformations->
                   if(workInformations.isNotEmpty()){
                       screenState.dispatcherState=workInformations.first().state
                   }
               }
        }

    }
}
