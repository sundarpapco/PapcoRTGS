package com.papco.sundar.papcortgs.ui.screens.party

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.papco.sundar.papcortgs.database.pojo.Party
import com.papco.sundar.papcortgs.database.pojo.PartyListItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

open class SearchablePartyListState{

    val query = MutableStateFlow("")
    var data:List<PartyListItem>? by mutableStateOf(null)

}