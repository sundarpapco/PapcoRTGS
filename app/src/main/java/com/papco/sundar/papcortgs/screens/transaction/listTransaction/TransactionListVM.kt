package com.papco.sundar.papcortgs.screens.transaction.listTransaction

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.MutableState
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.common.AutoFileExporter
import com.papco.sundar.papcortgs.common.Event
import com.papco.sundar.papcortgs.common.ManualFileExporter
import com.papco.sundar.papcortgs.database.common.MasterDatabase
import com.papco.sundar.papcortgs.database.transaction.TransactionForList
import com.papco.sundar.papcortgs.database.transactionGroup.TransactionGroup
import com.papco.sundar.papcortgs.ui.screens.transaction.TransactionListScreenState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class TransactionListVM(application: Application) : AndroidViewModel(application) {

    private val db: MasterDatabase

    private var isAlreadyLoaded=false
    private val _reportGenerated:MutableStateFlow<Event<String>?> = MutableStateFlow(null)
    val reportGenerated: Flow<Event<String>?> = _reportGenerated
    val screenState = TransactionListScreenState()

    init {
        db = MasterDatabase.getInstance(application)
    }

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

    fun createManualExportFile(transactionGroup: TransactionGroup, chequeNumber:String){
        viewModelScope.launch(Dispatchers.IO) {
            val exporter=ManualFileExporter(getApplication(),db,chequeNumber)
            val fileName=exporter.export(transactionGroup)
            withContext(Dispatchers.Main){
                _reportGenerated.value=Event(fileName)
            }
        }
    }

    fun createAutoXlFile(transactionGroup: TransactionGroup,time:Long){
        viewModelScope.launch(Dispatchers.IO) {
            val exporter=AutoFileExporter(getApplication(),db,time)
            val fileName=exporter.export(transactionGroup)
            withContext(Dispatchers.Main){
                _reportGenerated.value=Event(fileName)
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
