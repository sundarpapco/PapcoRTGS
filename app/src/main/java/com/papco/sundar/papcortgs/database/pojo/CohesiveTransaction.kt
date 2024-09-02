package com.papco.sundar.papcortgs.database.pojo

import androidx.room.Embedded
import androidx.room.Relation
import com.papco.sundar.papcortgs.database.receiver.Receiver
import com.papco.sundar.papcortgs.database.sender.Sender
import com.papco.sundar.papcortgs.database.transaction.Transaction

data class CohesiveTransaction(
    @Embedded val transaction: Transaction,

    @Relation(
        parentColumn = "senderId",
        entityColumn = "id"
    )
    val sender:Sender,

    @Relation(
        parentColumn = "receiverId",
        entityColumn = "id"
    )
    val receiver:Receiver
)