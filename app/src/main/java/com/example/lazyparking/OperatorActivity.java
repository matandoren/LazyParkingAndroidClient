package com.example.lazyparking;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import common.Reply;
import common.ReplyMessage;

public class OperatorActivity extends DriverActivity implements OperatorChangePasswordDialog.OperatorChangePasswordDialogListener, AddDriverDialog.AddDriverDialogListener, DriverChangePasswordDialog.DriverChangePasswordDialogListener, ChangeDriverExpirationDateDialog.ChangeDriverExpirationDateDialogListener, RemoveDriverDialog.RemoveDriverDialogListener, ReserveParkingSpotDialog.ReserveParkingSpotDialogListener {

    private final int REQUEST_CALL = 1;
    private final String GATE_PHONE_NUMBER = "5555555555";
    private int mostRecentlyClickedParkingSpotId;
    List<ExpiredUsersReportRecyclerAdapter.UsernameExpirationDatePair> expiredUsersList;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_operator);
        super.onCreate(savedInstanceState);
        MainActivity.activeUserActivity = this;

        mapView.setOnTouchListener(new View.OnTouchListener() {
            private final int CLICK_ACTION_THRESHOLD = 50;
            private float startX;
            private float startY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        float endX = event.getX();
                        float endY = event.getY();
                        if (endX - startX <= CLICK_ACTION_THRESHOLD && endX - startX >= -CLICK_ACTION_THRESHOLD && endY - startY <= CLICK_ACTION_THRESHOLD && endY - startY >= -CLICK_ACTION_THRESHOLD) {
                            toggleReservation((endX + startX) / 2.f, (endY + startY) / 2.f);
                            return true;
                        }
                }
                return mapView.onTouchEvent(event);
            }
        });

    }

    private void toggleReservation(float x, float y) {
        ParkingSpot spot = idToSpot.get(mapView.getParkingSpotAt(x, y));


        if (spot != null) {
            if (spot.isReserved)
                new AlertDialog.Builder(this)
                        .setTitle(R.string.cancel_reservation_title)
                        .setMessage(getString(R.string.cancel_reservation_message) + "\n" + spot.id)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.apply, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                new CancelReservationTask().execute(spot.id);
                            }
                        }).show();
            else {
                mostRecentlyClickedParkingSpotId = spot.id;
                (new ReserveParkingSpotDialog()).show(getSupportFragmentManager(), "Reserve Parking Spot");
            }
            mapView.invalidate();
        }

    }

    public int getMostRecentlyClickedParkingSpotId() {
        return mostRecentlyClickedParkingSpotId;
    }

    @Override
    public void applyNewParkingSpotReservation(String name, Date date, int parkingSpotNumber) {
        if (name.length() == 0 || name.length() > 20) {
            MainActivity.activeMainActivity.playMusic.StarSound();
            Toast.makeText(this, R.string.reservation_error_message, Toast.LENGTH_LONG).show();
        }
        else if (!name.matches("[A-Za-z][A-Za-z ]*")) {
            MainActivity.activeMainActivity.playMusic.StarSound();
            Toast.makeText(this, R.string.invalid_chars_message, Toast.LENGTH_LONG).show();
        }
        else {
            new ReserveParkingSpotTask().execute(name, date, parkingSpotNumber);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_operator, menu);
        inflater.inflate(R.menu.menu_driver, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId()){
            case R.id.open_gate:
                makePhoneCall();
                return true;
            case R.id.add_driver:
                openAddDriverDialog();
                return true;
            case R.id.change_driver_expiration_date:
                openChangeDriverExpirationDateDialog();
                return true;
            case R.id.remove_driver:
                openRemoveDriverDialog();
                return true;
            case R.id.expired_users_report:
                openExpireUsersReportDialog();
                return true;
            case R.id.change_password_button:
                openChangePasswordDriverDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openAddDriverDialog() {
        AddDriverDialog addDriver = new AddDriverDialog();
        addDriver.show(getSupportFragmentManager(),"Add Driver");
    }

    @Override
    public void applyNewDriver(String name, String password, Date date) {
        String[] names = name.split(" ");
        if(name.equals("") || password.equals("")) {
            MainActivity.activeMainActivity.playMusic.StarSound();
            Toast.makeText(this, R.string.name_pw_not_empty_msg, Toast.LENGTH_LONG).show();
        }
        else if (!name.matches("[A-Za-z]{2,}[ ]+[A-Za-z]{2,}")) {
            MainActivity.activeMainActivity.playMusic.StarSound();
            Toast.makeText(this, R.string.add_driver_instructions, Toast.LENGTH_LONG).show();
        }
        else if (names[0].length() + names[1].length() > 20) {
            MainActivity.activeMainActivity.playMusic.StarSound();
            Toast.makeText(this, R.string.names_too_long_message, Toast.LENGTH_LONG).show();
        }
        else {
            new AddDriverTask().execute(name, password, date);
        }
    }

    private void openChangeDriverExpirationDateDialog() {
        ChangeDriverExpirationDateDialog changeDriverExpirationDateDialog = new ChangeDriverExpirationDateDialog();
        changeDriverExpirationDateDialog.show(getSupportFragmentManager(), "Change Driver Expiration Date");
    }

    @Override
    public void applyChangeDriverExpirationDate(String username, Date date) {
        if(username.equals("")) {
            MainActivity.activeMainActivity.playMusic.StarSound();
            Toast.makeText(this, R.string.username_mustnt_be_empty_message, Toast.LENGTH_LONG).show();
        }
        else{
            new ChangeDriverExpirationDateTask().execute(username, date);
        }
    }

    private void openRemoveDriverDialog() {
        RemoveDriverDialog removeDriverDialog = new RemoveDriverDialog();
        removeDriverDialog.show(getSupportFragmentManager(),"Remove Driver");
    }

    @Override
    public void applyRemoveDriver(String username) {
        if(username.equals("")) {
            MainActivity.activeMainActivity.playMusic.StarSound();
            Toast.makeText(this, R.string.username_mustnt_be_empty_message, Toast.LENGTH_LONG).show();
        }
        else{
            new RemoveDriverTask().execute(username);
        }
    }

    private void openChangePasswordDriverDialog() {
        OperatorChangePasswordDialog operatorChangePasswordDialog = new OperatorChangePasswordDialog();
        operatorChangePasswordDialog.show(getSupportFragmentManager(),"Change Password");
    }

    @Override
    public void applyNewPasswordForOther(String password, String username) {
        if (username.equals(""))
            username = this.username;

        if (password.equals("")) {
            MainActivity.activeMainActivity.playMusic.StarSound();
            Toast.makeText(this, R.string.password_mustnt_be_empty_message, Toast.LENGTH_LONG).show();
        }
        else
            new changePasswordTask().execute(username, password);
    }

    private void openExpireUsersReportDialog() {
        new GetExpiredUsersTask().execute();
    }


    private void makePhoneCall(){
        if(ContextCompat.checkSelfPermission(OperatorActivity.this,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(OperatorActivity.this,
                    new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL);
        }
        else{
            String dial = "tel:" + GATE_PHONE_NUMBER;
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CALL){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                makePhoneCall();
            else {
                MainActivity.activeMainActivity.playMusic.StarSound();
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }


    protected static class ChangeDriverExpirationDateTask extends AsyncTask<Object, Void, ReplyMessage> {

        @Override
        protected ReplyMessage doInBackground(Object... objects) {
            String username = (String)objects[0];
            Date date = (Date)objects[1];
            MainActivity.messagePassing.sendUpdateDriverExpirationRequest(username, date);
            return MainActivity.messagePassing.getNextUpdateDriverExpirationReply();
        }

        @Override
        protected void onPostExecute(ReplyMessage reply) {
            String message;
            if (reply.reply == Reply.SUCCESS)
                message = MainActivity.activeUserActivity.getString(R.string.expiration_date_changed_success_message);
            else if (reply.reply == Reply.USERNAME_NOT_FOUND) {
                MainActivity.activeMainActivity.playMusic.StarSound();
                message = MainActivity.activeUserActivity.getString(R.string.username_not_found_message);
            }
            else {
                MainActivity.activeMainActivity.playMusic.StarSound();
                message = MainActivity.activeUserActivity.getString(R.string.date_expired_message);
            }

            Toast.makeText(MainActivity.activeUserActivity, message,Toast.LENGTH_LONG).show();
        }
    }

    private static class AddDriverTask extends AsyncTask<Object, Void, ReplyMessage> {

        @Override
        protected ReplyMessage doInBackground(Object... objects) {
            String name = (String)objects[0];
            String password = (String)objects[1];
            Date date = (Date)objects[2];
            MainActivity.messagePassing.sendAddDriverRequest(name, password, date);
            return MainActivity.messagePassing.getNextAddDriverReply();
        }

        @Override
        protected void onPostExecute(ReplyMessage reply) {
            if (reply.reply == Reply.SUCCESS) {
                (new AlertDialog.Builder(MainActivity.activeUserActivity))
                        .setTitle(R.string.add_driver_popup_title)
                        .setMessage(MainActivity.activeUserActivity.getString(R.string.username_hint) + ": " + reply.stringField + "\n" + MainActivity.activeUserActivity.getString(R.string.card_key_str) + ": " + reply.intField)
                        .show();
            }
            else {
                MainActivity.activeMainActivity.playMusic.StarSound();
                Toast.makeText(MainActivity.activeUserActivity, MainActivity.activeUserActivity.getString(R.string.date_expired_message), Toast.LENGTH_LONG).show();
            }
        }
    }

    protected static class RemoveDriverTask extends AsyncTask<String, Void, ReplyMessage> {

        @Override
        protected ReplyMessage doInBackground(String... strings) {

            MainActivity.messagePassing.sendDeleteDriverRequest(strings[0]);

            return MainActivity.messagePassing.getNextDeleteDriverReply();
        }

        @Override
        protected void onPostExecute(ReplyMessage reply) {
            if (reply.reply == Reply.SUCCESS)
                Toast.makeText(MainActivity.activeUserActivity, R.string.driver_removed_message, Toast.LENGTH_LONG).show();
            else {
                MainActivity.activeMainActivity.playMusic.StarSound();
                Toast.makeText(MainActivity.activeUserActivity, R.string.remove_driver_fail_message, Toast.LENGTH_LONG).show();
            }
        }
    }

    protected static class ReserveParkingSpotTask extends AsyncTask<Object, Void, ReplyMessage> {

        @Override
        protected ReplyMessage doInBackground(Object... objects) {
            String reservationMessage = (String) objects[0];
            Date expirationDate = (Date)objects[1];
            int parkingSpotId = (int)objects[2];

            MainActivity.messagePassing.sendReserveParkingSpotRequest(parkingSpotId, reservationMessage, expirationDate);

            return MainActivity.messagePassing.getNextReserveParkingSpotReply();
        }

        @Override
        protected void onPostExecute(ReplyMessage reply) {
            if (reply.reply == Reply.SUCCESS)
                Toast.makeText(MainActivity.activeUserActivity, R.string.reservation_success_message, Toast.LENGTH_LONG).show();
            else {
                MainActivity.activeMainActivity.playMusic.StarSound();
                Toast.makeText(MainActivity.activeUserActivity, R.string.reservation_expired_message, Toast.LENGTH_LONG).show();
            }
        }
    }

    protected static class CancelReservationTask extends AsyncTask<Integer, Void, ReplyMessage> {

        @Override
        protected ReplyMessage doInBackground(Integer... integers) {
            MainActivity.messagePassing.sendCancelReservationRequest(integers[0]);

            return MainActivity.messagePassing.getNextCancelReservationReply();
        }

        @Override
        protected void onPostExecute(ReplyMessage reply) {
            if (reply.reply == Reply.SUCCESS)
                Toast.makeText(MainActivity.activeUserActivity, R.string.reservation_cancelled_message, Toast.LENGTH_LONG).show();
        }
    }

    private static class GetExpiredUsersTask extends AsyncTask<Void, Void, List<ExpiredUsersReportRecyclerAdapter.UsernameExpirationDatePair>> {

        @Override
        protected List<ExpiredUsersReportRecyclerAdapter.UsernameExpirationDatePair> doInBackground(Void... voids) {
            List<ExpiredUsersReportRecyclerAdapter.UsernameExpirationDatePair> list = new LinkedList<>();
            ReplyMessage reply;
            do {
                MainActivity.messagePassing.sendRequestForExpiredUsersRequest();
                reply = MainActivity.messagePassing.getNextRequestForExpiredUsersReply();
                if (reply.reply == Reply.SUCCESS) {
                    ExpiredUsersReportRecyclerAdapter.UsernameExpirationDatePair pair = new ExpiredUsersReportRecyclerAdapter.UsernameExpirationDatePair();
                    pair.username = reply.stringField;
                    pair.expirationDate = reply.dateField;
                    list.add(pair);
                }
            } while (reply.reply == Reply.SUCCESS);
            return list;
        }

        @Override
        protected void onPostExecute(List<ExpiredUsersReportRecyclerAdapter.UsernameExpirationDatePair> list) {
            ((OperatorActivity)MainActivity.activeUserActivity).expiredUsersList = list;
            MainActivity.activeUserActivity.startActivity(new Intent(MainActivity.activeUserActivity, ExpiredUsersReportActivity.class));
        }
    }

}
