package com.papco.sundar.papcortgs.ui.screens.party

import android.content.Context
import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.text.isDigitsOnly
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.database.receiver.Receiver
import com.papco.sundar.papcortgs.database.sender.Sender

class AddEditPartyState(private val context:Context){

    var isWaiting:Boolean by mutableStateOf(false)

    var displayName:String by mutableStateOf("")
        private set

    var displayNameError:String? by mutableStateOf(null)
        private set

    var accountName:String by mutableStateOf("")
        private set

    var accountNameError:String? by mutableStateOf(null)
        private set

    var accountNumber:String by mutableStateOf("")
        private set

    var accountNumberError:String? by mutableStateOf(null)
        private set

    var confirmAccountNumber:String by mutableStateOf("")
        private set

    var confirmAccountNumberError:String? by mutableStateOf(null)
        private set

    var accountType:String by mutableStateOf("")
        private set

    var accountTypeError:String? by mutableStateOf(null)
        private set

    var ifsCode:String by mutableStateOf("")
        private set

    var ifsCodeError:String? by mutableStateOf(null)
        private set

    var bankAndBranch:String by mutableStateOf("")
        private set

    var bankAndBranchError:String? by mutableStateOf(null)
        private set

    var mobileNumber:String by mutableStateOf("")
        private set

    var mobileNumberError:String? by mutableStateOf(null)
        private set

    var email:String by mutableStateOf("")
        private set

    var emailError:String? by mutableStateOf(null)
        private set

    fun loadDisplayName(displayName:String) {
        this.displayName = displayName
        displayNameError=null
    }

    fun loadAccountName(accountName:String){
        this.accountName=accountName
        accountNameError=null
    }

    fun loadAccountNumber(accountNumber:String){
        this.accountNumber=accountNumber
        accountNumberError=null
    }

    fun loadConfirmAccountNumber(confirmAccountNumber:String){
        this.confirmAccountNumber=confirmAccountNumber
        confirmAccountNumberError=null
    }

    fun loadAccountType(accountType:String){
        this.accountType=accountType
        accountTypeError=null
    }

    fun loadIfsCode(code:String){
        this.ifsCode=code
        ifsCodeError=null
    }

    fun loadBankAndBranch(bankAndBranch:String){
        this.bankAndBranch=bankAndBranch
        bankAndBranchError=null
    }

    fun loadMobileNumber(mobileNumber:String){
        this.mobileNumber=mobileNumber
        mobileNumberError=null
    }

    fun loadEmail(email:String){
        this.email=email
        emailError=null
    }

    fun loadSender(sender: Sender){
        displayName=sender.displayName
        accountName=sender.name
        accountNumber=sender.accountNumber
        confirmAccountNumber=accountNumber
        accountType=sender.accountType
        ifsCode=sender.ifsc
        bankAndBranch=sender.bank
        mobileNumber=sender.mobileNumber
        email=sender.email
    }

    fun loadReceiver(receiver: Receiver){
        displayName=receiver.displayName
        accountName=receiver.name
        accountNumber=receiver.accountNumber
        confirmAccountNumber=accountNumber
        accountType=receiver.accountType
        ifsCode=receiver.ifsc
        bankAndBranch=receiver.bank
        mobileNumber=receiver.mobileNumber
        email=receiver.email
    }

    fun asSender(id:Int=0):Sender{
        val sender = Sender()
        sender.id=id
        sender.displayName = displayName
        sender.name = accountName
        sender.accountNumber = accountNumber
        sender.accountType = accountType
        sender.ifsc = ifsCode
        sender.bank = bankAndBranch
        sender.mobileNumber = mobileNumber
        sender.email = email

        return sender
    }

    fun asReceiver(id:Int=0):Receiver{
        val receiver = Receiver()
        receiver.id=id
        receiver.displayName = displayName
        receiver.name = accountName
        receiver.accountNumber = accountNumber
        receiver.accountType = accountType
        receiver.ifsc = ifsCode
        receiver.bank = bankAndBranch
        receiver.mobileNumber = mobileNumber
        receiver.email = email

        return receiver
    }


   fun validateState():Boolean{

       var result=true

       if(!isDisplayNameValid()){
           displayNameError=context.getString(R.string.invalid_display_name)
           result=false
       }

       if(!isAccountNameValid()){
           accountNameError=context.getString(R.string.invalid_account_name)
           result=false
       }

       if(!isAccountNumberValid()){
           accountNumberError=context.getString(R.string.invalid_account_number)
           result=false
       }

       if(!isConfirmAccountNumberValid()){
           confirmAccountNumberError=context.getString(R.string.invalid_confirmation_account_number)
           result=false
       }

       if(!isAccountTypeValid()){
           accountTypeError=context.getString(R.string.invalid_account_type)
           result=false
       }

       if(!isIfsCodeValid()){
           ifsCodeError=context.getString(R.string.invalid_ifs_code)
           result=false
       }

       if(!isBankAndBranchValid()){
           bankAndBranchError=context.getString(R.string.invalid_bank_branch)
           result=false
       }

       if(!isMobileNumberValid()){
           mobileNumberError=context.getString(R.string.invalid_mobile_number)
           result=false
       }

       if(!isEmailValid()){
           emailError=context.getString(R.string.invalid_email)
           result=false
       }

       return result

   }

    private fun isDisplayNameValid():Boolean =
        displayName.isNotBlank()

    private fun isAccountNameValid():Boolean=
        accountName.isNotBlank()

    private fun isAccountNumberValid():Boolean=
        accountNumber.isNotBlank()

    private fun isConfirmAccountNumberValid():Boolean=
        accountNumber.isBlank() || accountNumber==confirmAccountNumber

    private fun isAccountTypeValid():Boolean=
        accountType.isNotBlank()

    private fun isIfsCodeValid():Boolean=
        ifsCode.isNotBlank()

    private fun isBankAndBranchValid():Boolean=
        bankAndBranch.isNotBlank()

    private fun isMobileNumberValid():Boolean=
        mobileNumber.isBlank() || (mobileNumber.isDigitsOnly() && mobileNumber.length==10)

    private fun isEmailValid():Boolean{
        if (email.isBlank())
            return true

        val matcher=Patterns.EMAIL_ADDRESS.matcher(email)
        return matcher.matches()
    }

}