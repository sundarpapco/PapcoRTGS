package com.papco.sundar.papcortgs.screens.password

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.common.ResultDialogFragment
import com.papco.sundar.papcortgs.databinding.DialogTextInputBinding

class PasswordDialog : ResultDialogFragment() {

    companion object {

        private const val KEY_CODE = "REQUEST_CODE"
        private const val PASSWORD = "papco1954"
        const val TAG = "PasswordDialog"

        fun getInstance(code: Int): PasswordDialog {
            return PasswordDialog().apply {
                val args = Bundle()
                args.putInt(KEY_CODE, code)
                arguments = args
            }
        }

        fun getArgumentBundle(code: Int): Bundle {
            return Bundle().also {
                it.putInt(KEY_CODE, code)
            }
        }
    }

    private var _viewBinding: DialogTextInputBinding? = null
    private val viewBinding: DialogTextInputBinding
        get() = _viewBinding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _viewBinding = DialogTextInputBinding.inflate(inflater, container, false)
        return viewBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }


    private fun initViews() {
        viewBinding.textInputDialogTitle.text = "Enter Password"
        viewBinding.textInputDialogText.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        viewBinding.textInputDialogText.hint="Password"

        viewBinding.textInputDialogOk.setOnClickListener {
            if (enteredPassword() == PASSWORD) {
                if (dispatchResult(true, getCode())) dismiss()
            } else Toast.makeText(
                requireContext(),
                getString(R.string.incorrect_password),
                Toast.LENGTH_SHORT
            ).show()
        }

        viewBinding.textInputDialogCancel.setOnClickListener {
            dismiss()
        }

        showKeyboard()

    }

    private fun showKeyboard() {
        val inputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        viewBinding.textInputDialogText.requestFocus()
        inputMethodManager.showSoftInput(viewBinding.textInputDialogText, 0)

    }


    private fun enteredPassword(): String {
        return viewBinding.textInputDialogText.text.toString().trim()
    }

    private fun getCode(): Int {
        return arguments?.getInt(KEY_CODE) ?: error("Invalid argument code in password dialog")
    }
}
