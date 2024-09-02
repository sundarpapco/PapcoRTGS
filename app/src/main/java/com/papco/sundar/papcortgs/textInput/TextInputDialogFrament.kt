package com.papco.sundar.papcortgs.textInput

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.common.ResultDialogFragment
import com.papco.sundar.papcortgs.databinding.DialogTextInputBinding

class TextInputDialogFragment:ResultDialogFragment() {

    private var _viewBinding:DialogTextInputBinding?=null
    private val viewBinding:DialogTextInputBinding
        get() = _viewBinding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _viewBinding= DialogTextInputBinding.inflate(inflater,container,false)
        return viewBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding=null
    }

    inner class Builder(private val context:Context){

        var title=context.getString(R.string.default_text_input_dialog_title)
        var defaultText=""
        var okButtonText=context.getString(R.string.ok)
        var cancelButtonText=context.getString(R.string.cancel)
        var cancelButtonEnabled=true
        var allowBlankText=false

    }
}