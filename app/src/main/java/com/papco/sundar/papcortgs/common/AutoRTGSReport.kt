package com.papco.sundar.papcortgs.common

import android.os.Environment
import android.text.TextUtils
import com.papco.sundar.papcortgs.common.FileExporter.ColumnDetail
import com.papco.sundar.papcortgs.database.common.MasterDatabase
import com.papco.sundar.papcortgs.database.transaction.Transaction
import com.papco.sundar.papcortgs.database.transactionGroup.TransactionGroup
import jxl.Workbook
import jxl.WorkbookSettings
import jxl.format.Alignment
import jxl.format.Border
import jxl.format.BorderLineStyle
import jxl.format.CellFormat
import jxl.format.Colour
import jxl.format.VerticalAlignment
import jxl.write.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.max

@Suppress("BlockingMethodInNonBlockingContext")
class AutoRTGSReport(
        private val db: MasterDatabase,
        time: Long
) {

    private val columnDetails = ArrayList<ColumnDetail>(21)
    private var transactionGroup:TransactionGroup?=null

    private val filename by lazy{
        val prefix=transactionGroup?.name ?: "papcoRtgs"
        "${prefix}_auto.xls"
    }

    private val date by lazy{
        val dateFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
        dateFormat.format(Date(time)).toUpperCase(Locale.getDefault())
    }


    fun createReport(transactionGroup: TransactionGroup): String {

        this.transactionGroup=transactionGroup
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

        columnDetails.add(0, ColumnDetail(15))
        columnDetails.add(1, ColumnDetail(18))
        columnDetails.add(2, ColumnDetail(20))
        columnDetails.add(3, ColumnDetail(9))
        columnDetails.add(4, ColumnDetail(10))
        columnDetails.add(5, ColumnDetail(11))
        columnDetails.add(6, ColumnDetail(11))
        columnDetails.add(7, ColumnDetail(15))
        columnDetails.add(8, ColumnDetail(15))
        columnDetails.add(9, ColumnDetail(15))
        columnDetails.add(10, ColumnDetail(15))
        columnDetails.add(11, ColumnDetail(15))
        columnDetails.add(12, ColumnDetail(15))
        columnDetails.add(13, ColumnDetail(15))
        columnDetails.add(14, ColumnDetail(15))
        columnDetails.add(15, ColumnDetail(15))
        columnDetails.add(16, ColumnDetail(15))
        columnDetails.add(17, ColumnDetail(15))
        columnDetails.add(18, ColumnDetail(15))
        columnDetails.add(19, ColumnDetail(15))
        columnDetails.add(20, ColumnDetail(15))


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

    private fun headingCellFormat(compulsoryField: Boolean = true): CellFormat {

        val fontName=WritableFont.createFont("Calibri")
        val font=WritableFont(fontName,11,WritableFont.BOLD)
        if (compulsoryField)
            font.colour = Colour.RED
        return WritableCellFormat(font).apply {
            setBorder(Border.ALL, BorderLineStyle.THIN)
            alignment = Alignment.LEFT
            wrap = false
        }
    }

    private fun contentCellFormat(): CellFormat {

        val fontName=WritableFont.createFont("Calibri")
        val font=WritableFont(fontName,11,WritableFont.NO_BOLD)
        return WritableCellFormat(font).apply {
            setBorder(Border.ALL, BorderLineStyle.THIN)
            alignment = Alignment.LEFT
            verticalAlignment = VerticalAlignment.BOTTOM
            wrap = true
        }
    }

    private fun writeHeadings(sheet: WritableSheet) {

        val compulsoryHeadingFormat = headingCellFormat(true)
        val optionalHeadingFormat=headingCellFormat(false)

        sheet.setColumnView(0, 12) //setting the column width
        sheet.addCell(Label(0, 0, "Debit Ac No", compulsoryHeadingFormat))

        sheet.setColumnView(1, 12) //setting the column width
        sheet.addCell(Label(1, 0, "Beneficiary Ac No", compulsoryHeadingFormat))

        sheet.setColumnView(2, 10) //setting the column width
        sheet.addCell(Label(2, 0, "Beneficiary Name", compulsoryHeadingFormat))

        sheet.setColumnView(3, 9) //setting the column width
        sheet.addCell(Label(3, 0, "Amt", compulsoryHeadingFormat))

        sheet.setColumnView(4, 7) //setting the column width
        sheet.addCell(Label(4, 0, "Pay Mod", compulsoryHeadingFormat))

        sheet.setColumnView(5, 11) //setting the column width
        sheet.addCell(Label(5, 0, "Date", compulsoryHeadingFormat))

        sheet.setColumnView(6, 11) //setting the column width
        sheet.addCell(Label(6, 0, "IFSC", compulsoryHeadingFormat))

        sheet.setColumnView(7, 20) //setting the column width
        sheet.addCell(Label(7, 0, "Payable Location", optionalHeadingFormat))

        sheet.setColumnView(8, 20) //setting the column width
        sheet.addCell(Label(8, 0, "Print Location", optionalHeadingFormat))

        sheet.setColumnView(9, 20) //setting the column width
        sheet.addCell(Label(9, 0, "Bene Mobile No.", optionalHeadingFormat))

        sheet.setColumnView(10, 20) //setting the column width
        sheet.addCell(Label(10, 0, "Bene Email ID", optionalHeadingFormat))

        sheet.setColumnView(11, 20) //setting the column width
        sheet.addCell(Label(11, 0, "Bene add1", optionalHeadingFormat))

        sheet.setColumnView(12, 20) //setting the column width
        sheet.addCell(Label(12, 0, "Bene add2", optionalHeadingFormat))

        sheet.setColumnView(13, 20) //setting the column width
        sheet.addCell(Label(13, 0, "Bene add3", optionalHeadingFormat))

        sheet.setColumnView(14, 20) //setting the column width
        sheet.addCell(Label(14, 0, "Bene add4", optionalHeadingFormat))

        sheet.setColumnView(15, 20) //setting the column width
        sheet.addCell(Label(15, 0, "Add Details 1", optionalHeadingFormat))

        sheet.setColumnView(16, 20) //setting the column width
        sheet.addCell(Label(16, 0, "Add Details 2", optionalHeadingFormat))

        sheet.setColumnView(17, 20) //setting the column width
        sheet.addCell(Label(17, 0, "Add Details 3", optionalHeadingFormat))

        sheet.setColumnView(18, 20) //setting the column width
        sheet.addCell(Label(18, 0, "Add Details 4", optionalHeadingFormat))

        sheet.setColumnView(19, 20) //setting the column width
        sheet.addCell(Label(19, 0, "Add Details 5", optionalHeadingFormat))

        sheet.setColumnView(20, 20) //setting the column width
        sheet.addCell(Label(20, 0, "Remarks", optionalHeadingFormat))

    }

    private fun paymentMode(ifsc: String, amount: Int): String {

        val firstFourChars = ifsc.subSequence(0..3)
        if (firstFourChars == "ICIC") {
            return "I"
        }

        return if (amount <= 2_00_000)
            "N"
        else
            "R"

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

        sheet.addCell(Label(0, row, sender.accountNumber, format))
        calculateColumnWidth(0, sender.accountNumber)

        sheet.addCell(Label(1, row, receiver.accountNumber,format))
        calculateColumnWidth(1, receiver.accountNumber)

        sheet.addCell(Label(2, row, receiver.name,format))
        calculateColumnWidth(2, receiver.name)

        sheet.addCell(Label(3, row, transaction.amount.toString(),format))
        calculateColumnWidth(3, transaction.amount.toString())

        sheet.addCell(Label(4, row, paymentMode(receiver.ifsc, transaction.amount),format))
        calculateColumnWidth(4, paymentMode(receiver.ifsc, transaction.amount))

        sheet.addCell(Label(5, row, date,format))
        calculateColumnWidth(5, date)

        sheet.addCell(Label(6, row, receiver.ifsc,format))
        calculateColumnWidth(6, receiver.ifsc)

        sheet.addCell(Label(7,row,"",format))
        sheet.addCell(Label(8,row,"",format))

        sheet.addCell(Label(9,row,receiver.mobileNumber,format))
        calculateColumnWidth(9,receiver.mobileNumber)

        sheet.addCell(Label(10,row,receiver.email,format))
        calculateColumnWidth(10,receiver.email)

        //Writing nothing in the cell but drawing the border for the cells
        sheet.addCell(Label(11,row,"",format))
        sheet.addCell(Label(12,row,"",format))
        sheet.addCell(Label(13,row,"",format))
        sheet.addCell(Label(14,row,"",format))
        sheet.addCell(Label(15,row,"",format))
        sheet.addCell(Label(16,row,"",format))
        sheet.addCell(Label(17,row,"",format))
        sheet.addCell(Label(18,row,"",format))
        sheet.addCell(Label(19,row,"",format))
        sheet.addCell(Label(20,row,"",format))

    }

    private fun calculateColumnWidth(column: Int, matter: String) {

        val length = if (TextUtils.isDigitsOnly(matter))
            matter.length + 2
        else
            matter.length + 4

        if (length > columnDetails[column].recommendedWidth) columnDetails[column].recommendedWidth = length
    }

    private fun setColumnWidths(sheet: WritableSheet) {

        for ((index, columnDetail) in columnDetails.withIndex()) {
            sheet.setColumnView(index, max(columnDetail.minimumWidth, columnDetail.recommendedWidth))
        }

    }


}