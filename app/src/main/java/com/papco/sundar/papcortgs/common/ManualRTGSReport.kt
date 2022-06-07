@file:Suppress("INACCESSIBLE_TYPE")

package com.papco.sundar.papcortgs.common

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Environment
import com.papco.sundar.papcortgs.database.common.MasterDatabase
import com.papco.sundar.papcortgs.database.transaction.Transaction
import com.papco.sundar.papcortgs.database.transactionGroup.TransactionGroup
import jxl.Workbook
import jxl.WorkbookSettings
import jxl.format.*
import jxl.format.Alignment
import jxl.format.Border
import jxl.format.BorderLineStyle
import jxl.format.Colour
import jxl.format.VerticalAlignment
import jxl.write.*
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.ceil
import kotlin.math.max

@Suppress("BlockingMethodInNonBlockingContext")
class ManualRTGSReport(
        private val db: MasterDatabase,
        private val chequeNumber: String) {

    private val columnDetails = ArrayList<ColumnDetail>(14)
    private var transactionGroup: TransactionGroup? = null
    private val widthOfOneCharacterinPixels:Double=4.712

    private val filename by lazy {
        val prefix = transactionGroup?.name ?: "papcoRtgs"
        "${prefix}.xls"
    }

    private val textPaint by lazy {
        createTextPaint()
    }


    fun createReport(transactionGroup: TransactionGroup): String {

        this.transactionGroup = transactionGroup
        val transactions = loadTransactions(transactionGroup.id)
        setDefaultColumnWidths()
        val workbook = createWorkBook()
        val sheet = workbook.createSheet("Sheet1", 0)
        writeHeadings(sheet)
        writeTransactions(sheet, transactions)
        setColumnWidths(sheet)

        workbook.write()
        workbook.close()

        return filename

    }

    private fun loadTransactions(groupId: Int): List<Transaction> {

        val transactions = db.transactionDao.getTransactionsNonLive(groupId)

        for (transaction in transactions) {
            transaction.sender = db.senderDao.getSender(transaction.senderId)
            transaction.receiver = db.receiverDao.getReceiver(transaction.receiverId)
        }

        return transactions

    }

    private fun setDefaultColumnWidths() {

        columnDetails.add(0, ColumnDetail(5))
        columnDetails.add(1, ColumnDetail(8))
        columnDetails.add(2, ColumnDetail(10))
        columnDetails.add(3, ColumnDetail(9))
        columnDetails.add(4, ColumnDetail(12))
        columnDetails.add(5, ColumnDetail(28))
        columnDetails.add(6, ColumnDetail(8))
        columnDetails.add(7, ColumnDetail(20))
        columnDetails.add(8, ColumnDetail(28))
        columnDetails.add(9, ColumnDetail(11))
        columnDetails.add(10, ColumnDetail(14))
        columnDetails.add(11, ColumnDetail(12))
        columnDetails.add(12, ColumnDetail(10))
        columnDetails.add(13, ColumnDetail(10))


    }


    private fun createWorkBook(): WritableWorkbook {

        val sdCard = Environment.getExternalStorageDirectory()
        val destinationDirectoryPath = sdCard.absolutePath + "/papcoRTGS"
        val destinationDirectory = File(destinationDirectoryPath)

        if (!destinationDirectory.isDirectory) {
            destinationDirectory.mkdirs()
        }

        val workBookSettings = WorkbookSettings()
        workBookSettings.locale = Locale("en", "EN")

        val excelFile = File(destinationDirectory, filename)
        return Workbook.createWorkbook(excelFile, workBookSettings)

    }

    private fun headingCellFormat(): CellFormat {

        val font = WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD)
        font.colour = Colour.BLACK
        return WritableCellFormat(font).apply {
            setBorder(Border.ALL, BorderLineStyle.THIN)
            alignment = Alignment.CENTRE
            wrap = true
        }
    }

    private fun contentCellFormat(): CellFormat {

        val font = WritableFont(WritableFont.ARIAL, 10, WritableFont.NO_BOLD)
        return WritableCellFormat(font).apply {
            setBorder(Border.ALL, BorderLineStyle.THIN)
            alignment = Alignment.CENTRE
            verticalAlignment = VerticalAlignment.BOTTOM
            wrap = false
        }
    }

    private fun writeHeadings(sheet: WritableSheet) {

        val headingFormat = headingCellFormat()

        sheet.setColumnView(0, 5) //setting the column width
        sheet.addCell(Label(0, 0, "Sr. No.", headingFormat))

        sheet.setColumnView(1, 8) //setting the column width
        sheet.addCell(Label(1, 0, "TRAN ID", headingFormat))

        sheet.setColumnView(2, 10) //setting the column width
        sheet.addCell(Label(2, 0, "AMOUNT", headingFormat))

        sheet.setColumnView(3, 9) //setting the column width
        sheet.addCell(Label(3, 0, "SENDER ACCOUNT TYPE", headingFormat))

        sheet.setColumnView(4, 12) //setting the column width
        sheet.addCell(Label(4, 0, "SENDER ACCOUNT NO", headingFormat))

        sheet.setColumnView(5, 28) //setting the column width
        sheet.addCell(Label(5, 0, "SENDER NAME", headingFormat))

        sheet.setColumnView(6, 8) //setting the column width
        sheet.addCell(Label(6, 0, "SMS EML", headingFormat))

        sheet.setColumnView(7, 20) //setting the column width
        sheet.addCell(Label(7, 0, "Detail", headingFormat))

        sheet.setColumnView(8, 28) //setting the column width
        sheet.addCell(Label(8, 0, "OoR7002 (SENDER NAME)", headingFormat))

        sheet.setColumnView(9, 11) //setting the column width
        sheet.addCell(Label(9, 0, "BENEFICIARY IFSC", headingFormat))

        sheet.setColumnView(10, 14) //setting the column width
        sheet.addCell(Label(10, 0, "BENEFICIARY ACCOUNT TYPE", headingFormat))

        sheet.setColumnView(11, 12) //setting the column width
        sheet.addCell(Label(11, 0, "BENEFICIARY ACCOUNT NO", headingFormat))

        sheet.setColumnView(12, 10) //setting the column width
        sheet.addCell(Label(12, 0, "BENEFICIARY ACCOUNT NAME", headingFormat))

        sheet.setColumnView(13, 10) //setting the column width
        sheet.addCell(Label(13, 0, "SENDER TO RECEIVER INFORMATION", headingFormat))

    }


    private fun writeTransactions(sheet: WritableSheet, transactions: List<Transaction>) {

        val contentFormat = contentCellFormat()
        for ((index, transaction) in transactions.withIndex()) {
            writeTransaction(sheet, transaction, index + 1, contentFormat)
        }
    }

    private fun writeTransaction(sheet: WritableSheet, transaction: Transaction, row: Int, format: CellFormat) {

        val sender = transaction.sender
        val receiver = transaction.receiver

        sheet.addCell(Label(0, row, row.toString(), format))
        calculateColumnWidth(0, row.toString())

        sheet.addCell(Label(1, row, chequeNumber, format))
        calculateColumnWidth(1, chequeNumber)

        sheet.addCell(Label(2, row, transaction.amount.toString(), format))
        calculateColumnWidth(2, transaction.amount.toString())

        sheet.addCell(Label(3, row, sender.accountType, format))
        calculateColumnWidth(3, sender.accountType)

        sheet.addCell(Label(4, row, sender.accountNumber, format))
        calculateColumnWidth(4, sender.accountNumber)

        sheet.addCell(Label(5, row, sender.name, format))
        calculateColumnWidth(5, sender.name)

        sheet.addCell(Label(6, row, "EML", format))
        calculateColumnWidth(6, "EML")

        sheet.addCell(Label(7, row, receiver.email, format))
        calculateColumnWidth(7, receiver.email)

        sheet.addCell(Label(8, row, sender.name, format))
        calculateColumnWidth(8, sender.name)

        sheet.addCell(Label(9, row, receiver.ifsc, format))
        calculateColumnWidth(9, receiver.ifsc)

        sheet.addCell(Label(10, row, receiver.accountType, format))
        calculateColumnWidth(10, receiver.accountType)

        sheet.addCell(Label(11, row, receiver.accountNumber, format))
        calculateColumnWidth(11, receiver.accountNumber)

        sheet.addCell(Label(12, row, receiver.name, format))
        calculateColumnWidth(12, receiver.name)

        sheet.addCell(Label(13, row, "ON ACCOUNT", format))
        calculateColumnWidth(13, "ON ACCOUNT")

    }

    private fun calculateColumnWidth(column: Int, matter: String) {

        val textWidthInPixels=textPaint.measureText(matter).toDouble()
        val widthInCharacters=ceil(textWidthInPixels/widthOfOneCharacterinPixels).toInt()
        if (widthInCharacters> columnDetails[column].recommendedWidth) columnDetails[column].recommendedWidth = widthInCharacters

    }

    private fun setColumnWidths(sheet: WritableSheet) {

        for ((index, columnDetail) in columnDetails.withIndex()) {
            sheet.setColumnView(index, max(columnDetail.minimumWidth, columnDetail.recommendedWidth))
        }

    }

    private fun createTextPaint():Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isLinearText=true
        textSize = 10f
        color = Color.BLACK
        typeface = Typeface.create("Arial", Typeface.NORMAL)
    }


}