package com.papco.sundar.papcortgs.database.transaction

import android.text.TextUtils
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.papco.sundar.papcortgs.database.receiver.Receiver
import com.papco.sundar.papcortgs.database.sender.Sender
import com.papco.sundar.papcortgs.database.transactionGroup.TransactionGroup

@Entity(
    foreignKeys = [ForeignKey(
        entity = Sender::class,
        parentColumns = ["id"],
        childColumns = ["senderId"],
        onDelete = CASCADE
    ), ForeignKey(
        entity = Receiver::class,
        parentColumns = ["id"],
        childColumns = ["receiverId"],
        onDelete = CASCADE
    ), ForeignKey(
        entity = TransactionGroup::class,
        parentColumns = ["id"],
        childColumns = ["groupId"],
        onDelete = CASCADE
    )]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    var id:Int=0,
    var groupId:Int=0,
    var senderId:Int=0,
    var receiverId:Int=0,
    var amount: Int=0,
    var remarks:String?=null,
    var mailSent:Int=0,
    var messageSent:Int=0
) {
    @Ignore
    var sender: Sender? = null
    @Ignore
    var receiver: Receiver? = null
    @Ignore
    var smsStatus = -1

    companion object {
        @JvmStatic
        fun formatAmountAsString(amount: Int): String {
            val value = amount.toString()
            val lastDigit = value[value.length - 1]
            var result = ""
            val len = value.length - 1
            var nDigits = 0
            for (i in len - 1 downTo 0) {
                result = value[i].toString() + result
                nDigits++
                if (nDigits % 2 == 0 && i > 0) {
                    result = ",$result"
                }
            }
            return "\u20B9 $result$lastDigit"
        }

        @JvmStatic
        fun amountAsString(amount: Int): String {
            val value = amount.toString()
            val lastDigit = value[value.length - 1]
            var result = ""
            val len = value.length - 1
            var nDigits = 0
            for (i in len - 1 downTo 0) {
                result = value[i].toString() + result
                nDigits++
                if (nDigits % 2 == 0 && i > 0) {
                    result = ",$result"
                }
            }
            return "Rs.$result$lastDigit"
        }
    }
}
