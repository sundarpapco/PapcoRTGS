package com.papco.sundar.papcortgs.database.sender

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.papco.sundar.papcortgs.database.pojo.PartyListItem

@Entity
class Sender : PartyListItem {
    @PrimaryKey(autoGenerate = true)
    override var id: Int = 0
    var accountType: String = ""
    var accountNumber: String = ""
    override var name: String = ""
    var mobileNumber: String = ""
    var ifsc: String = ""
    var bank: String = ""
    var email: String = ""

    @Ignore
    override var searchText: String = ""

    @Ignore
    override var disabled: Boolean = false

    override fun toString(): String {
        return displayName!!
    }
}
