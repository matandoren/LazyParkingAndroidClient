package com.example.lazyparking;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

public class OperatorChangePasswordDialog extends AppCompatDialogFragment {

    private EditText changePasswordEditText;
    private EditText toWhoEditText;
    private OperatorChangePasswordDialogListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.operator_change_password_layout , null);

        builder.setView(view)
                .setTitle(R.string.change_password_in_dialog)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.applyNewPasswordForOther(changePasswordEditText.getText().toString(), toWhoEditText.getText().toString());
                    }
                });
        changePasswordEditText = view.findViewById(R.id.new_password);
        toWhoEditText = view.findViewById(R.id.change_password_username_edit_text);
        toWhoEditText.setText(MainActivity.activeUserActivity.username);

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (OperatorChangePasswordDialogListener)context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +" must implement OperatorChangePasswordDialogListener");
        }
    }

    public interface OperatorChangePasswordDialogListener{
        void applyNewPasswordForOther(String password, String username);
    }
}
