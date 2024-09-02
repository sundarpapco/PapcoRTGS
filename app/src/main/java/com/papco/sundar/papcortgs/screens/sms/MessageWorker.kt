package com.papco.sundar.papcortgs.screens.sms

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
import com.papco.sundar.papcortgs.extentions.weHaveNotificationPermission
import com.papco.sundar.papcortgs.settings.AppPreferences
import kotlinx.coroutines.flow.first

class MessageWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    companion object {

        private const val WORK_NAME = "com.sivakasi.papco.papcoRTGS.messagingWork"
        const val GROUP_ID = "groupId"
        private const val NOTIFICATION_ID_PROGRESS = 1
        private const val NOTIFICATION_ID_FAILURE = 2

        fun startWith(context: Context, groupId: Int = -1) {
            val request = OneTimeWorkRequestBuilder<MessageWorker>()
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
    private val database = MasterDatabase.getInstance(applicationContext)

    override suspend fun doWork(): Result {
        if(!applicationContext.weHaveNotificationPermission())
            return Result.failure()


        return try {
            sendMessages()
            cancelProgressNotification()
            Result.success()
        } catch (e: Exception) {
            postFailureNotification(
                e.message ?: applicationContext.getString(R.string.unknown_error)
            )
            Result.failure()
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {

        val contentText = applicationContext.getString(R.string.sending_sms_intimation)

        val notification = notificationBuilder.apply {
            setProgress(0, 0, true).build()
            setContentText(contentText)
        }.build()

        return ForegroundInfo(NOTIFICATION_ID_PROGRESS,notification)
    }

    private suspend fun sendMessages() {

        val mailingList = fetchTransactionsToMail()

        if(mailingList.isEmpty())
            return

        val messageDispatcher = MessageDispatcher(
            applicationContext,
            AppPreferences(applicationContext),
            mailingList
        )

        updateProgressNotification(0,mailingList.size)

        messageDispatcher.dispatchMessages()
            .collect{
                val processedResult=processDispatchResult(it)
                database.transactionDao.updateMessageSentStatus(it.transactionId,processedResult.code)
                updateProgressNotification(
                    messageDispatcher.dispatchCount,messageDispatcher.totalCount)
            }
    }

    private fun processDispatchResult(result:MessageDispatchResult):MessageSentStatus{

        return when(result.status){
            MessageSentStatus.SENT->MessageSentStatus.SENT
            MessageSentStatus.ALREADY_SENT->MessageSentStatus.SENT
            MessageSentStatus.NOT_SENT->MessageSentStatus.NOT_SENT
            MessageSentStatus.INVALID_NUMBER->MessageSentStatus.INVALID_NUMBER
            else->MessageSentStatus.NOT_SENT
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

    private fun cancelProgressNotification(){
        NotificationManagerCompat.from(applicationContext).apply {
            cancel(NOTIFICATION_ID_PROGRESS)
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
            "Group ID not set for the Message Worker"
        }
        return groupId
    }
}