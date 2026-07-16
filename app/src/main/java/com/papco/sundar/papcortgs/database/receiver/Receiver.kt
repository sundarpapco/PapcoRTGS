package com.papco.sundar.papcortgs.database.receiver

import androidx.core.text.isDigitsOnly
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.papco.sundar.papcortgs.database.pojo.PartyListItem

@Entity
class Receiver : PartyListItem{
    @PrimaryKey(autoGenerate = true)
    override var id = 0
    var accountType: String = ""
    var accountNumber: String = ""
    override var name: String = ""
    var mobileNumber: String = ""
    var ifsc: String = ""
    var bank: String = ""
    var email = ""

    @Ignore
    override var searchText: String = ""

    @Ignore
    override var disabled: Boolean = false

    fun hasValidMobileNumber(): Boolean {
        if (mobileNumber.trim { it <= ' ' }.length != 10) return false
        return mobileNumber.isDigitsOnly()
    }
}

