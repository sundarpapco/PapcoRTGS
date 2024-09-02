package com.papco.sundar.papcortgs.screens.mail

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.database.transactionGroup.TransactionGroup
import com.papco.sundar.papcortgs.databinding.FragmentGmailBinding
import com.papco.sundar.papcortgs.databinding.FragmentGmailSigninBinding

class FragmentGoogleSignIn : Fragment() {

    companion object{

        const val TAG = "FragmentEmailTag"
        private const val KEY_GROUP_ID = "key:groupId"
        private const val KEY_GROUP_NAME = "key:groupName"
        private const val KEY_DEFAULT_SENDER_ID="key:defaultSenderId"

        fun getArgumentBundle(group: TransactionGroup):Bundle{

            return Bundle().also {
                it.putInt(KEY_GROUP_ID, group.id)
                it.putString(KEY_GROUP_NAME,group.name)
                it.putInt(KEY_DEFAULT_SENDER_ID,group.defaultSenderId)
            }
        }
    }

    private val googleSignInClient: GoogleSignInClient by lazy{createGoogleClient()}
    private val signInLauncher = createSignInLauncher()

    private var _viewBinding:FragmentGmailSigninBinding?=null
    private val viewBinding:FragmentGmailSigninBinding
        get() = _viewBinding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (GoogleSignIn.getLastSignedInAccount(requireActivity().applicationContext) != null) navigateToEmailFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _viewBinding= FragmentGmailSigninBinding.inflate(inflater,container,false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding=null
    }

    private fun initViews(){
        viewBinding.gmailSigninButton.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        signInLauncher.launch(googleSignInClient.signInIntent)
    }

    private fun navigateToEmailFragment() {
        val args=FragmentEmail.getArgumentBundle(getGroup())
        findNavController().navigate(
            R.id.action_fragmentGoogleSignIn_to_fragmentEmail,
            args
        )
    }

    private fun createSignInLauncher(): ActivityResultLauncher<Intent> {
        return registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            handleSignInResult(GoogleSignIn.getSignedInAccountFromIntent(it.data))
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            completedTask.getResult(ApiException::class.java)
            // Signed in successfully, show authenticated UI.
            navigateToEmailFragment()
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
        }
    }

    private fun createGoogleClient(): GoogleSignInClient {
        val scopeSendMail = "https://www.googleapis.com/auth/gmail.send"
        val builder = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        builder.requestScopes(Scope(scopeSendMail))
        builder.requestIdToken(getString(R.string.client_id))
        builder.requestEmail()
        builder.requestProfile()
        val gso = builder.build()
        return GoogleSignIn.getClient(requireActivity().applicationContext, gso)
    }

    private fun getGroup():TransactionGroup{

        return arguments?.let{
            val group=TransactionGroup()
            group.id=it.getInt(KEY_GROUP_ID)
            group.name=it.getString(KEY_GROUP_NAME)
            group.defaultSenderId=it.getInt(KEY_DEFAULT_SENDER_ID)
            group

        } ?: throw Exception("Arguments not set for Fragment Email")
    }
}
