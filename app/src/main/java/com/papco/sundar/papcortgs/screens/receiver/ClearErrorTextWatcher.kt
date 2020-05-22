package com.papco.sundar.papcortgs.screens.receiver

import android.support.design.widget.TextInputLayout
import android.text.Editable
import android.text.TextWatcher


class ClearErrorTextWatcher(private val layout:TextInputLayout):TextWatcher{
    override fun afterTextChanged(s: Editable?) {
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        layout.error=null
        layout.isErrorEnabled=false
    }

}