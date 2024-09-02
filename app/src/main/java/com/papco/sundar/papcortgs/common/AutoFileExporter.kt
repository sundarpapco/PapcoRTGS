package com.papco.sundar.papcortgs.common

import android.content.Context
import android.os.AsyncTask
import com.papco.sundar.papcortgs.database.common.MasterDatabase
import com.papco.sundar.papcortgs.database.transactionGroup.TransactionGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

class AutoFileExporter(
    private val context: Context,
    private val db: MasterDatabase,
    private var time: Long
) {
    suspend fun export(transactionGroup: TransactionGroup): String {

        val result: String = try {
            val report = AutoRTGSReport(context, db, time)
            report.createReport(transactionGroup)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
        return result
    }
}
