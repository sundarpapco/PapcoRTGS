package com.papco.sundar.papcortgs.database.receiver

import android.text.SpannableString
import android.text.TextUtils
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
class Receiver {
    @PrimaryKey(autoGenerate = true)
    var id = 0
    var accountType: String = ""
    var accountNumber: String = ""
    var name: String = ""
    var displayName: String = name
    var mobileNumber: String = ""
    var ifsc: String = ""
    var bank: String = ""
    var email = ""

    @Ignore
    var highlightedName: SpannableString? = null

    fun hasValidMobileNumber(): Boolean {
        if (mobileNumber.trim { it <= ' ' }.length != 10) return false
        return TextUtils.isDigitsOnly(mobileNumber)
    }
}

