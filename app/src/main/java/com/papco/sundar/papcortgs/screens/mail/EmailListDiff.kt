package com.papco.sundar.papcortgs.screens.mail

import androidx.recyclerview.widget.DiffUtil
import com.papco.sundar.papcortgs.database.pojo.CohesiveTransaction

class EmailListDiff: DiffUtil.ItemCallback<CohesiveTransaction>() {

    override fun areItemsTheSame(
        oldItem: CohesiveTransaction, newItem: CohesiveTransaction
    ): Boolean {
        return oldItem.transaction.id == newItem.transaction.id
    }

    override fun areContentsTheSame(
        oldItem: CohesiveTransaction, newItem: CohesiveTransaction
    ): Boolean {
        return oldItem.transaction.mailSent==newItem.transaction.mailSent
    }

}