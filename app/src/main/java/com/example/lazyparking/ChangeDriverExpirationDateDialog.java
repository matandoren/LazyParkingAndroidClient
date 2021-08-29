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

public class ChangeDriverExpirationDateDialog extends AppCompatDialogFragment {
    private EditText userName;
    private ChangeDriverExpirationDateDialogListener listener;
    private Date date;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.change_driver_expiration_date_layout, null);

        builder.setView(view)
                .setTitle(R.string.change_driver_expiration_title)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String username = userName.getText().toString();
                        if (((CheckBox)view.findViewById(R.id.unlimited_checkBox)).isChecked())
                            listener.applyChangeDriverExpirationDate(username, null);
                        else
                            listener.applyChangeDriverExpirationDate(username, date);
                    }
                });
        date = new Date();
        userName = view.findViewById(R.id.username_expiration_date);
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
            listener = (ChangeDriverExpirationDateDialogListener)context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +" must implement ChangeDriverExpirationDateDialogListener");
        }
    }

    public interface ChangeDriverExpirationDateDialogListener{
        void applyChangeDriverExpirationDate(String username, Date date);
    }
}
