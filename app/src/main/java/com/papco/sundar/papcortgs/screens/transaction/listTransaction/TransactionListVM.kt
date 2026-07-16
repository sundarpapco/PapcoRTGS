package com.papco.sundar.papcortgs.screens.transaction.listTransaction

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.database.common.MasterDatabase
import com.papco.sundar.papcortgs.database.transactionGroup.TransactionGroup
import com.papco.sundar.papcortgs.reports.BizzPay360Report
import com.papco.sundar.papcortgs.reports.CMSReport
import com.papco.sundar.papcortgs.reports.RTGSReport
import com.papco.sundar.papcortgs.ui.screens.transaction.TransactionListScreenState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class TransactionListVM(val application: Application, groupId: Int) : ViewModel() {

    companion object {
        fun factory(application: Application, groupId: Int): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return if (modelClass.isAssignableFrom(TransactionListVM::class.java))
                        TransactionListVM(application, groupId) as T
                    else
                        error("Unknown ViewModelClass")
                }
            }
        }
    }

    private val db: MasterDatabase = MasterDatabase.getInstance(application)
    private val transactions = db.transactionDao.getAllTransactionListItems(groupId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val screen = TransactionListScreenState(transactions)

    private val currentTransactions
        get() = transactions.value


    fun deleteTransaction(transactionId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            db.getTransactionDao().deleteTransactionById(transactionId)
        }
    }

    private fun createReport(report: RTGSReport,transactionGroup: TransactionGroup){

        viewModelScope.launch {
            if (currentTransactions.isNotEmpty()) {
                try {
                    val fileName = report.createReport(transactionGroup)
                    screen.showReportGeneratedDialog(fileName)
                } catch (_: Exception) {
                    screen.toastResource(R.string.error_in_creating_the_excel_file)
                }
            } else
                screen.toastResource(R.string.add_at_least_one_transaction_to_export)
        }
    }

    fun createCMSReport(transactionGroup: TransactionGroup, time: Long) =
        createReport(CMSReport(application, db, time),transactionGroup)

    fun createBizzPay360Report(transactionGroup: TransactionGroup, time: Long) =
        createReport(BizzPay360Report(application,db,time),transactionGroup)

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
        emailIntent.type = "file/*"
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
