package com.papco.sundar.papcortgs.screens.backup

import com.example.payroll.dropbox.DropBox
import com.papco.sundar.papcortgs.database.common.MasterDatabase
import com.papco.sundar.papcortgs.database.receiver.Receiver
import com.papco.sundar.papcortgs.database.sender.Sender
import com.papco.sundar.papcortgs.database.transaction.Transaction
import com.papco.sundar.papcortgs.database.transactionGroup.TransactionGroup
import com.papco.sundar.papcortgs.settings.AppPreferences
import jxl.Cell
import jxl.Workbook
import jxl.WorkbookSettings
import jxl.format.Alignment
import jxl.format.VerticalAlignment
import jxl.write.Label
import jxl.write.Number
import jxl.write.WritableCellFormat
import jxl.write.WritableFont
import jxl.write.WritableWorkbook
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import java.io.File
import java.util.Locale

class BackupManager(
    private val db:MasterDatabase,
    private val appPreferences: AppPreferences,
    private val dropBox: DropBox
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    fun doBackup()= flow{

        //Create the backup file in local drive
        val receiversFile: File = File(appPreferences.getLocalBackupFilePath())

        //prepare and create the workbook and writable sheet
        val workbook: WritableWorkbook
        val wbSettings = WorkbookSettings()
        wbSettings.locale = Locale("en", "EN")
        workbook = Workbook.createWorkbook(receiversFile, wbSettings)

        emit("Backing up Receivers...")
        writeReceiverToWorkbook(workbook)

        emit("Backing up Senders...")
        writeSendersToWorkbook(workbook)

        emit("Backing up XL Sheets")
        writeGroupsToWorkBook(workbook)

        emit("Backing up Transactions")
        writeTransactionsToWorkBook(workbook)

        workbook.write()
        workbook.close()

        emit("Backing up to dropbox...")
        dropBox.uploadBackupFile()

        emit("Clearing up temp files")
        deleteTempFiles()

        emit("Backup Successful!")

    }

    fun restoreBackup()=flow{

        emit("Downloading the backup file...")
        dropBox.downloadBackupFiles()

        clearAllTables()

        val workbook: Workbook = Workbook.getWorkbook(File(appPreferences.getLocalBackupFilePath()))

        emit("Restoring Receivers...")
        restoreReceivers(workbook)

        emit("Restoring Senders...")
        restoreSenders(workbook)

        emit("Restoring Groups...")
        restoreGroups(workbook)

        emit("Restoring Transactions")
        restoreTransactions(workbook)

        emit("Clearing up temp files")
        deleteTempFiles()

        emit("Restore Successful!")
    }


    @Throws(Exception::class)
    private fun writeReceiverToWorkbook(workbook: WritableWorkbook) {

        //prepare the list of receivers to backup
        val receivers: List<Receiver> = db.getReceiverDao().getAllReceiversNonLive()
        val sheet = workbook.createSheet("receivers", 0)

        //prepare the cell format for writing
        val contentFont = WritableFont(WritableFont.ARIAL, 10, WritableFont.NO_BOLD)
        val contentFormat = WritableCellFormat(contentFont)
        contentFormat.alignment = Alignment.CENTRE
        contentFormat.verticalAlignment = VerticalAlignment.BOTTOM
        contentFormat.wrap = false

        //write the receivers to the file
        var row = 0
        for (receiver in receivers) {
            if (receiver.email == null) //since its added in the later version, older objects may have null value
                receiver.email = ""
            sheet.addCell(Number(0, row, receiver.id.toDouble(), contentFormat))
            sheet.addCell(Label(1, row, receiver.accountType, contentFormat))
            sheet.addCell(Label(2, row, receiver.accountNumber, contentFormat))
            sheet.addCell(Label(3, row, receiver.name, contentFormat))
            sheet.addCell(Label(4, row, receiver.mobileNumber, contentFormat))
            sheet.addCell(Label(5, row, receiver.ifsc, contentFormat))
            sheet.addCell(Label(6, row, receiver.bank, contentFormat))
            sheet.addCell(Label(7, row, receiver.email, contentFormat))
            sheet.addCell(Label(8, row, receiver.displayName, contentFormat))
            row++
        }
        sheet.addCell(Label(0, row, "--end--", contentFormat))

    }

    @Throws(Exception::class)
    private fun writeSendersToWorkbook(workbook: WritableWorkbook) {

        //prepare the list of senders to backup
        val senders: List<Sender> = db.getSenderDao().getAllSendersNonLive()
        val sheet = workbook.createSheet("senders", 1)

        //prepare the cellformat for writing
        val contentFont = WritableFont(WritableFont.ARIAL, 10, WritableFont.NO_BOLD)
        val contentformat = WritableCellFormat(contentFont)
        contentformat.alignment = Alignment.CENTRE
        contentformat.verticalAlignment = VerticalAlignment.BOTTOM
        contentformat.wrap = false

        //write the senders to the file
        var row = 0
        for (sender in senders) {
            if (sender.email == null) //Since email was added in later version, older senders may have this as null
                sender.email = ""
            sheet.addCell(Number(0, row, sender.id.toDouble(), contentformat))
            sheet.addCell(Label(1, row, sender.accountType, contentformat))
            sheet.addCell(Label(2, row, sender.accountNumber, contentformat))
            sheet.addCell(Label(3, row, sender.name, contentformat))
            sheet.addCell(Label(4, row, sender.mobileNumber, contentformat))
            sheet.addCell(Label(5, row, sender.ifsc, contentformat))
            sheet.addCell(Label(6, row, sender.bank, contentformat))
            sheet.addCell(Label(7, row, sender.email, contentformat))
            sheet.addCell(Label(8, row, sender.displayName, contentformat))
            row++
        }
        sheet.addCell(Label(0, row, "--end--", contentformat))
    }

    @Throws(Exception::class)
    private fun writeTransactionsToWorkBook(workbook: WritableWorkbook) {

        //prepare the list of transactions to backup
        val transactions: List<Transaction> = db.getTransactionDao().getAllTransactionsNonLive()
        val sheet = workbook.createSheet("transactions", 3)

        //prepare the cellformat for writing
        val contentFont = WritableFont(WritableFont.ARIAL, 10, WritableFont.NO_BOLD)
        val contentformat = WritableCellFormat(contentFont)
        contentformat.alignment = Alignment.CENTRE
        contentformat.verticalAlignment = VerticalAlignment.BOTTOM
        contentformat.wrap = false

        //write the transactions to the file
        var row = 0
        for (trans in transactions) {
            sheet.addCell(Number(0, row, trans.id.toDouble(), contentformat))
            sheet.addCell(Number(1, row, trans.groupId.toDouble(), contentformat))
            sheet.addCell(Number(2, row, trans.senderId.toDouble(), contentformat))
            sheet.addCell(Number(3, row, trans.receiverId.toDouble(), contentformat))
            sheet.addCell(Number(4, row, trans.amount.toDouble(), contentformat))
            sheet.addCell(Label(5, row, trans.remarks, contentformat))
            row++
        }
        sheet.addCell(Label(0, row, "--end--", contentformat))

        //write the workbook and close it
        //workbook.write();
        //workbook.close();
    }

    @Throws(Exception::class)
    private fun writeGroupsToWorkBook(workbook: WritableWorkbook) {

        //prepare the list of groups to backup
        val groups: List<TransactionGroup> = db.getTransactionGroupDao().getAllGroupsNonLive()
        val sheet = workbook.createSheet("groups", 2)

        //prepare the cellformat for writing
        val contentFont = WritableFont(WritableFont.ARIAL, 10, WritableFont.NO_BOLD)
        val contentformat = WritableCellFormat(contentFont)
        contentformat.alignment = Alignment.CENTRE
        contentformat.verticalAlignment = VerticalAlignment.BOTTOM
        contentformat.wrap = false

        //write the groups to the file
        var row = 0
        for (group in groups) {
            sheet.addCell(Number(0, row, group.id.toDouble(), contentformat))
            sheet.addCell(Label(1, row, group.name, contentformat))
            sheet.addCell(Number(2, row, group.defaultSenderId.toDouble(), contentformat))
            row++
        }
        sheet.addCell(Label(0, row, "--end--", contentformat))

    }

    // region ********** Methods for backup
    @Throws(java.lang.Exception::class)
    private fun clearAllTables() {
        db.getReceiverDao().deleteAllReceivers()
        db.getSenderDao().deleteAllSenders()
        db.getTransactionGroupDao().deleteAllTransactionGroups()
        db.getTransactionDao().deleteAllTransactions()
    }

    @Throws(java.lang.Exception::class)
    private fun restoreReceivers(workbook: Workbook) {
        val receivers: MutableList<Receiver> = ArrayList()
        var notReachedEnd: Boolean
        var currentCell: Cell
        var currentReceiver: Receiver
        val sheet = workbook.getSheet(0)
        notReachedEnd = true
        var row = 0
        while (notReachedEnd) {
            currentCell = sheet.getCell(0, row)
            if (currentCell.contents == "--end--") {
                notReachedEnd = false
                break
            }
            currentReceiver = Receiver()
            currentReceiver.id = sheet.getCell(0, row).contents.toInt()
            currentReceiver.accountType = sheet.getCell(1, row).contents
            currentReceiver.accountNumber = sheet.getCell(2, row).contents
            currentReceiver.name = sheet.getCell(3, row).contents
            currentReceiver.mobileNumber = sheet.getCell(4, row).contents
            currentReceiver.ifsc = sheet.getCell(5, row).contents
            currentReceiver.bank = sheet.getCell(6, row).contents
            currentReceiver.email = sheet.getCell(7, row).contents

            /*
            A try block is required because this field wont exist in the backup file
            if the backup file is for older database version than the current version. So,
            in case the display name field is not there in the backup file, simply migrate by making
            the display name same as the account name field
             */try {
                currentReceiver.displayName = sheet.getCell(8, row).contents
            } catch (e: java.lang.Exception) {
                currentReceiver.displayName = currentReceiver.name
            }
            receivers.add(currentReceiver)
            row++
        }
        db.getReceiverDao().addAllReceivers(receivers)
    }

    @Throws(java.lang.Exception::class)
    private fun restoreSenders(workbook: Workbook) {
        val senders: MutableList<Sender> = ArrayList()
        var notReachedEnd: Boolean
        var currentCell: Cell
        var currentSender: Sender
        val sheet = workbook.getSheet(1)
        notReachedEnd = true
        var row = 0
        while (notReachedEnd) {
            currentCell = sheet.getCell(0, row)
            if (currentCell.contents == "--end--") {
                notReachedEnd = false
                break
            }
            currentSender = Sender()
            currentSender.id = sheet.getCell(0, row).contents.toInt()
            currentSender.accountType = sheet.getCell(1, row).contents
            currentSender.accountNumber = sheet.getCell(2, row).contents
            currentSender.name = sheet.getCell(3, row).contents
            currentSender.mobileNumber = sheet.getCell(4, row).contents
            currentSender.ifsc = sheet.getCell(5, row).contents
            currentSender.bank = sheet.getCell(6, row).contents
            currentSender.email = sheet.getCell(7, row).contents

            /*
            A try block is required because this field wont exist in the backup file
            if the backup file is for older database version than the current version. So,
            in case the display name field is not there in the backup file, simply migrate by making
            the display name same as the account name field
             */try {
                currentSender.displayName = sheet.getCell(8, row).contents
            } catch (e: java.lang.Exception) {
                currentSender.displayName = currentSender.name
            }
            senders.add(currentSender)
            row++
        }
        db.getSenderDao().addAllSenders(senders)
    }

    @Throws(java.lang.Exception::class)
    private fun restoreGroups(workbook: Workbook) {
        val groups: MutableList<TransactionGroup> = ArrayList()
        var notReachedEnd: Boolean
        var currentCell: Cell
        var currentGroup: TransactionGroup
        val sheet = workbook.getSheet(2)
        notReachedEnd = true
        var row = 0
        while (notReachedEnd) {
            currentCell = sheet.getCell(0, row)
            if (currentCell.contents == "--end--") {
                notReachedEnd = false
                break
            }
            currentGroup = TransactionGroup()
            currentGroup.id = sheet.getCell(0, row).contents.toInt()
            currentGroup.name = sheet.getCell(1, row).contents

            /*
            Using a special try catch block here. Because the defaultSenderId feature is added later
            in database migration Version 3. So, if we are restoring a old Version 2 database backup,
            then the column defaultSenderId wont exist there and thus will cause Index Out of bounds
            exception. We are catching that case manually if an exception occurs, then we are simply
            restoring a default value of 0
             */try {
                val defaultSender = sheet.getCell(2, row).contents
                currentGroup.defaultSenderId = defaultSender.toInt()
            } catch (e: ArrayIndexOutOfBoundsException) {
                currentGroup.defaultSenderId = 0
            }
            groups.add(currentGroup)
            row++
        }
        db.getTransactionGroupDao().addAllTransactionGroups(groups)
    }

    @Throws(java.lang.Exception::class)
    private fun restoreTransactions(workbook: Workbook) {
        val transactions: MutableList<Transaction> = ArrayList()
        var notReachedEnd: Boolean
        var currentCell: Cell
        var currentTrans: Transaction
        val sheet = workbook.getSheet(3)
        notReachedEnd = true
        var row = 0
        while (notReachedEnd) {
            currentCell = sheet.getCell(0, row)
            if (currentCell.contents == "--end--") {
                notReachedEnd = false
                break
            }
            currentTrans = Transaction()
            currentTrans.id = sheet.getCell(0, row).contents.toInt()
            currentTrans.groupId = sheet.getCell(1, row).contents.toInt()
            currentTrans.senderId = sheet.getCell(2, row).contents.toInt()
            currentTrans.receiverId = sheet.getCell(3, row).contents.toInt()
            currentTrans.amount = sheet.getCell(4, row).contents.toInt()
            currentTrans.remarks = sheet.getCell(5, row).contents
            transactions.add(currentTrans)
            row++
        }
        db.getTransactionDao().addAllTransactions(transactions)
    }

    @Throws(java.lang.Exception::class)
    private fun deleteTempFiles() {
        val file= File(appPreferences.getLocalBackupFilePath())
        if (file.exists()) file.delete()
    }


}