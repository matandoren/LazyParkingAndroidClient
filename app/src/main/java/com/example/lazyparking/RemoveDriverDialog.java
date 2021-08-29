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

public class RemoveDriverDialog extends AppCompatDialogFragment {

    private EditText userName;
    private RemoveDriverDialogListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.remove_driver_layout, null);

        builder.setView(view)
                .setTitle(R.string.remove_driver_menu)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String username = userName.getText().toString();
                        listener.applyRemoveDriver(username);
                    }
                });
        userName = view.findViewById(R.id.user_to_remove);

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (RemoveDriverDialogListener)context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +" must implement RemoveDriverDialogListener");
        }
    }

    public interface RemoveDriverDialogListener{
        void applyRemoveDriver(String username);
    }
}
