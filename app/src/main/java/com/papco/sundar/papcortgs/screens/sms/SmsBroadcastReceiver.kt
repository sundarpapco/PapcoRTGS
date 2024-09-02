package com.papco.sundar.papcortgs.screens.sms

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.util.Log

class SmsBroadcastReceiver(
    private val onSmsReceived:(MessageDispatchResult)->Unit
) : BroadcastReceiver() {
    companion object{
        const val SMS_SENT_ACTION = "com.papco.sundar.papcortgs.SMS_SENT"
        const val EXTRA_TRANS_ID = "transId"
    }

    override fun onReceive(context: Context, intent: Intent) {

        Log.d("SAATVIK","Broadcast receiver received a intent")
        // This is the result for a send.
        if (SMS_SENT_ACTION == intent.action) {
            Log.d("SAATVIK","Broadcast receiver says its our message only")
            val transId = intent.getIntExtra(EXTRA_TRANS_ID, -1)
            onSmsReceived(
                MessageDispatchResult(transId,transcodeResultCode())
            )
        }

    }

    private fun transcodeResultCode():MessageSentStatus{

        return when (resultCode) {
            Activity.RESULT_OK -> MessageSentStatus.SENT
            SmsManager.RESULT_ERROR_LIMIT_EXCEEDED -> MessageSentStatus.LIMIT_EXCEEDED
            SmsManager.RESULT_ERROR_RADIO_OFF -> MessageSentStatus.NO_SERVICE
            SmsManager.RESULT_ERROR_NO_SERVICE -> MessageSentStatus.NO_SERVICE
            else -> MessageSentStatus.UNKNOWN_ERROR
        }

    }
}