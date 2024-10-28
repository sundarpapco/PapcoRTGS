package com.papco.sundar.papcortgs.screens.sms

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class SmsBroadcastReceiver(
    private val onSmsReceived:(MessageDispatchResult)->Unit
) : BroadcastReceiver() {
    companion object{
        const val SMS_SENT_ACTION = "com.papco.sundar.papcortgs.SMS_SENT"
        const val EXTRA_TRANS_ID = "transId"
    }

    override fun onReceive(context: Context, intent: Intent) {

        // This is the result for a send.
        if (SMS_SENT_ACTION == intent.action) {
            val transId = intent.getIntExtra(EXTRA_TRANS_ID, -1)
            val result=if(resultCode==Activity.RESULT_OK)
                MessageDispatcher.SENT
            else
                MessageDispatcher.ERROR

            if(transId != -1)
                onSmsReceived(MessageDispatchResult(transId,result))
        }
    }
}