package com.papco.sundar.papcortgs.screens.sms

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.common.SpacingDecoration
import com.papco.sundar.papcortgs.common.TextFunctions
import com.papco.sundar.papcortgs.databinding.ActivityComposeMessageBinding
import com.papco.sundar.papcortgs.extentions.enableBackArrow
import com.papco.sundar.papcortgs.extentions.getActionBar
import com.papco.sundar.papcortgs.extentions.updateTitle
import com.papco.sundar.papcortgs.settings.AppPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ComposeMessageFragment : Fragment() {

    private val adapter = TagAdapter()
    private val watcher: TextWatcher = MessageWatcher()

    private var _viewBinding: ActivityComposeMessageBinding? = null
    private val viewBinding: ActivityComposeMessageBinding
        get() = _viewBinding!!

    private val appPreferences: AppPreferences by lazy {
        AppPreferences(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        getActionBar()?.show()
        _viewBinding = ActivityComposeMessageBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableBackArrow()


        viewBinding.composeRecycler.setLayoutManager(
            LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
        )
        viewBinding.composeRecycler.addItemDecoration(
            SpacingDecoration(
                requireContext(), SpacingDecoration.HORIZONTAL, 16f, 8f, 16f
            )
        )
        viewBinding.composeRecycler.setAdapter(adapter)
        viewBinding.composeMessageBox.addTextChangedListener(watcher)
        registerOptionsMenu()
        updateTitle("Compose Message")
        loadDefaultMessageFormat()
        markBLUEinHeading()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding.composeRecycler.adapter = null
        _viewBinding = null
    }

    private fun registerOptionsMenu() {

        val menuProvider = object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_compose, menu)
            }

            override fun onMenuItemSelected(item: MenuItem): Boolean {

                when (item.itemId) {

                    android.R.id.home->{
                        findNavController().popBackStack()
                    }

                    R.id.action_compose_done -> {
                        saveMessageTemplateAndClose()
                        return true
                    }

                    R.id.action_compose_reset -> {
                        showResetMessageDialog()
                        return true
                    }
                }
                return false
            }
        }

        requireActivity().addMenuProvider(menuProvider, viewLifecycleOwner)
    }

    private fun loadDefaultMessageFormat() {

        lifecycleScope.launch {
            val format=appPreferences.getMessageTemplate().first()
            format?.let{
                viewBinding.composeMessageBox.setText(it)
            }
        }
    }

    private fun markBLUEinHeading() {
        val temp = viewBinding.composeInfo.text.toString()
        val result = SpannableString(temp)
        val startIndex = temp.indexOf("BLUE", 0)
        result.setSpan(ForegroundColorSpan(Color.BLUE), startIndex, startIndex + 4, 0)
        viewBinding.composeInfo.setText(result, TextView.BufferType.SPANNABLE)
    }

    private fun onRecyclerItemClicked(adapterPosition: Int) {
        var toInsert = "UNKNOWN"
        when (adapterPosition) {
            0 -> toInsert = TextFunctions.TAG_RECEIVER_ACC_NAME
            1 -> toInsert = TextFunctions.TAG_RECEIVER_ACC_NUMBER
            2 -> toInsert = TextFunctions.TAG_AMOUNT
            3 -> toInsert = TextFunctions.TAG_RECEIVER_BANK
            4 -> toInsert = TextFunctions.TAG_RECEIVER_IFSC
            5 -> toInsert = TextFunctions.TAG_SENDER_NAME
        }
        viewBinding.composeMessageBox.text.insert(
            viewBinding.composeMessageBox.selectionStart, "$toInsert "
        )
    }

    private fun updateSampleMessage(source: Editable) {
        var result = source.toString()
        result = result.replace(TextFunctions.TAG_RECEIVER_ACC_NAME.toRegex(), "ABC PVT LTD")
        result = result.replace(TextFunctions.TAG_RECEIVER_ACC_NUMBER.toRegex(), "123456789")
        result = result.replace(TextFunctions.TAG_AMOUNT.toRegex(), "Rs.12,345")
        result = result.replace(TextFunctions.TAG_RECEIVER_BANK.toRegex(), "ICICI, Svks")
        result = result.replace(TextFunctions.TAG_RECEIVER_IFSC.toRegex(), "ICIC0000012")
        result = result.replace(TextFunctions.TAG_SENDER_NAME.toRegex(), "PAPCO PRIVATE LIMITED")
        viewBinding.composeSampleBox.text=result
    }

    private fun showResetMessageDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Sure want to reset the text to default format?")
        builder.setNegativeButton("CANCEL", null)
        builder.setPositiveButton("RESET") { _, _ ->
            viewBinding.composeMessageBox.setText(
                TextFunctions.getDefaultMessageFormat()
            )
        }
        builder.create().show()
    }

    private fun saveMessageTemplateAndClose() {

        lifecycleScope.launch {
            val format = viewBinding.composeMessageBox.text.toString()
            appPreferences.saveMessageTemplate(format)
            findNavController().popBackStack()
        }
    }

    private inner class TagAdapter : RecyclerView.Adapter<TagAdapter.TagVH>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagVH {
            return TagVH(layoutInflater.inflate(R.layout.compose_tag_item, parent, false))
        }

        override fun onBindViewHolder(holder: TagVH, position: Int) {
            holder.bind()
        }

        override fun getItemCount(): Int {
            return 6
        }

        internal inner class TagVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var txtName: TextView

            init {
                txtName = itemView.findViewById(R.id.tag_item_name)
                itemView.setOnClickListener { onRecyclerItemClicked(adapterPosition) }
            }

            fun bind() {
                var name = "UNKNOWN TAG"
                when (bindingAdapterPosition) {
                    0 -> name = "RECEIVER\nACCOUNT\nNAME"
                    1 -> name = "RECEIVER\nACCOUNT\nNUMBER"
                    2 -> name = "TRANSACTION\nAMOUNT"
                    3 -> name = "RECEIVER\nBANK"
                    4 -> name = "RECEIVER\nIFSC"
                    5 -> name = "SENDER\nACCOUNT\nNAME"
                }
                txtName.text = name
            }
        }
    }

    internal inner class MessageWatcher : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {
            TextFunctions.removeAllForegroundSpan(editable)
            TextFunctions.markAllTagsWithSpan(editable)
            updateSampleMessage(editable)
        }
    }
}
