package com.papco.sundar.papcortgs.screens.sms

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.papco.sundar.papcortgs.ui.screens.message.MessageScreen
import com.papco.sundar.papcortgs.ui.theme.RTGSTheme

class SMSFragment : Fragment() {

    companion object {
        private const val KEY_GROUP_ID = "key:group:id"
        private const val KEY_GROUP_NAME = "key:group:name"

        fun getArgumentBundle(groupId: Int, groupName: String): Bundle {
            return Bundle().also {
                it.putInt(KEY_GROUP_ID, groupId)
                it.putString(KEY_GROUP_NAME, groupName)
            }
        }
    }

    private val viewModel: FragmentSMSVM by lazy {
        ViewModelProvider(this)[FragmentSMSVM::class.java]
    }

    private val currentGroupId: Int
        get() = arguments?.getInt(KEY_GROUP_ID, -1) ?: error("Missing Group Id Argument")

    private val currentGroupName: String
        get() = arguments?.getString(KEY_GROUP_NAME, "Unspecified Group")
            ?: error("Missing Group Name argument")


    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

            var granted = true
            for (permission in permissions) {
                granted = permission.value
            }
            if(granted)
                onPermissionGranted()
        }


    override fun onCreate(savedInstanceSt3ate: Bundle?) {
        super.onCreate(savedInstanceSt3ate)
        viewModel.loadMessagingList(currentGroupId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
       return ComposeView(requireContext()).apply {
           setContent {
               RTGSTheme {
                   MessageScreen(
                       screenState = viewModel.screenState,
                       onSendMessages = { checkPermissionAndStartService() },
                       onBackPressed = {findNavController().popBackStack()}
                   )
               }
           }
       }
    }

    private fun checkPermissionAndStartService() {
        if (weHaveSMSPermission()) startSmsService() else requestSMSPermission()
    }

    private fun startSmsService() {
        MessageWorker.startWith(
            requireContext(), currentGroupId
        )
    }

    private fun weHaveSMSPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) return false

        return ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestSMSPermission() {

        val requiredPermissions= mutableListOf(Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS)

        if(Build.VERSION.SDK_INT > 32)
            requiredPermissions.add(Manifest.permission.POST_NOTIFICATIONS)

        requestPermission.launch(
           requiredPermissions.toTypedArray()
        )
    }

    private fun onPermissionGranted(){
        startSmsService()
    }
}
