package com.papco.sundar.papcortgs.screens.receiver

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem

class DisableEditTextPastingCallBack: ActionMode.Callback {
    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        return false
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        menu?.clear()
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode?) {

    }
}