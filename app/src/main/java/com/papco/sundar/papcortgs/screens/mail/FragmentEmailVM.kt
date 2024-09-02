package com.papco.sundar.papcortgs.screens.mail

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.papco.sundar.papcortgs.database.common.MasterDatabase
import com.papco.sundar.papcortgs.database.pojo.CohesiveTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FragmentEmailVM(application: Application) : AndroidViewModel(application) {

    private val db = MasterDatabase.getInstance(application)

    private var _emailList: MutableLiveData<List<CohesiveTransaction>> =
        MutableLiveData(emptyList())
    val emailList: LiveData<List<CohesiveTransaction>> = _emailList
    val isIntimationRunning=MediatorLiveData(false)

    init{
        isIntimationRunning.addSource(MailWorker.getWorkStatusLiveData(application)) { workInfo ->
            isIntimationRunning.value = workInfo?.let {
                it.isNotEmpty() && it.first().state == WorkInfo.State.RUNNING
            } ?: false
        }
    }

    private var alreadyLoaded: Boolean = false

    fun loadEmailList(groupId: Int) {

        if (alreadyLoaded) return else alreadyLoaded = true

        viewModelScope.launch {
            db.transactionDao
                .getAllCohesiveTransactionsOfGroup(groupId)
                .collect {
                    _emailList.value = it
                }

        }
    }
}
