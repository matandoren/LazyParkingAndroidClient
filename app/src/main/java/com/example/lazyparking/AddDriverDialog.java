package com.example.lazyparking;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.EditText;

import java.util.Calendar;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

public class AddDriverDialog extends AppCompatDialogFragment {

    private EditText userName;
    private EditText password;
    private AddDriverDialogListener listener;
    private Date date;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_driver_layout, null);

        builder.setView(view)
                .setTitle(R.string.add_driver_title)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (((CheckBox)view.findViewById(R.id.unlimited_expiration_date_add_driver)).isChecked())
                            listener.applyNewDriver(userName.getText().toString(), password.getText().toString(), null);
                        else
                            listener.applyNewDriver(userName.getText().toString(), password.getText().toString(), date);
                    }
                });
        date = new Date();
        password = view.findViewById(R.id.new_password_edit_text);
        userName = view.findViewById(R.id.new_username_edit_text);
        CalendarView calendarView = view.findViewById(R.id.calendarView_expiration_date);
        calendarView.setDate(date.getTime());
        calendarView.setOnDateChangeListener((v, y, m, d)->{
            Calendar c = Calendar.getInstance();
            c.set(y, m, d);
            date = new Date(c.getTimeInMillis());
        });

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (AddDriverDialogListener)context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +" must implement AddDriverDialogListener");
        }
    }

    public interface AddDriverDialogListener{
        void applyNewDriver(String name, String password, Date date);
    }
}
