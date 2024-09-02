package com.papco.sundar.papcortgs.screens.backup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.databinding.ActivityDropboxBinding
import com.papco.sundar.papcortgs.extentions.enableBackArrow
import com.papco.sundar.papcortgs.extentions.registerBackArrowMenu
import com.papco.sundar.papcortgs.extentions.updateTitle
import kotlinx.coroutines.runBlocking

class DropBoxFragment : Fragment() {


    private val viewModel: DropBoxFragmentVM by lazy {
        ViewModelProvider(this)[DropBoxFragmentVM::class.java]
    }

    private var _viewBinding: ActivityDropboxBinding? = null
    private val viewBinding: ActivityDropboxBinding
        get() = _viewBinding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _viewBinding = ActivityDropboxBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        enableBackArrow()
        registerBackArrowMenu()
        updateTitle("Backup to DropBox")
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        runBlocking {
            if (viewModel.isDropBoxConnected())
                renderConnectedState()
            else
                renderDisconnectedState()
        }
    }


    private fun initViews() {

        with(viewBinding) {

            btnLinkDropbox.setOnClickListener {
                dropboxProgess.text = ""
                runBlocking {
                    if (viewModel.isDropBoxConnected()) {
                        viewModel.unlinkFromDropBox()
                        renderDisconnectedState()
                    } else {
                        viewModel.linkToDropBox()
                    }
                }
            }

            dropboxBtnBackup.setOnClickListener { //if already backup in progress, do nothing
                viewModel.backupFile()
            }

            dropboxBtnRestore.setOnClickListener { //if already restoring in progress, do nothing
                showAlertDialogForRestore()
            }
        }

    }

    private fun observeViewModel() {
        viewModel.backupOperationStatus.observe(viewLifecycleOwner) {
            viewBinding.dropboxProgess.text = it
        }
    }

    private fun renderDisconnectedState() {
        with(viewBinding) {
            btnLinkDropbox.text = getString(R.string.link_to_dropbox)
            dropboxBtnBackup.visibility = View.GONE
            dropboxBtnRestore.visibility = View.GONE
        }


    }

    private fun renderConnectedState() {
        with(viewBinding) {
            btnLinkDropbox.text = getString(R.string.unlink_from_dropbox)
            dropboxBtnBackup.visibility = View.VISIBLE
            dropboxBtnRestore.visibility = View.VISIBLE
        }
    }

    private fun showAlertDialogForRestore() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Caution!")
        builder.setMessage(
            """
    Restoring from backup will erase and overwrite all your current data with the dropbox data.
    Sure want to restore and overwrite?
    """.trimIndent()
        )
        builder.setPositiveButton("RESTORE") { dialogInterface, i ->
            viewModel.restoreBackup()
        }
        builder.setNegativeButton("CANCEL", null)
        builder.create().show()
    }
}