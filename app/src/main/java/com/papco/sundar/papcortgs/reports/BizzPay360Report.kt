@file:Suppress("INACCESSIBLE_TYPE")

package com.papco.sundar.papcortgs.reports

import android.content.Context
import com.papco.sundar.papcortgs.database.common.MasterDatabase
import com.papco.sundar.papcortgs.database.transaction.Transaction
import com.papco.sundar.papcortgs.database.transactionGroup.TransactionGroup
import jxl.HeaderFooter
import jxl.Sheet
import jxl.Workbook
import jxl.WorkbookSettings
import jxl.format.Alignment
import jxl.format.Border
import jxl.format.BorderLineStyle
import jxl.format.CellFormat
import jxl.format.Colour
import jxl.format.PageOrientation
import jxl.format.VerticalAlignment
import jxl.write.Label
import jxl.write.WritableCellFormat
import jxl.write.WritableFont
import jxl.write.WritableSheet
import jxl.write.WritableWorkbook
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BizzPay360Report(
    private val context: Context,
    private val db: MasterDatabase,
    time: Long
) {

    private val columnWidths = ArrayList<ColumnWidth>(21)
    private var transactionGroup: TransactionGroup? = null

    private var totalAmount: Int = 0

    private val filename by lazy {

        val date = Calendar.getInstance(Locale.getDefault())
        date.timeInMillis = time
        val year = date.get(Calendar.YEAR).toString().takeLast(2)
        val month = "%02d".format(date.get(Calendar.MONTH) + 1)
        val day = "%02d".format(date.get(Calendar.DAY_OF_MONTH))
        val amount = totalAmount.toString().trim()

        "${day}${month}${year}_PAY_${amount}.xls"
    }

    private val date by lazy {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        dateFormat.format(Date(time))
    }

    private var rowSize: Int = 0


    suspend fun createReport(transactionGroup: TransactionGroup): String =
        withContext(Dispatchers.IO) {

            this@BizzPay360Report.transactionGroup = transactionGroup
            val transactions = loadTransactions(transactionGroup.id)
            totalAmount = transactions.sumOf { it.amount }
            setDefaultColumnWidths()
            val workbook = createWorkBook()
            val sheet = workbook.createSheet("Sheet1", 0)
            rowSize = sheet.getRowView(0).size
            writeHeadings(sheet)
            writeTransactions(sheet, transactions)
            setColumnWidths(sheet)
            writeSheetPrintSettings(sheet, transactions.size)

            workbook.write()
            workbook.close()

            filename

        }

    private suspend fun loadTransactions(groupId: Int): List<Transaction> {

        val transactions = db.transactionDao.getTransactionsNonLive(groupId)

        for (transaction in transactions) {
            transaction.sender = db.senderDao.getSender(transaction.senderId)
            transaction.receiver = db.receiverDao.getReceiver(transaction.receiverId)
        }

        return transactions

    }

    private fun setDefaultColumnWidths() {

        //fixed with column
        columnWidths.add(0, ColumnWidth(11, 11)) //PAB Vendor
        columnWidths.add(1, ColumnWidth(6, 6)) //Payment Mode
        columnWidths.add(2, ColumnWidth(15)) //Debit Ac No
        columnWidths.add(3, ColumnWidth(20)) //Bene Name
        columnWidths.add(4, ColumnWidth(18)) //Bene Ac No
        columnWidths.add(5, ColumnWidth(8)) //IFSC
        columnWidths.add(6, ColumnWidth(9)) //Amt
        columnWidths.add(7, ColumnWidth(6, 6)) //Debit Narrative
        columnWidths.add(8, ColumnWidth(6, 6)) //Credit Narrative
        columnWidths.add(9, ColumnWidth(12)) //Bene Mob No
        columnWidths.add(10, ColumnWidth(10, 23)) //Bene Email ID
        columnWidths.add(11, ColumnWidth(7)) //Remarks
        columnWidths.add(12, ColumnWidth(10)) //Date
        columnWidths.add(13, ColumnWidth(10)) //Ref No
        columnWidths.add(14, ColumnWidth(10)) //Add Info 1
        columnWidths.add(15, ColumnWidth(10)) //Add Info 2
        columnWidths.add(16, ColumnWidth(10)) //Add Info 3
        columnWidths.add(17, ColumnWidth(10)) //Add Info 4
        columnWidths.add(18, ColumnWidth(10)) //Add Info 5

    }


    private fun createWorkBook(): WritableWorkbook {

        val workBookSettings = WorkbookSettings()
        workBookSettings.locale = Locale("en", "EN")

        val excelFile = File(context.cacheDir, filename)
        return Workbook.createWorkbook(excelFile, workBookSettings)

    }

    private fun headingCellFormat(compulsoryField: Boolean = true): CellFormat {

        val fontName = WritableFont.createFont("Calibri")
        val font = WritableFont(fontName, 11, WritableFont.BOLD)
        if (compulsoryField)
            font.colour = Colour.RED
        return WritableCellFormat(font).apply {
            setBorder(Border.ALL, BorderLineStyle.THIN)
            alignment = Alignment.LEFT
            wrap = false
        }
    }

    private fun contentCellFormat(): CellFormat {

        val fontName = WritableFont.createFont("Calibri")
        val font = WritableFont(fontName, 11, WritableFont.NO_BOLD)
        return WritableCellFormat(font).apply {
            setBorder(Border.ALL, BorderLineStyle.THIN)
            alignment = Alignment.LEFT
            verticalAlignment = VerticalAlignment.CENTRE
            wrap = false
        }
    }

    private fun writeHeadings(sheet: WritableSheet) {

        val compulsoryHeadingFormat = headingCellFormat(true)
        val optionalHeadingFormat = headingCellFormat(false)

        sheet.setColumnView(0, 12) //setting the column width
        sheet.addCell(Label(0, 0, "CODE", compulsoryHeadingFormat))

        sheet.setColumnView(1, 12) //setting the column width
        sheet.addCell(Label(1, 0, "Mode", compulsoryHeadingFormat))

        sheet.setColumnView(2, 10) //setting the column width
        sheet.addCell(Label(2, 0, "Debit Ac No", compulsoryHeadingFormat))

        sheet.setColumnView(3, 9) //setting the column width
        sheet.addCell(Label(3, 0, "Beneficiary Name", compulsoryHeadingFormat))

        sheet.setColumnView(4, 7) //setting the column width
        sheet.addCell(Label(4, 0, "Beneficiary Ac No", compulsoryHeadingFormat))

        sheet.setColumnView(5, 11) //setting the column width
        sheet.addCell(Label(5, 0, "IFSC", compulsoryHeadingFormat))

        sheet.setColumnView(6, 11) //setting the column width
        sheet.addCell(Label(6, 0, "Amt", compulsoryHeadingFormat))

        sheet.setColumnView(7, 20) //setting the column width
        sheet.addCell(Label(7, 0, "Debit Narr", optionalHeadingFormat))

        sheet.setColumnView(8, 20) //setting the column width
        sheet.addCell(Label(8, 0, "Credit Narr", optionalHeadingFormat))

        sheet.setColumnView(9, 20) //setting the column width
        sheet.addCell(Label(9, 0, "Bene Mobile No.", optionalHeadingFormat))

        sheet.setColumnView(10, 20) //setting the column width
        sheet.addCell(Label(10, 0, "Bene Email ID", optionalHeadingFormat))

        sheet.setColumnView(11, 20) //setting the column width
        sheet.addCell(Label(11, 0, "Remark", optionalHeadingFormat))

        sheet.setColumnView(12, 20) //setting the column width
        sheet.addCell(Label(12, 0, "Date", compulsoryHeadingFormat))

        sheet.setColumnView(13, 20) //setting the column width
        sheet.addCell(Label(13, 0, "Ref No", optionalHeadingFormat))

        sheet.setColumnView(14, 20) //setting the column width
        sheet.addCell(Label(14, 0, "Add Info 1", optionalHeadingFormat))

        sheet.setColumnView(15, 20) //setting the column width
        sheet.addCell(Label(15, 0, "Add Info 2", optionalHeadingFormat))

        sheet.setColumnView(16, 20) //setting the column width
        sheet.addCell(Label(16, 0, "Add Info 3", optionalHeadingFormat))

        sheet.setColumnView(17, 20) //setting the column width
        sheet.addCell(Label(17, 0, "Add Info 4", optionalHeadingFormat))

        sheet.setColumnView(18, 20) //setting the column width
        sheet.addCell(Label(18, 0, "Add Info 5", optionalHeadingFormat))

    }

    private fun paymentMode(ifsc: String): String {

        val firstFourChars = ifsc.subSequence(0..3)
        return if (firstFourChars == "ICIC") {
            "FT"
        } else
            "IMPS"
    }

    private fun writeTransactions(sheet: WritableSheet, transactions: List<Transaction>) {

        val contentFormat = contentCellFormat()
        for ((index, transaction) in transactions.withIndex()) {
            val currentRow = sheet.getRowView(index + 1)
            currentRow.size = rowSize
            sheet.setRowView(index + 1, currentRow)
            writeTransaction(sheet, transaction, index + 1, contentFormat)
        }
    }

    private fun writeTransaction(
        sheet: WritableSheet,
        transaction: Transaction,
        row: Int,
        format: CellFormat
    ) {

        val sender = transaction.sender!!
        val receiver = transaction.receiver!!

        sheet.addCell(Label(0, row, "PAB_VENDOR", format))
        columnWidths[0].calculateRecommendedWidth("PAB_VENDOR")

        sheet.addCell(Label(1, row, paymentMode(receiver.ifsc), format))
        columnWidths[1].calculateRecommendedWidth(paymentMode(receiver.ifsc))

        sheet.addCell(Label(2, row, sender.accountNumber, format))
        columnWidths[2].calculateRecommendedWidth(sender.accountNumber)

        sheet.addCell(Label(3, row, receiver.name, format))
        columnWidths[3].calculateRecommendedWidth(receiver.name)

        sheet.addCell(Label(4, row, receiver.accountNumber, format))
        columnWidths[4].calculateRecommendedWidth(receiver.accountNumber)

        sheet.addCell(Label(5, row, receiver.ifsc, format))
        columnWidths[5].calculateRecommendedWidth(receiver.ifsc)

        sheet.addCell(Label(6, row, transaction.amount.toString(), format))
        columnWidths[6].calculateRecommendedWidth(transaction.amount.toString())

        //Debit Narrative. Write Nothing. But Draw the Border alone
        sheet.addCell(Label(7, row, "", format))

        //Credit Narrative. Write Nothing. But Draw the Border alone
        sheet.addCell(Label(8, row, "", format))

        sheet.addCell(Label(9, row, receiver.mobileNumber, format))
        columnWidths[9].calculateRecommendedWidth(receiver.mobileNumber)

        sheet.addCell(Label(10, row, receiver.email, format))
        columnWidths[10].calculateRecommendedWidth(receiver.email)

        //Remarks. Write Nothing. But Draw the Border alone
        sheet.addCell(Label(11, row, "", format))

        sheet.addCell(Label(12, row, date, format))
        columnWidths[12].calculateRecommendedWidth(date)

        //Writing nothing in the cell but drawing the border for the cells
        sheet.addCell(Label(13, row, "", format)) //Ref No
        sheet.addCell(Label(14, row, "", format)) // Add Info 1
        sheet.addCell(Label(15, row, "", format)) // Add Info 2
        sheet.addCell(Label(16, row, "", format)) // Add Info 3
        sheet.addCell(Label(17, row, "", format)) // Add Info 5
        sheet.addCell(Label(18, row, "", format)) // Add Info 5

    }

    private fun writeSheetPrintSettings(sheet: Sheet, noOfTransactions: Int) {

        //Write header
        val header = HeaderFooter()
        header.left.append("&15")
        header.left.append("&\"Arial\"")
        header.left.append("File Name: $filename")
        header.right.append("&15")
        header.right.append("&\"Arial\"")
        header.right.append("Date: $date")


        //Set Page Margins to Minimum
        with(sheet.settings) {

            this.header = header

            setPrintArea(0, 0, 12, noOfTransactions)

            //topMargin is default since we have Header
            bottomMargin = 0.0
            leftMargin = 0.0
            rightMargin = 0.0

            orientation = PageOrientation.LANDSCAPE

            fitToPages = true
            fitWidth = 1       // Force data to fit within 1 page wide
        }

    }

    private fun setColumnWidths(sheet: WritableSheet) {

        for ((index, columnDetail) in columnWidths.withIndex()) {
            sheet.setColumnView(
                index,
                columnDetail.recommendedWidth
            )
        }

    }


}