package com.papco.sundar.papcortgs.screens.mail

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
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
                       Log.d("SAAT","Mail Worker detected")
                       when (workInformations.first().state){
                           WorkInfo.State.ENQUEUED -> {
                               Log.d("SAAT","Worker Enqueued...")
                           }
                           WorkInfo.State.RUNNING -> {
                               Log.d("SAAT","Worker Running...")
                           }
                           WorkInfo.State.SUCCEEDED -> {
                               Log.d("SAAT","Worker Succeeded...")
                           }
                           WorkInfo.State.FAILED -> {
                               Log.d("SAAT","Worker Failed...")
                           }
                           WorkInfo.State.BLOCKED -> {
                               Log.d("SAAT","Worker Blocked...")
                           }
                           WorkInfo.State.CANCELLED -> {
                               Log.d("SAAT","Worker Cancelled...")
                           }
                       }


                       screenState.dispatcherState=workInformations.first().state
                   }
               }
        }

    }
}
