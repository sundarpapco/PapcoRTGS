package com.papco.sundar.papcortgs.screens.mail;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.papco.sundar.papcortgs.R;
import com.papco.sundar.papcortgs.common.DividerDecoration;
import com.papco.sundar.papcortgs.database.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;

public class FragmentEmail extends Fragment implements EmailCallBack {


    public static final String TAG = "FragmentEmailTag";

    TextView currentLoginName, signOutView;
    ActivityEmailVM viewModel;
    EmailAdapter adapter;
    RecyclerView recycler;
    EmailService emailService;
    EmailServiceConnection connection;
    boolean mBound = false;
    FloatingActionButton fab;
    GoogleSignInClient googleSignInClient;

    ActivityResultLauncher<String> requestPermissionLauncher = createRequestPermissionsLauncher();


    // region Override methods ---------------------------------------------

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ActivityEmailVM.class);
        googleSignInClient = createGoogleClient();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (EmailService.isRunning() && !mBound) {
            bindToEmailService();
        }

        if (EmailService.isRunning()) {
            fab.hide();
            signOutView.setVisibility(View.GONE);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if (EmailService.isRunning() && mBound) {
            unBindFromEmailService();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (container != null)
            container.removeAllViews();
        return inflater.inflate(R.layout.fragment_gmail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        observeViewModel();
    }

    private void initViews(View ui) {

        currentLoginName = ui.findViewById(R.id.current_login_email);
        signOutView = ui.findViewById(R.id.email_signout);
        recycler = ui.findViewById(R.id.email_recycler);
        fab = ui.findViewById(R.id.email_fab);

        GoogleSignInAccount currentLogin = GoogleSignIn.getLastSignedInAccount(requireActivity().getApplicationContext());
        if (currentLogin != null)
            currentLoginName.setText(currentLogin.getEmail());
        else
            currentLoginName.setText("");

        signOutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSignOutDialog();
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!weHaveInternetConnection()) {
                    Toast.makeText(requireActivity(), getString(R.string.error_no_internet_connection), Toast.LENGTH_SHORT).show();
                    return;
                }

                checkPermissionAndStartSending();
            }
        });

        if (adapter == null)
            adapter = new EmailAdapter(new ArrayList<Transaction>());

        recycler.setLayoutManager(new LinearLayoutManager(requireActivity()));
        recycler.addItemDecoration(new DividerDecoration(requireActivity(), 1, requireActivity().getResources().getColor(R.color.selectionGrey)));
        recycler.setAdapter(adapter);

    }

    private void observeViewModel() {

        viewModel.getEmailList().observe(getViewLifecycleOwner(), new Observer<List<Transaction>>() {
            @Override
            public void onChanged(@Nullable List<Transaction> transactions) {
                if (transactions == null)
                    return;

                adapter.setData(transactions);
            }
        });

    }

    // endregion override methods ---------------------------------------------

    private void showSignOutDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        GoogleSignInAccount currentLogin = GoogleSignIn.getLastSignedInAccount(requireActivity().getApplicationContext());
        String currentEmail;
        if (currentLogin != null)
            currentEmail = currentLogin.getEmail();
        else
            currentEmail = "";

        builder.setMessage("Sure want to sign out of " + currentEmail + "?");
        builder.setNegativeButton("CANCEL", null);
        builder.setPositiveButton("SIGN OUT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                signOut();
            }
        });
        builder.show();
    }


    public void startEmailService() {

        Intent intent = EmailService.getStartingIntent(
                requireContext(),
                viewModel.currentGroupId,
                viewModel.currentGroupName,
                viewModel.currentDefaultSenderId);

        requireActivity().startService(intent);
        bindToEmailService();
        updateLayout(true);

    }

    private void stopEmailService() {

        unBindFromEmailService();
        requireActivity().startService(EmailService.getStoppingIntent(requireActivity()));
        updateLayout(false);

    }

    private void bindToEmailService() {

        mBound = true;
        Intent bindIntent = new Intent(requireActivity(), EmailService.class);
        if (connection == null)
            connection = new EmailServiceConnection();
        requireActivity().bindService(bindIntent, connection, Context.BIND_AUTO_CREATE);


    }

    private void unBindFromEmailService() {

        mBound = false;
        //updateLayout(false);
        emailService.removeCallBack();
        requireActivity().unbindService(connection);


    }


    private void checkPermissionAndStartSending() {

        if (weHaveAccountsPermission()) {
            showSendConfirmDialog();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.GET_ACCOUNTS);
        }

    }

    public void showSendConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle("SEND EMAILS?");
        builder.setMessage("Start sending mails to all beneficiaries? This operation cannot be cancelled in the middle");
        builder.setNegativeButton("CANCEL", null);
        builder.setPositiveButton("SEND", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startEmailService();
            }
        });
        builder.create().show();
    }

    private void updateLayout(boolean isSending) {

        Animation fabAnim, signoutAnim;

        if (isSending) {

            if (fab.getVisibility() == View.GONE)
                return;

            fabAnim = AnimationUtils.loadAnimation(requireActivity(), R.anim.hide_view_shrink);
            fabAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    fab.hide();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            signoutAnim = AnimationUtils.loadAnimation(requireActivity(), R.anim.hide_view_shrink);
            signoutAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    signOutView.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            fab.startAnimation(fabAnim);
            signOutView.startAnimation(signoutAnim);
        } else {

            if (fab.getVisibility() == View.VISIBLE)
                return;

            fabAnim = AnimationUtils.loadAnimation(requireActivity(), R.anim.show_view_grow);
            fab.show();
            signOutView.setVisibility(View.VISIBLE);
            fab.startAnimation(fabAnim);
            signOutView.startAnimation(fabAnim);

        }

    }


    private boolean weHaveInternetConnection() {

        ConnectivityManager cmanager = (ConnectivityManager) requireActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cmanager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }


    // region Observers for service ----------------------------------------------------

    @Override
    public void onObserverAttached(List<Transaction> currentList) {

        if (viewModel.getEmailList().getValue() == null)
            viewModel.getEmailList().setValue(currentList);
        else
            adapter.updateEmailStatus();
    }

    @Override
    public void onStartSending() {
        adapter.updateEmailStatus();
    }

    @Override
    public void onUpdate(int changedIndex) {

        adapter.updateStatus(changedIndex);
    }

    @Override
    public void onComplete(List<Transaction> list) {
        if (viewModel.getEmailList().getValue() == null)
            viewModel.getEmailList().setValue(list);

        stopEmailService();
    }

    //endregion Observers for service ----------------------------------------------------

    class EmailAdapter extends RecyclerView.Adapter<EmailAdapter.EmailViewHolder> {


        private final int COLOR_RED = getResources().getColor(android.R.color.holo_red_dark);
        private final int COLOR_GREEN = getResources().getColor(android.R.color.holo_green_dark);
        private final int COLOR_BLACK = getResources().getColor(android.R.color.black);
        private final int COLOR_GREY = getResources().getColor(android.R.color.darker_gray);

        private List<Transaction> data;


        public EmailAdapter(List<Transaction> data) {
            this.data = data;
            setHasStableIds(true);
        }

        @Override
        public long getItemId(int position) {
            return data.get(position).id;
        }

        @NonNull
        @Override
        public EmailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            return new EmailViewHolder(getLayoutInflater().inflate(R.layout.sms_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull EmailViewHolder holder, int position) {

            Transaction transaction = data.get(holder.getAdapterPosition());

            holder.name.setText(transaction.receiver.name);

            if (transaction.receiver.email == null)
                transaction.receiver.email = "";

            if (transaction.receiver.email.equals("")) {
                holder.emailAddress.setVisibility(View.INVISIBLE);
                holder.icon.setVisibility(View.INVISIBLE);
            } else {
                holder.emailAddress.setVisibility(View.VISIBLE);
                holder.icon.setVisibility(View.VISIBLE);
                holder.emailAddress.setText(transaction.receiver.email);
            }

            switch (transaction.emailStatus) {
                case EmailService.STATUS_FAILED:
                    holder.status.setText(getString(R.string.sending_failed));
                    holder.status.setTextColor(COLOR_RED);
                    break;

                case EmailService.STATUS_SENT:
                    holder.status.setText(getString(R.string.mail_sent));
                    holder.status.setTextColor(COLOR_GREEN);
                    break;

                case EmailService.STATUS_QUEUED:
                    holder.status.setText(getString(R.string.queued));
                    holder.status.setTextColor(COLOR_GREY);
                    break;

                case EmailService.STATUS_SENDING:
                    holder.status.setText(getString(R.string.sending));
                    holder.status.setTextColor(COLOR_BLACK);
                    break;

                default:
                    holder.status.setText("");
            }

        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        public void setData(List<Transaction> data) {
            this.data = data;
            notifyDataSetChanged();

        }

        public void updateStatus(int index) {
            notifyItemChanged(index);
        }

        public void updateEmailStatus() {

            notifyItemRangeChanged(0, data.size());

        }


        class EmailViewHolder extends RecyclerView.ViewHolder {

            TextView name, status, emailAddress;
            ImageView icon;

            public EmailViewHolder(View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.sms_name);
                status = itemView.findViewById(R.id.sms_status);
                emailAddress = itemView.findViewById(R.id.sms_mobilenumber);
                icon = itemView.findViewById(R.id.sms_icon);
                icon.setImageResource(R.drawable.gmail);
            }
        }
    }

    class EmailServiceConnection implements ServiceConnection {


        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            emailService = ((EmailService.EmailBinder) iBinder).getService();
            emailService.setCallBack(FragmentEmail.this);
            emailService.setListAndStartSending(viewModel.getEmailList().getValue());

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            emailService = null;

        }
    }

    private GoogleSignInClient createGoogleClient() {

        String scopeSendMail = "https://www.googleapis.com/auth/gmail.send";
        GoogleSignInOptions.Builder builder = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN);
        builder.requestScopes(new Scope(scopeSendMail));
        builder.requestIdToken(getString(R.string.client_id));
        builder.requestEmail();
        builder.requestProfile();
        GoogleSignInOptions gso = builder.build();
        return GoogleSignIn.getClient(requireActivity().getApplicationContext(), gso);

    }

    public void signOut() {

        viewModel.clearEmailList();
        googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                navigateToSignInFragment();
            }
        });

    }

    private void navigateToSignInFragment() {

        int containerId = R.id.container;
        if (getView() != null)
            containerId = ((ViewGroup) getView().getParent()).getId();

        FragmentManager manager = getParentFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(containerId, new FragmentGoogleSignIn());
        transaction.commit();

    }

    private ActivityResultLauncher<String> createRequestPermissionsLauncher() {

        return registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result)
                    showSendConfirmDialog();
            }
        });
    }

    public boolean weHaveAccountsPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.GET_ACCOUNTS)
                == PackageManager.PERMISSION_GRANTED;
    }


}
