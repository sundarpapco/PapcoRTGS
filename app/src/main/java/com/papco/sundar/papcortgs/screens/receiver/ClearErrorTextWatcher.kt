package com.papco.sundar.papcortgs.screens.receiver

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText


class ClearErrorTextWatcher(private val editText:EditText):TextWatcher{
    override fun afterTextChanged(s: Editable?) {
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        editText.error=null
    }

}