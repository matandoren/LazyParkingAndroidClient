package com.example.lazyparking;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

public class AdvancedSettingsDialog extends AppCompatDialogFragment {
    private EditText newIP;
    private EditText newPort;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.advanced_settings_layout, null);

        builder.setView(view)
                .setTitle(R.string.advance_settings_menu)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor spEditor = view.getContext().getSharedPreferences(getString(R.string.shared_prefs_filename), Context.MODE_PRIVATE).edit();

                        String ip = newIP.getText().toString();
                        String port = newPort.getText().toString();
                        if (ip.equals("") && port.equals(""))
                            return;
                        if (!ip.equals("")) {
                            spEditor.putString("SERVER_IP", ip);
                            MainActivity.activeMainActivity.SERVER_IP = ip;
                        }
                        if (!port.equals("")) {
                            int portNumber = Integer.parseInt(port);
                            spEditor.putInt("SERVER_PORT", portNumber);
                            MainActivity.activeMainActivity.SERVER_PORT = portNumber;
                        }
                        spEditor.apply();
                    }
                });
        newIP = view.findViewById(R.id.newIP_ET);
        newPort = view.findViewById(R.id.newPort_ET);

        return builder.create();
    }
}
