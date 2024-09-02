package com.papco.sundar.papcortgs.screens.mail

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.papco.sundar.papcortgs.R
import com.papco.sundar.papcortgs.common.DividerDecoration
import com.papco.sundar.papcortgs.database.pojo.CohesiveTransaction
import com.papco.sundar.papcortgs.database.transactionGroup.TransactionGroup
import com.papco.sundar.papcortgs.databinding.FragmentGmailBinding
import com.papco.sundar.papcortgs.extentions.enableBackArrow
import com.papco.sundar.papcortgs.extentions.registerBackArrowMenu
import com.papco.sundar.papcortgs.extentions.updateSubTitle
import com.papco.sundar.papcortgs.extentions.updateTitle

class FragmentEmail : Fragment() {

    companion object{

        const val TAG = "FragmentEmailTag"
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

    private var adapter: EmailAdapter? = null
    private var googleSignInClient: GoogleSignInClient? = null
    private var requestPermissionLauncher = createRequestPermissionsLauncher()

    private var _viewBinding:FragmentGmailBinding?=null
    private val viewBinding:FragmentGmailBinding
        get() = _viewBinding!!
    
    private val viewModel: FragmentEmailVM by lazy {
        ViewModelProvider(this)[FragmentEmailVM::class.java]
    }

    // region Override methods ---------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        googleSignInClient = createGoogleClient()
        viewModel.loadEmailList(getGroup().id)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding=FragmentGmailBinding.inflate(inflater,container,false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        observeViewModel()
        enableBackArrow()
        registerBackArrowMenu()
        updateTitle(getString(R.string.email_receivers))
        updateSubTitle("")
    }

    private fun initViews() {
        
        val currentLogin = GoogleSignIn.getLastSignedInAccount(requireActivity().applicationContext)
        if (currentLogin != null) 
            viewBinding.currentLoginEmail.text = currentLogin.email 
        else
            viewBinding.currentLoginEmail.text=""
        
        viewBinding.emailSignout.setOnClickListener{ 
            showSignOutDialog() 
        }
        
        viewBinding.emailFab.setOnClickListener {
            if (!weHaveInternetConnection()) {
                Toast.makeText(
                    requireActivity(),
                    getString(R.string.error_no_internet_connection),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            checkPermissionAndStartSending()
        }
        
        if (adapter == null) adapter = EmailAdapter(ArrayList())
        viewBinding.emailRecycler.setLayoutManager(LinearLayoutManager(requireActivity()))
        viewBinding.emailRecycler.addItemDecoration(
            DividerDecoration(
                requireActivity(),
                1,
                requireActivity().getColor(R.color.selectionGrey)
            )
        )
        viewBinding.emailRecycler.adapter=this.adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding.emailRecycler.adapter=null
        _viewBinding=null
    }

    private fun observeViewModel() {

        viewModel.isIntimationRunning.observe(viewLifecycleOwner){
            updateLayout(it)
        }
        
        viewModel.emailList.observe(viewLifecycleOwner){
            it?.let{
                adapter?.setData(it)
            }
        }
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

    // endregion override methods ---------------------------------------------
    private fun showSignOutDialog() {
        
        val builder = AlertDialog.Builder(requireActivity())
        val currentLogin = GoogleSignIn.getLastSignedInAccount(requireActivity().applicationContext)
        val currentEmail: String? = if (currentLogin != null) currentLogin.email else ""
        
        builder.setMessage("Sure want to sign out of $currentEmail?")
        builder.setNegativeButton("CANCEL", null)
        builder.setPositiveButton("SIGN OUT") { _, _ -> signOut() }
        builder.show()
    }

    private fun startEmailService() {
        MailWorker.startWith(
            requireContext(),
            getGroup().id
        )
    }

    private fun checkPermissionAndStartSending() {
        if (weHaveAccountsPermission()) {
            showSendConfirmDialog()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.GET_ACCOUNTS)
        }
    }

    private fun showSendConfirmDialog() {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle("SEND EMAILS?")
        builder.setMessage("Start sending mails to all beneficiaries? This operation cannot be cancelled in the middle")
        builder.setNegativeButton("CANCEL", null)
        builder.setPositiveButton("SEND") { _, _ -> startEmailService() }
        builder.create().show()
    }

    private fun updateLayout(isSending: Boolean) {
        if(isSending){
            viewBinding.emailFab.hide()
            viewBinding.emailSignout.visibility=View.GONE
        }else{
            viewBinding.emailFab.show()
            viewBinding.emailSignout.visibility=View.VISIBLE
        }
    }

    private fun weHaveInternetConnection(): Boolean {
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

    }

    private inner class EmailAdapter(private var data: List<CohesiveTransaction>) :
        RecyclerView.Adapter<EmailAdapter.EmailViewHolder>() {

        private val red = requireActivity().getColor(android.R.color.holo_red_dark)
        private val green =requireActivity().getColor(android.R.color.holo_green_dark)


        val asyncDiff=AsyncListDiffer(this,EmailListDiff())

        init {
            setHasStableIds(true)
        }

        override fun getItemId(position: Int): Long {
            return asyncDiff.currentList[position].transaction.id.toLong()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmailViewHolder {
            return EmailViewHolder(
                getLayoutInflater().inflate(
                    R.layout.sms_list_item,
                    parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: EmailViewHolder, position: Int) {

            val cohesiveTransaction = asyncDiff.currentList[position]
            holder.name.text = cohesiveTransaction.receiver.name
            if (cohesiveTransaction.receiver.email == null) cohesiveTransaction.receiver.email = ""
            if (cohesiveTransaction.receiver.email == "") {
                holder.emailAddress.visibility = View.INVISIBLE
                holder.icon.visibility = View.INVISIBLE
            } else {
                holder.emailAddress.visibility = View.VISIBLE
                holder.icon.visibility = View.VISIBLE
                holder.emailAddress.text = cohesiveTransaction.receiver.email
            }
            when (cohesiveTransaction.transaction.mailSent) {
                2 -> {
                    holder.status.text = getString(R.string.sending_failed)
                    holder.status.setTextColor(red)
                }

                1 -> {
                    holder.status.text = getString(R.string.mail_sent)
                    holder.status.setTextColor(green)
                }

                else -> holder.status.text = ""
            }
        }

        override fun getItemCount(): Int {
            return asyncDiff.currentList.size
        }

        fun setData(data: List<CohesiveTransaction>) {
            asyncDiff.submitList(data)
        }

        inner class EmailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var name: TextView
            var status: TextView
            var emailAddress: TextView
            var icon: ImageView

            init {
                name = itemView.findViewById(R.id.sms_name)
                status = itemView.findViewById(R.id.sms_status)
                emailAddress = itemView.findViewById(R.id.sms_mobilenumber)
                icon = itemView.findViewById(R.id.sms_icon)
                icon.setImageResource(R.drawable.gmail)
            }
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

    private fun signOut() {
        googleSignInClient!!.signOut().addOnCompleteListener {
            findNavController().popBackStack()
        }
    }

    private fun createRequestPermissionsLauncher(): ActivityResultLauncher<String> {

        return registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            result -> if (result) showSendConfirmDialog()
        }
    }

    private fun weHaveAccountsPermission(): Boolean {
        return (ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.GET_ACCOUNTS
        ) == PackageManager.PERMISSION_GRANTED)
    }
}
