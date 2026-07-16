package com.papco.sundar.papcortgs.database.receiver

import androidx.core.text.isDigitsOnly
import androidx.room.Entity
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

    fun hasValidMobileNumber(): Boolean {
        if (mobileNumber.trim { it <= ' ' }.length != 10) return false
        return mobileNumber.isDigitsOnly()
    }
}

