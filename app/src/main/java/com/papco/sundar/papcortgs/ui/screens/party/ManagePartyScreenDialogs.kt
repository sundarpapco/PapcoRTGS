package com.papco.sundar.papcortgs.ui.screens.party

import com.papco.sundar.papcortgs.database.pojo.Party

sealed interface ManagePartyScreenDialogs{
    data object WaitDialog:ManagePartyScreenDialogs
    class DeletePartyDialog(val party: Party):ManagePartyScreenDialogs
}

