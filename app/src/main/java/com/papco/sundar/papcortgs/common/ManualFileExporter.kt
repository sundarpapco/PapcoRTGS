package com.papco.sundar.papcortgs.common

import android.content.Context
import com.papco.sundar.papcortgs.database.common.MasterDatabase
import com.papco.sundar.papcortgs.database.transactionGroup.TransactionGroup

class ManualFileExporter(
    private val context:Context,
    private val db: MasterDatabase,
    private val chequeNumber: String
) {
    suspend fun export( transactionGroup: TransactionGroup): String {

        val result: String = try {
            val report = ManualRTGSReport(context,db, chequeNumber)
            report.createReport(transactionGroup)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
        return result
    }
}
