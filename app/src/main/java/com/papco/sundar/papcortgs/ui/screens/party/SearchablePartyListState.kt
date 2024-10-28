package com.papco.sundar.papcortgs.ui.screens.party

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.papco.sundar.papcortgs.database.pojo.Party
import kotlinx.coroutines.flow.MutableStateFlow

class SearchablePartyListState {

    var query = MutableStateFlow("")
    var data:List<Party>? by mutableStateOf(null)

}