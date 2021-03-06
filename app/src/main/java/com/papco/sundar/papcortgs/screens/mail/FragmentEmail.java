package com.papco.sundar.papcortgs.screens.mail;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.papco.sundar.papcortgs.common.DividerDecoration;
import com.papco.sundar.papcortgs.R;
import com.papco.sundar.papcortgs.database.transaction.Transaction;

import java.util.ArrayList;
import java.util.List;

public class FragmentEmail extends Fragment implements EmailCallBack {


    TextView currentLoginName, signOutView;
    ActivityEmailVM viewmodel;
    EmailAdapter adapter;
    RecyclerView recycler;
    EmailService emailService;
    EmailServiceConnection connection;
    boolean mbound = false;
    FloatingActionButton fab;


    // region Override methods ---------------------------------------------

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewmodel = ViewModelProviders.of(getActivity()).get(ActivityEmailVM.class);
        viewmodel.getEmailList().observe(this, new Observer<List<Transaction>>() {
            @Override
            public void onChanged(@Nullable List<Transaction> transactions) {
                if (transactions == null)
                    return;

                adapter.setData(transactions);
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        if (EmailService.isRunning() && !mbound) {

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
        if (EmailService.isRunning() && mbound) {
            unBindFromEmailService();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View ui = inflater.inflate(R.layout.fragment_gmail, container, false);
        currentLoginName = ui.findViewById(R.id.current_login_email);
        signOutView = ui.findViewById(R.id.email_signout);
        recycler = ui.findViewById(R.id.email_recycler);
        fab = ui.findViewById(R.id.email_fab);

        currentLoginName.setText(GoogleSignIn.getLastSignedInAccount(getActivity().getApplicationContext()).getEmail());
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
                    Toast.makeText(getActivity(), "No active internet connection found. Please try again later!", Toast.LENGTH_SHORT).show();
                    return;
                }

                checkPermissionAndStartSending();
                //printProfileDetails();
            }
        });

        if (adapter == null)
            adapter = new EmailAdapter(new ArrayList<Transaction>());

        recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        recycler.addItemDecoration(new DividerDecoration(getActivity(), 1, getActivity().getResources().getColor(R.color.selectionGrey)));
        recycler.setAdapter(adapter);
        return ui;
    }

    // endregion override methods ---------------------------------------------

    private void showSignOutDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Sure want to sign out of " + GoogleSignIn.getLastSignedInAccount(getActivity().getApplicationContext()).getEmail() + "?");
        builder.setNegativeButton("CANCEL", null);
        builder.setPositiveButton("SIGN OUT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                signOut();
            }
        });
        builder.show();
    }

    private void signOut() {
        viewmodel.clearEmailList();
        ((ActivityEmail) getActivity()).signOut();
    }


    public void startEmailService() {

        Intent intent=EmailService.getStartingIntent(getActivity(),viewmodel.currentGroupId,viewmodel.currentGroupName);
        getActivity().startService(intent);
        bindToEmailService();
        updateLayout(true);

    }

    private void stopEmailService() {

        unBindFromEmailService();
        getActivity().startService(EmailService.getStoppingIntent(getActivity()));
        updateLayout(false);

    }

    private void bindToEmailService() {

        mbound = true;
        Intent bindIntent = new Intent(getActivity(), EmailService.class);
        if (connection == null)
            connection = new EmailServiceConnection();
        getActivity().bindService(bindIntent, connection, Context.BIND_AUTO_CREATE);


    }

    private void unBindFromEmailService() {

        mbound = false;
        //updateLayout(false);
        emailService.removeCallBack();
        getActivity().unbindService(connection);


    }



    private void checkPermissionAndStartSending() {

        if (((ActivityEmail) getActivity()).weHaveAccountsPermission()) {
            showSendConfirmDialog();
        } else {
            ((ActivityEmail) getActivity()).requestAccountsPermission();
        }

    }

    public void showSendConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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

            fabAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.hide_view_shrink);
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

            signoutAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.hide_view_shrink);
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

            fabAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.show_view_grow);
            fab.show();
            signOutView.setVisibility(View.VISIBLE);
            fab.startAnimation(fabAnim);
            signOutView.startAnimation(fabAnim);

        }

    }


    private boolean weHaveInternetConnection() {

        ConnectivityManager cmanager = (ConnectivityManager) getActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cmanager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting())
            return true;
        else
            return false;
    }


    // region Observers for service ----------------------------------------------------

    @Override
    public void onObserverAttached(List<Transaction> currentList) {

        if(viewmodel.getEmailList().getValue()==null)
            viewmodel.getEmailList().setValue(currentList);
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
        if (viewmodel.getEmailList().getValue() == null)
            viewmodel.getEmailList().setValue(list);

        stopEmailService();
    }

    //endregion Observers for service ----------------------------------------------------

    class EmailAdapter extends RecyclerView.Adapter<EmailAdapter.EmailViewHolder> {


        private final int COLOR_RED = getResources().getColor(android.R.color.holo_red_dark);
        private final int COLOR_GREEN = getResources().getColor(android.R.color.holo_green_dark);
        private final int COLOR_BLACK = getResources().getColor(android.R.color.black);
        private final int COLOR_GREY=getResources().getColor(android.R.color.darker_gray);

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
                    holder.status.setText("SENDING FAILED");
                    holder.status.setTextColor(COLOR_RED);
                    break;

                case EmailService.STATUS_SENT:
                    holder.status.setText("MAIL SENT");
                    holder.status.setTextColor(COLOR_GREEN);
                    break;

                case EmailService.STATUS_QUEUED:
                    holder.status.setText("QUEUED");
                    holder.status.setTextColor(COLOR_GREY);
                    break;

                case EmailService.STATUS_SENDING:
                    holder.status.setText("SENDING...");
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

            // TODO: 19-11-2018 update the layout for progress
            emailService = ((EmailService.EmailBinder) iBinder).getService();
            emailService.setCallBack(FragmentEmail.this);
            emailService.setListAndStartSending(viewmodel.getEmailList().getValue());

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            emailService = null;

        }
    }
}
