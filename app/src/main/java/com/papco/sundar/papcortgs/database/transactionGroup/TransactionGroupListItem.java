package com.papco.sundar.papcortgs.database.transactionGroup;


import androidx.room.Embedded;
import androidx.room.Relation;

import com.papco.sundar.papcortgs.database.sender.Sender;

public class TransactionGroupListItem {

    @Embedded
    public TransactionGroup transactionGroup;
    @Relation(parentColumn = "defaultSenderId",entityColumn = "id")
    public Sender sender;

}
