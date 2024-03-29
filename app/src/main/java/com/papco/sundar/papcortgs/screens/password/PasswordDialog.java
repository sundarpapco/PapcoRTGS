package com.papco.sundar.papcortgs.screens.password;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.papco.sundar.papcortgs.R;

public class PasswordDialog extends DialogFragment {

    public static final int CODE_RECEIVERS=1;
    public static final int CODE_SENDERS=2;
    private final String KEY_SAVE_CODE="REQUEST_CODE";

    private PasswordCallback callback;
    private final String PASSWORD="papco1954";
    private int code=1;
    private EditText password;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState!=null){

            code=savedInstanceState.getInt(KEY_SAVE_CODE);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view=getActivity().getLayoutInflater().inflate(R.layout.new_group,null);
        password=view.findViewById(R.id.new_tag_editText);
        password.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD);
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle("Enter password");
        builder.setView(view);
        builder.setNegativeButton("CANCEL",null);
        builder.setPositiveButton("OK", null);
        final AlertDialog dialog=builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        validatePassword(dialog,password.getText().toString());
                    }
                });
            }
        });

        showKeyboard();

        return dialog;


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SAVE_CODE,code);
    }


    private void showKeyboard(){

        InputMethodManager imgr=(InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if(password!=null) {
            password.requestFocus();
            //imgr.showSoftInput(password, InputMethodManager.SHOW_IMPLICIT);
            imgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }

    }

    public void setRequestCode(int code){
        this.code=code;
    }

    private void validatePassword(AlertDialog dialog, String enteredPassword) {

        if(enteredPassword.equals(PASSWORD)){
            dialog.dismiss();
            try{

                callback=(PasswordCallback)getActivity();
                callback.onPasswordOk(code);

            }catch (Exception e){

                Toast.makeText(getActivity(),"Did you forget to implement PasswordCallback in calling activity?",Toast.LENGTH_SHORT).show();

            }
        }else{
            Toast.makeText(getActivity(),"Invalid password. Try again",Toast.LENGTH_SHORT).show();
        }
    }
}
