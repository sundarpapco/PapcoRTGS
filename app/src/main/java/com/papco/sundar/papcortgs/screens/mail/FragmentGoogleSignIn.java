package com.papco.sundar.papcortgs.screens.mail;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.papco.sundar.papcortgs.R;

public class FragmentGoogleSignIn extends Fragment {

    public static final String TAG = "FragmentGoogleSignIn:TAG";

    GoogleSignInClient googleSignInClient;
    ActivityResultLauncher<Intent> signInLauncher = createSignInLauncher();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        googleSignInClient = createGoogleClient();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (container != null)
            container.removeAllViews();

        View ui = inflater.inflate(R.layout.fragment_gmail_signin, container, false);
        SignInButton button = ui.findViewById(R.id.gmail_signin_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        return ui;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (GoogleSignIn.getLastSignedInAccount(requireActivity().getApplicationContext()) != null)
            navigateToEmailFragment();
    }

    private void signIn() {
        signInLauncher.launch(googleSignInClient.getSignInIntent());
    }

    private void navigateToEmailFragment() {

        int containerId = R.id.container;
        if (getView() != null)
            containerId = ((ViewGroup) getView().getParent()).getId();

        FragmentManager manager = getParentFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(containerId, new FragmentEmail(), FragmentEmail.TAG);
        transaction.commit();
    }

    private ActivityResultLauncher<Intent> createSignInLauncher() {

        return registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                handleSignInResult(GoogleSignIn.getSignedInAccountFromIntent(result.getData()));
            }
        });

    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {

        try {
            completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
            navigateToEmailFragment();
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.

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

}
