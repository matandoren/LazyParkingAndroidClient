package com.example.lazyparking;

import android.annotation.SuppressLint;
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
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;


public class ReserveParkingSpotDialog extends AppCompatDialogFragment {
    private EditText reservedForEditText;
    private ReserveParkingSpotDialogListener listener;
    private Date date;
    private Integer parkingSpotNumber;

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.reserve_parking_spot_layout, null);

        builder.setView(view)
                .setTitle(R.string.reserve_parking_spot_title)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (((CheckBox)view.findViewById(R.id.reserve_parking_spot_checkBox)).isChecked())
                            listener.applyNewParkingSpotReservation(reservedForEditText.getText().toString(), null, parkingSpotNumber);
                        else
                            listener.applyNewParkingSpotReservation(reservedForEditText.getText().toString(), date, parkingSpotNumber);
                    }
                });
        date = new Date();
        TextView parkingSpotNumberTextView = view.findViewById(R.id.parking_spot_number_textView);
        reservedForEditText = view.findViewById(R.id.reserved_for_edit_text);
        CalendarView calendarView = view.findViewById(R.id.reserve_parking_spot_calendarView);
        calendarView.setDate(date.getTime());
        calendarView.setOnDateChangeListener((v, y, m, d)->{
            Calendar c = Calendar.getInstance();
            c.set(y, m, d);
            date = new Date(c.getTimeInMillis());
        });
        parkingSpotNumber = ((OperatorActivity)MainActivity.activeUserActivity).getMostRecentlyClickedParkingSpotId();
        parkingSpotNumberTextView.setText(parkingSpotNumber.toString());

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (ReserveParkingSpotDialogListener)context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +" must implement ReserveParkingSpotDialogListener");
        }
    }

    public interface ReserveParkingSpotDialogListener{
        void applyNewParkingSpotReservation(String name, Date date, int parkingSpotNumber);
    }
}
