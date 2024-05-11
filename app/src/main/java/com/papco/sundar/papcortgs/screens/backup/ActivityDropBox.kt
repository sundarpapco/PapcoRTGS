package com.papco.sundar.papcortgs.screens.backup

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.papco.sundar.papcortgs.R
import kotlinx.coroutines.runBlocking

class ActivityDropBox : AppCompatActivity() {

    private var link: Button? = null
    private var backup: Button? = null
    private var restore: Button? = null
    private var progress: TextView? = null
    private val viewModel by lazy {
        ViewModelProvider(this).get(ActivityDropBoxVM::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dropbox)

        link = findViewById(R.id.btn_link_dropbox)
        backup = findViewById(R.id.dropbox_btn_backup)
        restore = findViewById(R.id.dropbox_btn_restore)
        progress = findViewById(R.id.dropbox_progess)

        link!!.setOnClickListener {
            progress!!.text = ""
            runBlocking {
                if (viewModel.isDropBoxConnected()) {
                    viewModel.unlinkFromDropBox()
                    renderDisconnectedState()
                } else {
                    viewModel.linkToDropBox()
                }
            }
        }

        backup!!.setOnClickListener { //if already backup in progress, do nothing
            viewModel.backupFile()
        }

        restore!!.setOnClickListener { //if already restoring in progress, do nothing
            showAlertDialogForRestore()
        }

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

    private fun observeViewModel() {
        viewModel.backupOperationStatus.observe(this) {
            progress!!.text = it
        }
    }


    private fun showAlertDialogForRestore() {
        val builder = AlertDialog.Builder(this)
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

    private fun renderDisconnectedState() {
        link!!.text = getString(R.string.link_to_dropbox)
        backup!!.visibility = View.GONE
        restore!!.visibility = View.GONE
    }

    private fun renderConnectedState() {
        link!!.text = getString(R.string.unlink_from_dropbox)
        backup!!.visibility = View.VISIBLE
        restore!!.visibility = View.VISIBLE
    }

}
