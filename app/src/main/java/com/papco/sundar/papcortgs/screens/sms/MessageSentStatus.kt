package com.papco.sundar.papcortgs.screens.sms

import android.content.Context
import com.papco.sundar.papcortgs.R

enum class MessageSentStatus(val code: Int) {

    SENT(1),
    NOT_SENT(0),
    INVALID_NUMBER(4),
    LIMIT_EXCEEDED(5),
    NO_SERVICE(6),
    UNKNOWN_ERROR(7),
    ALREADY_SENT(8),
    TIMEOUT(9);

    companion object {
        fun descriptionFromCode(context: Context,code:Int): String {
            return when (code) {
                SENT.code -> context.getString(R.string.message_sent)
                NOT_SENT.code -> context.getString(R.string.message_not_sent)
                INVALID_NUMBER.code -> context.getString(R.string.message_number_not_valid)
                LIMIT_EXCEEDED.code -> context.getString(R.string.message_limit_exceeded)
                NO_SERVICE.code -> context.getString(R.string.message_no_service)
                UNKNOWN_ERROR.code -> context.getString(R.string.message_unknown_error)
                ALREADY_SENT.code -> context.getString(R.string.message_already_sent)
                TIMEOUT.code -> context.getString(R.string.message_time_out)
                else->error("Invalid code provided for MessageSentStatus")
            }
        }

        fun fromCode(code:Int):MessageSentStatus{
            return when(code){
                SENT.code -> SENT
                NOT_SENT.code -> NOT_SENT
                INVALID_NUMBER.code -> INVALID_NUMBER
                LIMIT_EXCEEDED.code -> LIMIT_EXCEEDED
                NO_SERVICE.code -> NO_SERVICE
                UNKNOWN_ERROR.code -> UNKNOWN_ERROR
                ALREADY_SENT.code -> ALREADY_SENT
                TIMEOUT.code -> TIMEOUT
                else->error("Invalid code provided for MessageSentStatus")
            }
        }
    }
}


class MessageDispatchResult(
    val transactionId: Int, val status: MessageSentStatus
)