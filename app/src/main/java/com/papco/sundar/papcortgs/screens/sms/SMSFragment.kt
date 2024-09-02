package com.papco.sundar.papcortgs.screens.sms

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.common.DividerDecoration
import com.papco.sundar.papcortgs.database.pojo.CohesiveTransaction
import com.papco.sundar.papcortgs.databinding.ActivitySmsBinding
import com.papco.sundar.papcortgs.extentions.updateTitle

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

    private val adapter: SMSAdapter by lazy {
        SMSAdapter(requireContext(), ArrayList())
    }

    private val currentGroupId: Int
        get() = arguments?.getInt(KEY_GROUP_ID, -1) ?: error("Missing Group Id Argument")

    private val currentGroupName: String
        get() = arguments?.getString(KEY_GROUP_NAME, "Unspecified Group")
            ?: error("Missing Group Name argument")


    private var _viewBinding: ActivitySmsBinding? = null
    private val viewBinding: ActivitySmsBinding
        get() = _viewBinding!!

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
        _viewBinding = ActivitySmsBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding.smsRecycler.adapter = null
        _viewBinding = null
    }

    private fun initViews() {

        with(viewBinding) {
            smsButtonSendAll.setOnClickListener {
                showSendConfirmDialog()
            }

            smsRecycler.setLayoutManager(LinearLayoutManager(requireContext()))
            smsRecycler.addItemDecoration(DividerDecoration(requireContext()))
            smsRecycler.adapter = adapter
            updateTitle("Send SMS to Receivers")
        }
    }

    private fun observeViewModel() {

        viewModel.messagingList.observe(viewLifecycleOwner) { list ->
            list?.let {
                adapter.setData(it)
            }
        }

        viewModel.isIntimationRunning.observe(viewLifecycleOwner){intimationRunning->
            if(intimationRunning)
                viewBinding.smsButtonSendAll.visibility=View.INVISIBLE
            else
                viewBinding.smsButtonSendAll.visibility=View.VISIBLE
        }
    }

    private fun showSendConfirmDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("SEND MESSAGES")
        builder.setMessage("Start sending messages to all beneficiaries? This operation cannot be cancelled in the middle")
        builder.setNegativeButton("CANCEL", null)
        builder.setPositiveButton("SEND") { _, _ -> checkPermissionAndStartService() }
        builder.create().show()
    }

    private fun checkPermissionAndStartService() {
        if (weHaveSMSPermission()) startSmsService() else requestSMSPermission()
    }

    private fun startSmsService() {
        Log.d("SAATVIK","Starting the Messaging work")
        MessageWorker.startWith(
            requireContext(), currentGroupId
        )
    }

    private fun weHaveSMSPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) return false
        return if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) false else true
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

    private inner class SMSAdapter(
        private val context: Context, private var data: List<CohesiveTransaction>
    ) : RecyclerView.Adapter<SMSAdapter.SMSViewHolder>() {
        init {
            setHasStableIds(true)
        }

        override fun getItemId(position: Int): Long {
            return data[position].transaction.id.toLong()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SMSViewHolder {
            return SMSViewHolder(layoutInflater.inflate(R.layout.sms_list_item, parent, false))
        }

        override fun onBindViewHolder(holder: SMSViewHolder, position: Int) {
            val item = data[holder.bindingAdapterPosition]
            holder.name.text = item.receiver.name
            holder.status.text =
                MessageSentStatus.descriptionFromCode(context, item.transaction.messageSent)
            if (item.receiver.mobileNumber == "") {
                holder.mobilenumer.visibility = View.INVISIBLE
                holder.icon.visibility = View.INVISIBLE
            } else {
                holder.mobilenumer.visibility = View.VISIBLE
                holder.icon.visibility = View.VISIBLE
                holder.mobilenumer.text = data[holder.bindingAdapterPosition].receiver.mobileNumber
            }
            if (item.transaction.messageSent != MessageSentStatus.SENT.code) holder.status.setTextColor(
                requireContext().getColor(android.R.color.holo_red_dark)
            ) else holder.status.setTextColor(requireContext().getColor(android.R.color.holo_green_dark))
        }

        override fun getItemCount(): Int {
            return data.size
        }

        fun setData(data: List<CohesiveTransaction>) {
            this.data = data
            notifyDataSetChanged()
        }

        inner class SMSViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var name: TextView
            var status: TextView
            var mobilenumer: TextView
            var icon: ImageView

            init {
                name = itemView.findViewById(R.id.sms_name)
                status = itemView.findViewById(R.id.sms_status)
                mobilenumer = itemView.findViewById(R.id.sms_mobilenumber)
                icon = itemView.findViewById(R.id.sms_icon)
            }
        }
    }

}
