package com.papco.sundar.papcortgs.screens.backup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.papco.sundar.papcortgs.ui.backup.BackupScreen
import com.papco.sundar.papcortgs.ui.theme.RTGSTheme

class DropBoxFragment : Fragment() {


    private val viewModel: DropBoxFragmentVM by lazy {
        ViewModelProvider(this)[DropBoxFragmentVM::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                RTGSTheme {
                    BackupScreen(
                        screenState = viewModel.screenState,
                        onLink = { viewModel.linkToDropBox() },
                        onUnlink = { viewModel.unlinkFromDropBox() },
                        onBackup = { viewModel.backupFile() },
                        onRestore = { viewModel.restoreBackup() },
                        onBackPressed = {findNavController().popBackStack()}
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshDropBoxConnection()
    }
}