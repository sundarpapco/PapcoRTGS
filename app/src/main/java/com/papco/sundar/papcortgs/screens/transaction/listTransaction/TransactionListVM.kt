package com.papco.sundar.papcortgs.screens.transaction.listTransaction

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.common.Event
import com.papco.sundar.papcortgs.database.common.MasterDatabase
import com.papco.sundar.papcortgs.database.transactionGroup.TransactionGroup
import com.papco.sundar.papcortgs.reports.BizzPay360Report
import com.papco.sundar.papcortgs.reports.ChequeRTGSReport
import com.papco.sundar.papcortgs.ui.screens.transaction.TransactionListScreenState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.io.File

class TransactionListVM(application: Application) : AndroidViewModel(application) {

    private val db: MasterDatabase = MasterDatabase.getInstance(application)

    private var isAlreadyLoaded=false
    private val _reportGenerated:MutableStateFlow<Event<String>?> = MutableStateFlow(null)
    val reportGenerated: Flow<Event<String>?> = _reportGenerated
    val screenState = TransactionListScreenState()

    fun loadTransactions(groupId: Int) {

        if(isAlreadyLoaded) return else isAlreadyLoaded=true
        viewModelScope.launch {
            db.transactionDao.getAllTransactionListItems(groupId)
                .collect{
                    screenState.transactions=it
                }
        }
    }

    fun deleteTransaction(transactionId: Int) {
        viewModelScope.launch(Dispatchers.IO){
            db.getTransactionDao().deleteTransactionById(transactionId)
        }
    }

    fun createChequeBasedRTGSReport(transactionGroup: TransactionGroup, chequeNumber:String){
        viewModelScope.launch {
            try {
                val report = ChequeRTGSReport(getApplication(),db,chequeNumber)
                val fileName=report.createReport(transactionGroup)
                _reportGenerated.value=Event(fileName)
            } catch (_: Exception) {
                //Setting empty string for filename will toast error in UI
                _reportGenerated.value= Event("")
            }

        }
    }

    fun createBizzPay360Report(transactionGroup: TransactionGroup, time:Long){
        viewModelScope.launch {
            try {
                val report = BizzPay360Report(getApplication(),db,time)
                val fileName=report.createReport(transactionGroup)
                _reportGenerated.value=Event(fileName)
            } catch (_: Exception) {
                //Setting empty string for filename will toast error in UI
                _reportGenerated.value=Event("")
            }
        }
    }

    fun shareFile(context: Context, filename: String) {


        val sd = context.cacheDir
        val fileLocation = File(sd, filename)
        val path = FileProvider.getUriForFile(
            context, context.getString(R.string.file_provider_authority), fileLocation
        )
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        //emailIntent.setDataAndType(path,"file/*");
        //emailIntent.setType("vnd.android.cursor.dir/email");
        emailIntent.setType("file/*")
        emailIntent.putExtra(Intent.EXTRA_STREAM, path)
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.email_subject_line))
        context.startActivity(
            Intent.createChooser(
                emailIntent,
                context.getString(R.string.share_report_chooser_heading)
            )
        )

    }
}
