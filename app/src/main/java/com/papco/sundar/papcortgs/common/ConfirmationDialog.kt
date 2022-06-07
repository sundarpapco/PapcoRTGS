package com.papco.sundar.papcortgs.common

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class ConfirmationDialog : DialogFragment() {

    companion object {
        private const val KEY_CONFIRMATION_ID = "key_confirmation_id"
        private const val DEFAULT_MSG = "Sure want to confirm?"
        private const val DEFAULT_POSITIVE_TEXT = "CONFIRM"
        private const val KEY_MSG = "Key_message"
        private const val KEY_POSITIVE_TEXT = "positive_text"
        private const val KEY_TITLE="key:title"
        private const val KEY_EXTRA="key:extra"
        private const val DEFAULT_ID = -1
        const val TAG = "com.papco.sundar.confirmation.dialog"

        fun getInstance(
            msg: String,
            positiveButtonText: String,
            confirmId: Int,
            title:String="",
            extra:String=""
        ): ConfirmationDialog {
            val args = Bundle()
            args.putString(KEY_MSG, msg)
            args.putString(KEY_POSITIVE_TEXT, positiveButtonText)
            args.putInt(KEY_CONFIRMATION_ID, confirmId)
            args.putString(KEY_TITLE,title)
            args.putString(KEY_EXTRA,extra)
            return ConfirmationDialog()
                .also { it.arguments = args }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage(getMessage())
        builder.setPositiveButton(getPositiveText()) { _, _ ->
            dispatchConfirmation()
        }
        builder.setNegativeButton("CANCEL", null)

        with(getTitle()){
            if(!isBlank())
                builder.setTitle(this)
        }

        return builder.create()
    }

    private fun dispatchConfirmation() {

        var callback:ConfirmationDialogListener?=null

        try {
            callback=when {
                parentFragment != null -> {
                    parentFragment as ConfirmationDialogListener
                }
                activity != null -> {
                    activity as ConfirmationDialogListener
                }
                else -> {
                    toast("Should be called either from Activity or fragment")
                    null
                }
            }
        } catch (exception: Exception) {
            toast("The caller should implement ConfirmationDialogListener")
        }

        callback?.onConfirmationDialogConfirm(getConfirmationId(),getExtra())
    }

    private fun getMessage(): String = arguments?.getString(KEY_MSG, DEFAULT_MSG) ?: DEFAULT_MSG


    private fun getPositiveText(): String =
        arguments?.getString(KEY_POSITIVE_TEXT, DEFAULT_POSITIVE_TEXT) ?: DEFAULT_POSITIVE_TEXT


    private fun getConfirmationId(): Int =
        arguments?.getInt(KEY_CONFIRMATION_ID, DEFAULT_ID) ?: DEFAULT_ID

    private fun getTitle():String =
        arguments?.getString(KEY_TITLE) ?: ""

    private fun getExtra():String =
        arguments?.getString(KEY_EXTRA) ?: ""


    interface ConfirmationDialogListener {
        fun onConfirmationDialogConfirm(confirmationId: Int,extra:String)
    }
}