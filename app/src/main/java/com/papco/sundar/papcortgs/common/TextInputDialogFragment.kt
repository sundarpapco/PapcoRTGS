package com.papco.sundar.papcortgs.common

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.papco.sundar.papcortgs.R

class TextInputDialogFragment : androidx.fragment.app.DialogFragment() {

    companion object {
        const val TAG = "Tag:Number:input:dialog:fragment"
    }

    private val defaultTitle = "Enter text"
    private val defaultHint = "Text"
    private val defaultOkButtonText = "OK"
    private val defaultResponseCode = 0
    private val defaultInitialValue = ""

    private lateinit var title: TextView
    private lateinit var enteredText: EditText
    private lateinit var okButton: TextView


    override fun onResume() {
        dialog?.window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
        )
        super.onResume()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_text_input, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        linkViews(view)
        initViews()

        okButton.setOnClickListener {
            onOkButtonClicked()
        }
    }

    private fun linkViews(view: View) {
        title = view.findViewById(R.id.text_input_dialog_title)
        enteredText = view.findViewById(R.id.text_input_dialog_text)
        okButton = view.findViewById(R.id.text_input_dialog_ok)
    }

    private fun initViews() {
        title.text = getTitle()
        enteredText.hint = getHint()
        enteredText.setText(getInitialValue())
        okButton.text = getOkButtonText()
        enteredText.requestFocus()
    }

    private fun onOkButtonClicked() {

        val userText = enteredText.text.toString().trim()

        if (!validateText(userText))
            return

        if (tryToDispatchResult(userText))
            dismiss()

    }

    private fun tryToDispatchResult(enteredText: String): Boolean {

        val callback = getCallBack()

        return if (callback == null)
            false
        else {
            callback.onTextEntered(enteredText, getResponseCode())
            true
        }
    }

    private fun validateText(enteredText: String): Boolean {
        val callback = getCallBack()
        return callback?.onValidate(enteredText) ?: false
    }

    private fun getCallBack(): TextInputListener? {

        var callback: TextInputListener? = null

        try {
            callback = when {
                parentFragment != null -> {
                    parentFragment as TextInputListener
                }
                activity != null -> {
                    activity as TextInputListener
                }
                else -> {
                    toast("Should be called either from Activity or fragment")
                    null
                }
            }
        } catch (exception: Exception) {
            toast("Caller should implement Text Input Listener")
        }

        return callback
    }

    private fun getTitle(): String = arguments?.getString(Builder.KEY_TITLE) ?: defaultTitle

    private fun getHint(): String = arguments?.getString(Builder.KEY_HINT) ?: defaultHint

    private fun getOkButtonText(): String =
            arguments?.getString(Builder.KEY_OK_BUTTON_TEXT) ?: defaultOkButtonText

    private fun getResponseCode(): Int =
            arguments?.getInt(Builder.KEY_RESPONSE_CODE) ?: defaultResponseCode

    private fun getInitialValue(): String =
            arguments?.getString(Builder.KEY_INITIAL_VALUE) ?: defaultInitialValue


    class Builder {

        companion object {
            const val KEY_TITLE = "numberInputDialog:key:title"
            const val KEY_HINT = "numberInputDialog:key:hint"
            const val KEY_OK_BUTTON_TEXT = "numberInputDialog:okButton:text"
            const val KEY_RESPONSE_CODE = "numberInputDialog:ResponseCode"
            const val KEY_INITIAL_VALUE = "numberInputDialog:InitialValue"
        }

        var title = "Enter a number"
        var hint = "Enter a number"
        var okButtonText = "OK"
        var responseCode = 0
        var initialValue: String = ""

        fun build(): TextInputDialogFragment {


            val args = Bundle().apply {
                putString(KEY_TITLE, title)
                putString(KEY_HINT, hint)
                putString(KEY_OK_BUTTON_TEXT, okButtonText)
                putInt(KEY_RESPONSE_CODE, responseCode)
                putString(KEY_INITIAL_VALUE, initialValue)
            }

            return TextInputDialogFragment().apply {
                arguments = args
            }

        }

    }

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    interface TextInputListener {
        fun onValidate(enteredText: String): Boolean
        fun onTextEntered(text: String, responseCode: Int)
    }
}