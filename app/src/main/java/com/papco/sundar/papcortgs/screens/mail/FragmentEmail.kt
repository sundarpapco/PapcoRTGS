package com.papco.sundar.papcortgs.screens.mail

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.database.transactionGroup.TransactionGroup
import com.papco.sundar.papcortgs.ui.screens.mail.MailScreen
import com.papco.sundar.papcortgs.ui.theme.RTGSTheme

class FragmentEmail : Fragment() {

    companion object{

        private const val KEY_GROUP_ID = "key:groupId"
        private const val KEY_GROUP_NAME = "key:groupName"
        private const val KEY_DEFAULT_SENDER_ID="key:defaultSenderId"

        fun getArgumentBundle(group:TransactionGroup):Bundle{

            return Bundle().also {
                it.putInt(KEY_GROUP_ID, group.id)
                it.putString(KEY_GROUP_NAME,group.name)
                it.putInt(KEY_DEFAULT_SENDER_ID,group.defaultSenderId)
            }
        }
    }

    private var googleSignInClient: GoogleSignInClient? = null
    private var requestPermissionLauncher = createRequestPermissionsLauncher()
    
    private val viewModel: FragmentEmailVM by lazy {
        ViewModelProvider(this)[FragmentEmailVM::class.java]
    }

    // region Override methods ---------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadEmailList(getGroup().id)
        googleSignInClient = createGoogleClient()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                RTGSTheme {
                    MailScreen(
                        screenState = viewModel.screenState,
                        onSendMails = { onFabPressed() },
                        onBackPressed = { findNavController().popBackStack() },
                        onSignOut = {signOut()}
                        )
                }
            }
        }
    }



    private fun onFabPressed() {

           /* if (!weHaveInternetConnection()) {
                Toast.makeText(
                    requireActivity(),
                    getString(R.string.error_no_internet_connection),
                    Toast.LENGTH_SHORT
                ).show()
                return
            }*/
            checkPermissionAndStartSending()
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

    private fun startEmailService() {
        MailWorker.startWith(
            requireContext(),
            getGroup().id
        )
    }

    private fun checkPermissionAndStartSending() {
        if (weHaveAccountsPermission()) {
            startEmailService()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.GET_ACCOUNTS)
        }
    }

    /*private fun weHaveInternetConnection(): Boolean {
        val connectionManager =
            requireActivity().applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities=connectionManager.getNetworkCapabilities(connectionManager.activeNetwork)

        return networkCapabilities?.let{
            when{
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)->true
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)->true
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)->true
                else->false
            }
        } ?: false

    }*/

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

    private fun signOut() {
        googleSignInClient!!.signOut().addOnCompleteListener {
            findNavController().popBackStack()
        }
    }

    private fun createRequestPermissionsLauncher(): ActivityResultLauncher<String> {

        return registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            result -> if (result) startEmailService()
        }
    }

    private fun weHaveAccountsPermission(): Boolean {
        return (ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.GET_ACCOUNTS
        ) == PackageManager.PERMISSION_GRANTED)
    }
}
