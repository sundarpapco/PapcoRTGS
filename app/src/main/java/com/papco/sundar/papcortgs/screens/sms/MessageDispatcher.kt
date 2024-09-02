package com.papco.sundar.papcortgs.screens.sms

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.telephony.SmsManager
import com.papco.sundar.papcortgs.common.TextFunctions
import com.papco.sundar.papcortgs.database.pojo.CohesiveTransaction
import com.papco.sundar.papcortgs.database.transaction.Transaction
import com.papco.sundar.papcortgs.settings.AppPreferences
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds


class MessageDispatcher(
    private val context: Context,
    private val appPreferences: AppPreferences,
    private val transactions: List<CohesiveTransaction>
) {

    private val smsManager = context.getSystemService(SmsManager::class.java)
    private val messageList = prepareList()

    private var iterator = messageList.listIterator()
    private var lastDispatchedId: Int = -1

    private val hasNext: Boolean
        get() = iterator.hasNext()

    val dispatchCount: Int
        get() = iterator.previousIndex() + 1

    val totalCount: Int
        get() = messageList.size

    private suspend fun sendNextMessage(): MessageSentStatus {
        require(hasNext) { "No more transactions to send message to" }
        val trans = iterator.next()
        val result = dispatchMessage(trans)
        lastDispatchedId = trans.transaction.id
        return result
    }

    private suspend fun dispatchMessage(trans: CohesiveTransaction): MessageSentStatus {

        //we have a valid number. send the message
        val requestCode = trans.transaction.id
        val sentIntent = Intent(SmsBroadcastReceiver.SMS_SENT_ACTION)
        val number = trans.receiver.mobileNumber
        val message = composeMessage(trans)
        sentIntent.putExtra(SmsBroadcastReceiver.EXTRA_TRANS_ID, trans.transaction.id)


        // Construct the PendingIntents for the results.
        // FLAG_ONE_SHOT cancels the PendingIntent after use so we
        // can safely reuse the request codes in subsequent runs.
        val sentPI = PendingIntent.getBroadcast(
            context,
            requestCode,
            sentIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        return try {
            // Send our message.
            if (message.length > 160) {
                val parts = smsManager.divideMessage(message)
                val sentIntents = ArrayList<PendingIntent>()
                sentIntents.add(sentPI)
                smsManager.sendMultipartTextMessage(number, null, parts, sentIntents, null)
                MessageSentStatus.SENT
            } else {
                smsManager.sendTextMessage(number, null, message, sentPI, null)
                MessageSentStatus.SENT
            }
        } catch (e: Exception) {
            e.printStackTrace()
            MessageSentStatus.UNKNOWN_ERROR
        }
    }

    private suspend fun composeMessage(trans: CohesiveTransaction): String {

        var format = appPreferences.getMessageTemplate().first()
        format = format!!.replace(TextFunctions.TAG_RECEIVER_ACC_NAME, trans.receiver.name)
        format = format.replace(
            TextFunctions.TAG_RECEIVER_ACC_NUMBER, trans.receiver.accountNumber
        )
        format = format.replace(
            TextFunctions.TAG_AMOUNT, Transaction.amountAsString(trans.transaction.amount)
        )
        format = format.replace(TextFunctions.TAG_RECEIVER_BANK, trans.receiver.bank)
        format = format.replace(TextFunctions.TAG_RECEIVER_IFSC, trans.receiver.ifsc)
        format = format.replace(TextFunctions.TAG_SENDER_NAME, trans.sender.name)
        return format
    }


    @OptIn(FlowPreview::class)
    suspend fun dispatchMessages() = channelFlow {
        coroutineScope {
            launch {

                receiverFlow().timeout(3000.milliseconds).catch { e ->
                        if (e is TimeoutCancellationException) {
                            emit(
                                MessageDispatchResult(lastDispatchedId, MessageSentStatus.TIMEOUT)
                            )
                        }
                    }.collect {
                        trySend(it)
                        if (hasNext) {
                            sendNextMessage()
                        } else cancel()
                    }

            }

            kotlinx.coroutines.delay(500)
            if (hasNext) sendNextMessage()
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private fun receiverFlow() = callbackFlow {

        val receiver = SmsBroadcastReceiver {
            trySend(it)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            context.registerReceiver(
                receiver,
                IntentFilter(SmsBroadcastReceiver.SMS_SENT_ACTION),
                Context.RECEIVER_NOT_EXPORTED
            )
        }
        else{
            context.registerReceiver(receiver, IntentFilter(SmsBroadcastReceiver.SMS_SENT_ACTION))
        }

        awaitClose { (context.unregisterReceiver(receiver)) }
    }

    private fun prepareList(): List<CohesiveTransaction> {
        return transactions.filter {
                it.transaction.messageSent != MessageSentStatus.SENT.code && it.receiver.hasValidMobileNumber()
            }
    }

}