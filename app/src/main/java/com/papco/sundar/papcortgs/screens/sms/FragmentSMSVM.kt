package com.papco.sundar.papcortgs.screens.sms

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.papco.sundar.papcortgs.database.common.MasterDatabase
import com.papco.sundar.papcortgs.database.pojo.CohesiveTransaction
import com.papco.sundar.papcortgs.screens.mail.MailWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class FragmentSMSVM(application: Application) : AndroidViewModel(application) {

    private val db = MasterDatabase.getInstance(application)

    private var _messaginglList: MutableLiveData<List<CohesiveTransaction>> =
        MutableLiveData(emptyList())
    val messagingList: LiveData<List<CohesiveTransaction>> = _messaginglList
    val isIntimationRunning= MediatorLiveData(false)

    init{
        isIntimationRunning.addSource(MessageWorker.getWorkStatusLiveData(application)) { workInfo ->
            isIntimationRunning.value = workInfo?.let {
                it.isNotEmpty() && it.first().state == WorkInfo.State.RUNNING
            } ?: false
        }
    }

    private var alreadyLoaded: Boolean = false

    fun loadMessagingList(groupId: Int) {

        if (alreadyLoaded) return else alreadyLoaded = true

        viewModelScope.launch {
            db.transactionDao
                .getAllCohesiveTransactionsOfGroup(groupId)
                .collect {
                    _messaginglList.value = it
                }

        }
    }
}
