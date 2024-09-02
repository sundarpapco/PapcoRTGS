package com.papco.sundar.papcortgs.screens.mail

import android.annotation.SuppressLint
import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LiveData
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.papco.sundar.papcortgs.PapcoRTGSApp
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.database.common.MasterDatabase
import com.papco.sundar.papcortgs.database.pojo.CohesiveTransaction
import com.papco.sundar.papcortgs.extentions.isInternetConnected
import com.papco.sundar.papcortgs.extentions.weHaveNotificationPermission
import kotlinx.coroutines.flow.first

class MailWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    companion object {

        private const val WORK_NAME = "com.sivakasi.papco.papcoRTGS.emailWork"
        const val GROUP_ID = "groupId"
        private const val NOTIFICATION_ID_PROGRESS = 1
        private const val NOTIFICATION_ID_FAILURE = 2

        fun startWith(context: Context, groupId: Int = -1) {
            val request = OneTimeWorkRequestBuilder<MailWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setInputData(
                    workDataOf(
                        GROUP_ID to groupId
                    )
                )
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.KEEP,
                request
            )
        }

        fun getWorkStatusLiveData(context: Context): LiveData<List<WorkInfo>> {
            return WorkManager.getInstance(context).getWorkInfosForUniqueWorkLiveData(WORK_NAME)
        }
    }

    private val notificationBuilder by lazy { createNotificationBuilder() }
    private val mailDispatcher = MailDispatcher(applicationContext)
    private val database = MasterDatabase.getInstance(applicationContext)

    override suspend fun doWork(): Result {

        if(!applicationContext.weHaveNotificationPermission())
            return Result.failure()

        if(!applicationContext.isInternetConnected()){
            postFailureNotification(applicationContext.getString(R.string.check_internet_connection))
            return Result.failure()
        }

        return try {
            sendMails()
            Result.success()
        } catch (e: Exception) {
            postFailureNotification(
                e.message ?: applicationContext.getString(R.string.unknown_error)
            )
            Result.failure()
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {

        val contentText = applicationContext.getString(R.string.sending_email_intimation)

        val notification = notificationBuilder.apply {
            setProgress(0, 0, true).build()
            setContentText(contentText)
        }.build()

        return ForegroundInfo(NOTIFICATION_ID_PROGRESS,notification)
    }

    private suspend fun sendMails() {

        val mailingList = fetchTransactionsToMail()

        run mailSendingBlock@{
            mailingList.forEachIndexed { index, recipient ->

                if (isStopped) return@mailSendingBlock
                if(recipient.transaction.mailSent==1) return@forEachIndexed

                updateProgressNotification(index + 1, mailingList.size)
                if (recipient.receiver.email.isNotEmpty()) {
                    val sendingSuccess = mailDispatcher.dispatchEmail(recipient)
                    if (sendingSuccess) database.transactionDao.updateMailSentStatus(
                        recipient.transaction.id, 1
                    )
                } else {
                    database.transactionDao.updateMailSentStatus(recipient.transaction.id, 2)
                }
            }
        }
    }

    private suspend fun fetchTransactionsToMail(): List<CohesiveTransaction> {
        return database.transactionDao.getAllCohesiveTransactionsOfGroup(getGroupId()).first()
    }


    private fun updateProgressNotification(currentProgress: Int, maxProgress: Int) {

        require(currentProgress <= maxProgress) { "Invalid progress detected" }

        val contentText = applicationContext.getString(
            R.string.intimation_sending_status, currentProgress, maxProgress
        )

        val notification = notificationBuilder.apply {
            setProgress(maxProgress - 1, currentProgress - 1, false).build()
            setContentText(contentText)
        }.build()

        notify(notification)

    }

    private fun postFailureNotification(reason: String) {
        val notification =
            NotificationCompat.Builder(applicationContext, PapcoRTGSApp.CHANNEL_ID_INTIMATION)
                .apply {
                    setContentTitle(applicationContext.getString(R.string.sending_email_failed))
                    setContentText(reason)
                    setSmallIcon(R.drawable.app_icon)
                    priority = NotificationCompat.PRIORITY_DEFAULT
                    setAutoCancel(true)
                }.build()

        notify(notification, NOTIFICATION_ID_FAILURE)
    }

    @SuppressLint("MissingPermission")
    private fun notify(notification: Notification, id: Int = NOTIFICATION_ID_PROGRESS) {
        NotificationManagerCompat.from(applicationContext).apply {
            notify(id, notification)
        }
    }

    private fun createNotificationBuilder(): NotificationCompat.Builder {

        return NotificationCompat.Builder(applicationContext, PapcoRTGSApp.CHANNEL_ID_INTIMATION)
            .apply {
                setContentTitle(applicationContext.getString(R.string.sending_email_intimation))
                setProgress(0, 100, true)
                setSmallIcon(R.drawable.app_icon)
                foregroundServiceBehavior = NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE
                priority = NotificationCompat.PRIORITY_DEFAULT

                val cancelIntent =
                    WorkManager.getInstance(applicationContext).createCancelPendingIntent(id)
                addAction(
                    R.drawable.ic_close, applicationContext.getString(R.string.cancel), cancelIntent
                )
            }

    }

    private fun getGroupId(): Int {
        val groupId = inputData.getInt(GROUP_ID, -1)
        require(groupId != -1) {
            "Group ID not set for the Mail Worker"
        }
        return groupId
    }
}