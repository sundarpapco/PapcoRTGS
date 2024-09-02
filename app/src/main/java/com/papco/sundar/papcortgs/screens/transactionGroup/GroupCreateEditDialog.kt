package com.papco.sundar.papcortgs.screens.transactionGroup

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.database.transactionGroup.TransactionGroup

class GroupCreateEditDialog : DialogFragment() {
    private var viewModel: GroupActivityVM? = null
    private var editingGroup: TransactionGroup? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[GroupActivityVM::class.java]
        editingGroup = viewModel!!.editingGroup
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return if (editingGroup == null) createNewAlertDialog() else createEditAlertDialog()
    }

    private fun createNewAlertDialog(): AlertDialog {
        val view = requireActivity().layoutInflater.inflate(R.layout.new_group, null)
        val editText = view.findViewById<EditText>(R.id.new_tag_editText)
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle("New XL file")
        builder.setView(view)
        builder.setPositiveButton("CREATE", null)
        builder.setNegativeButton("CANCEL", null)
        val dialog = builder.create()
        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                .setOnClickListener { validateAndCreateNewGroup(dialog, editText) }
        }
        return dialog
    }

    private fun createEditAlertDialog(): AlertDialog {
        val view = requireActivity().layoutInflater.inflate(R.layout.new_group, null)
        val editText = view.findViewById<EditText>(R.id.new_tag_editText)
        editText.setText(editingGroup!!.name)
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle("Edit XL filename")
        builder.setView(view)
        builder.setPositiveButton("SAVE", null)
        builder.setNegativeButton("CANCEL", null)
        builder.setNeutralButton("DELETE") { dialogInterface, i -> deleteGroup(editingGroup) }
        val dialog = builder.create()
        dialog.setOnShowListener {
            dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                .setOnClickListener { validateAndUpdateGroup(dialog, editText) }
        }
        return dialog
    }

    private fun validateAndUpdateGroup(dialog: AlertDialog, groupName: EditText) {
        val enteredTag = groupName.text.toString()
        if (enteredTag.trim { it <= ' ' } == "") {
            Toast.makeText(activity, "Please enter a XL file name", Toast.LENGTH_SHORT).show()
            return
        }
        dialog.dismiss()
        editingGroup!!.name = groupName.text.toString()
        viewModel!!.updateTransactionGroup(editingGroup)
    }

    private fun validateAndCreateNewGroup(dialog: AlertDialog, groupName: EditText) {
        val enteredTag = groupName.text.toString()
        if (enteredTag.trim { it <= ' ' } == "") {
            Toast.makeText(activity, "Please enter a XL file name", Toast.LENGTH_SHORT).show()
            return
        }
        dialog.dismiss()
        val newGroup = TransactionGroup()
        newGroup.name = groupName.text.toString()
        viewModel!!.addTransactionGroup(newGroup)
    }

    private fun deleteGroup(delGroup: TransactionGroup?) {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setMessage("Sure delete XL file " + delGroup!!.name + " ?")
        builder.setPositiveButton("DELETE") { dialogInterface, i ->
            viewModel!!.deleteTransactionGroup(
                delGroup
            )
        }
        builder.setNegativeButton("CANCEL", null)
        builder.create().show()
    }
}
