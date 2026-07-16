package com.papco.sundar.papcortgs.reports

import com.papco.sundar.papcortgs.database.transactionGroup.TransactionGroup

interface RTGSReport {
    suspend fun createReport(transactionGroup: TransactionGroup): String
}