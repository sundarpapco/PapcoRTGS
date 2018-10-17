package com.papco.sundar.papcortgs;

import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class GroupCreateEditDialog extends DialogFragment {

    GroupActivityVM viewmodel;
    TransactionGroup editingGroup=null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewmodel=ViewModelProviders.of(getActivity()).get(GroupActivityVM.class);
        editingGroup=viewmodel.editingGroup;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if(editingGroup==null)
            return createNewAlertDialog();
        else
            return createEditAlertDialog();

    }

    private AlertDialog createNewAlertDialog(){

        View view=getActivity().getLayoutInflater().inflate(R.layout.new_group,null);
        final EditText editText=view.findViewById(R.id.new_tag_editText);

        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle("New XL file");
        builder.setView(view);
        builder.setPositiveButton("CREATE",null);
        builder.setNegativeButton("CANCEL",null);

        final AlertDialog dialog=builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        validateAndCreateNewGroup(dialog,editText);
                    }
                });
            }
        });
        return dialog;
    }

    private AlertDialog createEditAlertDialog(){
        View view=getActivity().getLayoutInflater().inflate(R.layout.new_group,null);
        final EditText editText=view.findViewById(R.id.new_tag_editText);
        editText.setText(editingGroup.name);

        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle("Edit XL filename");
        builder.setView(view);
        builder.setPositiveButton("SAVE",null);
        builder.setNegativeButton("CANCEL",null);
        builder.setNeutralButton("DELETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteGroup(editingGroup);
            }
        });


        final AlertDialog dialog=builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        validateAndUpdateGroup(dialog,editText);
                    }
                });
            }
        });
        return dialog;
    }

    private void validateAndUpdateGroup(AlertDialog dialog, EditText groupName) {
        String enteredTag=groupName.getText().toString();
        if(enteredTag.trim().equals("")){
            Toast.makeText(getActivity(),"Please enter a XL file name",Toast.LENGTH_SHORT).show();
            return;
        }

        dialog.dismiss();
        editingGroup.name=groupName.getText().toString();
        viewmodel.updateTransactionGroup(editingGroup);
    }

    private void validateAndCreateNewGroup(AlertDialog dialog,EditText groupName) {
        String enteredTag=groupName.getText().toString();
        if(enteredTag.trim().equals("")){
            Toast.makeText(getActivity(),"Please enter a XL file name",Toast.LENGTH_SHORT).show();
            return;
        }

        dialog.dismiss();
        TransactionGroup newGroup=new TransactionGroup();
        newGroup.name=groupName.getText().toString();
        viewmodel.addTransactionGroup(newGroup);
    }

    private void deleteGroup(final TransactionGroup delGroup    ){

        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setMessage("Sure delete XL file "+delGroup.name+" ?");
        builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                viewmodel.deleteTransactionGroup(delGroup);
            }
        });
        builder.setNegativeButton("CANCEL",null);
        builder.create().show();
    }
}
