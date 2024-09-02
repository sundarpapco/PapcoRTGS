package com.papco.sundar.papcortgs.screens.sender

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter.AllCaps
import android.text.InputFilter.LengthFilter
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.database.sender.Sender
import com.papco.sundar.papcortgs.databinding.FragmentNewEditPersonBinding
import com.papco.sundar.papcortgs.extentions.enableBackArrow
import com.papco.sundar.papcortgs.extentions.updateTitle
import com.papco.sundar.papcortgs.screens.receiver.ClearErrorTextWatcher
import com.papco.sundar.papcortgs.screens.receiver.DisableEditTextPastingCallBack
import java.util.regex.Matcher
import java.util.regex.Pattern

class CreateSenderFragment : Fragment() {

    companion object {
        private const val KEY_SENDER_ID = "key_editing_sender_id"
        const val EVENT_SUCCESS = "EVENT_SUCCESS"
        fun getArgumentBundle(editingSenderId: Int): Bundle {
            val args = Bundle()
            args.putInt(KEY_SENDER_ID, editingSenderId)
            return args
        }
    }

    private val isEditMode: Boolean
        get() = editingSenderId() != -1

    val viewModel: CreateSenderVM by lazy {
        ViewModelProvider(this)[CreateSenderVM::class.java]
    }

    private var _viewBinding: FragmentNewEditPersonBinding? = null
    private val viewBinding: FragmentNewEditPersonBinding
        get() = _viewBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isEditMode) viewModel.loadSender(editingSenderId())
    }

    private fun registerOptionsMenu() {
        val menuProvider = object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_done, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == R.id.action_done) {
                    validateAndSave()
                    return true
                }

                if(menuItem.itemId==android.R.id.home){
                    findNavController().popBackStack()
                    return true
                }

                return false
            }
        }

        requireActivity().addMenuProvider(menuProvider, viewLifecycleOwner)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _viewBinding = FragmentNewEditPersonBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableBackArrow()
        registerOptionsMenu()
        initViews()
        observeViewModel()
        if (isEditMode) updateTitle("Update Sender")
        else updateTitle("Create Sender")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }

    private fun initViews() {

        with(viewBinding) {
            senderAccountName.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence, start: Int, count: Int, after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable) {
                    senderAccountName.error = null
                }
            })
            senderDisplayName.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence, start: Int, count: Int, after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable) {
                    senderDisplayName.error = null
                }
            })
            senderAccountNumber.addTextChangedListener(ClearErrorTextWatcher(senderAccountNumber))
            senderAccountNumber.addTextChangedListener(
                ClearErrorTextWatcher(
                    senderConfirmAccountNumber
                )
            )
            senderAccountNumber.onFocusChangeListener =
                CheckSameContentListener(senderConfirmAccountNumber)

            senderConfirmAccountNumber.addTextChangedListener(
                ClearErrorTextWatcher(
                    senderConfirmAccountNumber
                )
            )
            senderConfirmAccountNumber.customSelectionActionModeCallback =
                DisableEditTextPastingCallBack()
            senderConfirmAccountNumber.isLongClickable = false
            senderConfirmAccountNumber.setTextIsSelectable(false)
            senderConfirmAccountNumber.onFocusChangeListener =
                CheckSameContentListener(senderAccountNumber)

            senderAccountType.addTextChangedListener(ClearErrorTextWatcher(senderAccountType))
            senderIfsc.addTextChangedListener(ClearErrorTextWatcher(senderIfsc))

            val filters = arrayOf(AllCaps(), LengthFilter(11))
            senderIfsc.text?.filters = filters
            senderIfsc.setOnClickListener {
                if (senderIfsc.selectionStart < 4) senderIfsc.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS else senderIfsc.inputType =
                    InputType.TYPE_CLASS_NUMBER
            }
            senderBank.addTextChangedListener(ClearErrorTextWatcher(senderBank))
            senderBank.addTextChangedListener(ClearErrorTextWatcher(senderBank))
        }
    }

    private fun observeViewModel() {

        viewModel.sender.observe(viewLifecycleOwner) { senderEvent ->
            if (!senderEvent.isAlreadyHandled) loadValues(senderEvent.handleEvent())
        }

        viewModel.eventStatus.observe(viewLifecycleOwner) { event ->
            if (event.isAlreadyHandled) return@observe
            val result = event.handleEvent()
            if (result == EVENT_SUCCESS)
                findNavController().popBackStack()
            else
                Toast.makeText(
                requireContext(),
                    result,
                    Toast.LENGTH_SHORT
                ).show()
        }

    }

    private fun checkAccountNumbersAndSetError() {

        with(viewBinding) {
            val accountNumber = senderAccountNumber.text.toString()
            val confirmationNumber = senderConfirmAccountNumber.text.toString()
            if (accountNumber != confirmationNumber) {
                senderConfirmAccountNumber.error =
                    "Account number and confirmation number does not match"
            } else {
                senderConfirmAccountNumber.error = null
            }
        }
    }

    private fun loadValues(sender: Sender) {

        with(viewBinding) {
            senderDisplayName.setText(sender.displayName)
            senderAccountName.setText(sender.name)
            senderAccountNumber.setText(sender.accountNumber)
            senderConfirmAccountNumber.setText(sender.accountNumber)
            senderAccountType.setText(sender.accountType)
            senderIfsc.setText(sender.ifsc)
            senderBank.setText(sender.bank)
            senderMobileNumber.setText(sender.mobileNumber)
            senderEmail.setText(sender.email)
        }
    }

    private fun validateAndSave() {
        if (!isAllFieldsValid) return

        val sender = Sender()
        with(viewBinding) {
            sender.name = senderAccountName.text.toString()
            sender.accountNumber = senderAccountNumber.text.toString()
            sender.accountType = senderAccountType.text.toString()
            sender.ifsc = senderIfsc.text.toString()
            sender.bank = senderBank.text.toString()
            sender.mobileNumber = senderMobileNumber.text.toString()
            sender.email = senderEmail.text.toString()
            sender.displayName = senderDisplayName.text.toString()
        }

        if (!isEditMode) { //add as new sender
            viewModel.addSender(sender)
        } else {
            sender.id = editingSenderId()
            viewModel.updateSender(sender)
        }

    }

    private val isAllFieldsValid: Boolean
        get() {
            var result = true
            if (TextUtils.isEmpty(viewBinding.senderAccountName.editableText)) {
                viewBinding.senderAccountName.error = "Enter a valid name"
                result = false
            }
            if (TextUtils.isEmpty(viewBinding.senderDisplayName.editableText)) {
                viewBinding.senderDisplayName.error = "Enter a valid Display name"
                result = false
            }
            val accountNumber = viewBinding.senderAccountNumber.editableText.toString()
            if (TextUtils.isEmpty(accountNumber)) {
                viewBinding.senderAccountNumber.error = "Enter valid account number"
                result = false
            }
            val confirmationAccountNumber =
                viewBinding.senderConfirmAccountNumber.editableText.toString()
            if (accountNumber != confirmationAccountNumber) {
                viewBinding.senderConfirmAccountNumber.error =
                    "Account number and confirmation number not matching"
                result = false
            }
            if (TextUtils.isEmpty(viewBinding.senderAccountType.editableText)) {
                viewBinding.senderAccountType.error = "Enter valid account type"
                result = false
            }
            if (TextUtils.isEmpty(viewBinding.senderIfsc.editableText)) {
                viewBinding.senderIfsc.error = "Enter valid IFS code"
                result = false
            }
            if (TextUtils.isEmpty(viewBinding.senderBank.editableText)) {
                viewBinding.senderBank.error = "Enter valid bank name"
                result = false
            }

            val mobile = viewBinding.senderMobileNumber.editableText
            if (mobile.isNotEmpty() && !isValidMobileNumber(mobile.toString())) {
                viewBinding.senderMobileNumber.error = "Enter valid mobile number"
                result = false
            }

            if (!TextUtils.isEmpty(viewBinding.senderEmail.editableText)) {
                if (!isValidEmail(viewBinding.senderEmail.text.toString())) {
                    viewBinding.senderEmail.error = "Enter a valid email"
                    return false
                }
            }
            return result
        }

    private fun isValidMobileNumber(mobileNumber: String): Boolean {
        return mobileNumber.isDigitsOnly() && mobileNumber.trim().length == 10
    }
    private fun isValidEmail(email: String): Boolean {
        val emailRegEx = "^[\\w-+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-zA-Z]{2,})$"
        val pattern = Pattern.compile(emailRegEx, Pattern.CASE_INSENSITIVE)
        val matcher: Matcher = pattern.matcher(email)
        return matcher.matches()
    }

    private fun editingSenderId(): Int = arguments?.getInt(KEY_SENDER_ID, -1) ?: -1


    private inner class CheckSameContentListener(private val editText: EditText) :
        OnFocusChangeListener {
        override fun onFocusChange(v: View, hasFocus: Boolean) {
            if (hasFocus || editText.hasFocus()) return
            checkAccountNumbersAndSetError()
        }
    }


}
