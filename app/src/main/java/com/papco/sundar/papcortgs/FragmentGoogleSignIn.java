package com.papco.sundar.papcortgs;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.SignInButton;

public class FragmentGoogleSignIn extends Fragment {

    private static final int RC_SIGNIN=1;

    SignInButton button;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View ui=inflater.inflate(R.layout.fragment_gmail_signin,container,false);
        button=ui.findViewById(R.id.gmail_signin_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        return ui;
    }

    private void signIn() {

        ((ActivityEmail)getActivity()).signIn();
    }
}
