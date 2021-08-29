package com.example.lazyparking;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;

import common.Reply;
import common.ReplyMessage;


public class DriverActivity extends AppCompatActivity implements DriverChangePasswordDialog.DriverChangePasswordDialogListener {

    private static final int PERMISSION_ALL = 10;
    private static final String[] PERMISSIONS = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA};


    private static final int IMAGE_CAPTURE_CODE = 1001;
    private String EMAIL;

    private Uri image_uri;

    protected MapView mapView;
    private ImageButton upButton;
    private ImageButton downButton;
    private TextView floorIndicator;

    public final int PARKING_SPOT_WIDTH = 42; // in pixels
    public final int PARKING_SPOT_HEIGHT = 82; // in pixels
    public final int ORIGINAL_FLOOR_IMAGE_WIDTH = 294;
    public final int ORIGINAL_FLOOR_IMAGE_HEIGHT = 809;
    public final int NUM_OF_FLOORS = 2;
    private int currentFloor;
    private Floor[] floors;
    protected String username;
    protected HashMap<Integer, ParkingSpot> idToSpot;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_driver, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId()){
            case R.id.change_password_button:
                openChangePasswordDialog();
                return true;
            case R.id.log_out:
                finish();
                return true;
            case R.id.send_email:
                sendMail();
                return true;
            case R.id.make_photo:
                makePhoto();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openChangePasswordDialog() {
        new DriverChangePasswordDialog().show(getSupportFragmentManager(),getString(R.string.change_password_title));
    }

    @Override
    public void applyNewPassword(String password) {
        if(password.equals("")) {
            MainActivity.activeMainActivity.playMusic.StarSound();
            Toast.makeText(this, R.string.password_mustnt_be_empty_message, Toast.LENGTH_LONG).show();
        }
        else {
            new changePasswordTask().execute(username, password);
        }

    }

    public String[] hasPermissions(String[] permissions) {
        if (permissions == null)
            return null;

        boolean[] isPermissionRequired = new boolean[permissions.length];
        int size = 0;
        for (int i = 0; i < permissions.length; i++) {
            if (ActivityCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                isPermissionRequired[i] = true;
                size++;
            }
        }
        if (size == 0)
            return null;
        String[] result = new String[size];
        for (int i = 0, j = 0; i < isPermissionRequired.length; i++)
            if (isPermissionRequired[i])
                result[j++] = permissions[i];

        return result;
    }

    //CAMERA SECTION
    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Image");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From The Camera");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values); // THIS IS THE LINE THAT CREATES THE FILE

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent,IMAGE_CAPTURE_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK)
            sendMail(image_uri);
        else{
            File fdelete = new File(image_uri.getPath());
            if(fdelete.exists()) {

                System.out.println("Before Deleting The File");
                fdelete.delete();
                System.out.println("After Deleting The File");
            }

        }
    }

    private void makePhoto() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            String[] requiredPermissions = hasPermissions(PERMISSIONS);
            if (requiredPermissions != null) {
                ActivityCompat.requestPermissions(this, requiredPermissions, PERMISSION_ALL);
                return;
            }
        }
        openCamera();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode != PERMISSION_ALL || grantResults.length == 0)
            return;
        for (int result : grantResults)
            if(result != PackageManager.PERMISSION_GRANTED) {
                MainActivity.activeMainActivity.playMusic.StarSound();
                Toast.makeText(this, R.string.permission_denied_message,Toast.LENGTH_SHORT).show();
                return;
            }
        openCamera();
    }

    // EMAIL SECTION
    private void sendMail(){
        sendMail(null);
    }

    private void sendMail(Uri uri){
        String subject = "Complaint from: " + username;
        String message = "Someone is parking in my spot";
        String[] emails = {EMAIL};

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL, emails);
        intent.putExtra(Intent.EXTRA_SUBJECT,subject);
        intent.putExtra(Intent.EXTRA_TEXT, message);
        if(uri != null){
            intent.putExtra(Intent.EXTRA_STREAM, image_uri);
        }

        intent.setType("message/rfc822");
        startActivity(Intent.createChooser(intent, "Choose an email client"));
    }

    @SuppressLint({"SourceLockedOrientationActivity", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        username = getIntent().getStringExtra("username");
        MainActivity.activeUserActivity = this;

        floorIndicator = findViewById(R.id.currentFloor);

        currentFloor = 0;
        upButton = findViewById(R.id.upButton);
        downButton = findViewById(R.id.downButton);

        idToSpot = new HashMap<>();
        currentFloor = 0;
        constructFloors();
        mapView = findViewById(R.id.floorCanvas);
        mapView.setFloor(floors[currentFloor]);

        final Animation upButtonAnimation = AnimationUtils.loadAnimation(this, R.anim.up_down_button_animation);
        final Animation downButtonAnimation = AnimationUtils.loadAnimation(this, R.anim.up_down_button_animation);


        upButton.setOnClickListener((e)->{
            e.startAnimation(upButtonAnimation);
            currentFloor++;
            floorIndicator.setText(((Integer) (currentFloor + 1)).toString());
            if (currentFloor == NUM_OF_FLOORS - 1) { // the top floor
                upButton.setEnabled(false);
                upButton.setImageResource(R.drawable.ic_up_grayedout);
                floorChanged();
                if (currentFloor > 0) {// not the bottom floor
                    downButton.setImageResource(R.drawable.ic_down);
                    downButton.setEnabled(true);
                }
            }
        });

        downButton.setOnClickListener((e)->{
            e.startAnimation(downButtonAnimation);
            currentFloor--;
            floorIndicator.setText(((Integer) (currentFloor + 1)).toString());
            if (currentFloor == 0) {// the bottom floor
                downButton.setEnabled(false);
                downButton.setImageResource(R.drawable.ic_down_grayedout);
            }
            floorChanged();
            if (currentFloor < NUM_OF_FLOORS - 1) { // not the top floor
                upButton.setImageResource(R.drawable.ic_up);
                upButton.setEnabled(true);
            }
        });

        if (currentFloor == 0) {
            downButton.setEnabled(false);
            downButton.setImageResource(R.drawable.ic_down_grayedout);
        }
        if (currentFloor == NUM_OF_FLOORS - 1) {
            upButton.setEnabled(false);
            upButton.setImageResource(R.drawable.ic_up_grayedout);
        }

        EMAIL = getString(R.string.email);

        floorChanged();
    }

    private void floorChanged() {
        LinkedList<Integer> unknownParkingSpots = new LinkedList<>();
        for (int i = 0; i < floors[currentFloor].rows.length; i++)
            for (int j = 0; j < floors[currentFloor].rows[i].parkingSpots.length; j++)
                if (!floors[currentFloor].rows[i].parkingSpots[j].isValid)
                    unknownParkingSpots.add(floors[currentFloor].rows[i].parkingSpots[j].id);

        mapView.setFloor(floors[currentFloor]);

        new GetParkingStatusTask().execute(unknownParkingSpots);
    }

    private void constructFloors() {
        floors = new Floor[NUM_OF_FLOORS];
        int k = 0;
        for (int i = 0; i < floors.length; i++) {
            floors[i] = new Floor(BitmapFactory.decodeResource(getResources(), R.drawable.floor), ORIGINAL_FLOOR_IMAGE_WIDTH, ORIGINAL_FLOOR_IMAGE_HEIGHT);
            floors[i].rows = new ParkingRow[2];
            floors[i].rows[0] = new ParkingRow();
            floors[i].rows[0].parkingSpotWidth = PARKING_SPOT_WIDTH;
            floors[i].rows[0].parkingSpotHeight = PARKING_SPOT_HEIGHT;
            floors[i].rows[0].upperLeftX = 6;
            floors[i].rows[0].upperLeftY = 3;
            floors[i].rows[0].parkingSpots = new ParkingSpot[19];
            for (int j = 0; j < floors[i].rows[0].parkingSpots.length; j++) {
                floors[i].rows[0].parkingSpots[j] = new ParkingSpot(++k, i);
            }
            floors[i].rows[1] = new ParkingRow();
            floors[i].rows[1].parkingSpotWidth = PARKING_SPOT_WIDTH;
            floors[i].rows[1].parkingSpotHeight = PARKING_SPOT_HEIGHT;
            floors[i].rows[1].upperLeftX = 200;
            floors[i].rows[1].upperLeftY = 130;
            floors[i].rows[1].parkingSpots = new ParkingSpot[14];
            for (int j = 0; j < floors[i].rows[1].parkingSpots.length; j++)
                floors[i].rows[1].parkingSpots[j] = new ParkingSpot(++k, i);
        }
    }

    public void setParkingSpot(int id, boolean isOccupied, boolean isReserved, String reservedFor) {
        ParkingSpot parkingSpot = idToSpot.get(id);
        if (parkingSpot != null) {
            parkingSpot.isValid = true;
            parkingSpot.isOccupied = isOccupied;
            parkingSpot.isReserved = isReserved;
            if (isReserved)
                parkingSpot.reservedFor = reservedFor;
            else
                parkingSpot.reservedFor = null;
        }
        mapView.invalidate();
    }


    class Floor {
        ParkingRow[] rows;
        Bitmap image;
        final int originalImageWidth;
        final int originalImageHeight;

        Floor(Bitmap image, int originalImageWidth, int originalImageHeight) {
            this.image = image;
            this.originalImageWidth = originalImageWidth;
            this.originalImageHeight = originalImageHeight;
        }

    }

    @Override
    protected void onDestroy() {
        new LogoutTask().execute();
        MainActivity.activeUserActivity = null;
        super.onDestroy();
    }

    class ParkingSpot {
        boolean isValid;
        int id;
        boolean isOccupied;
        boolean isReserved;
        String reservedFor;
        int floor;

        ParkingSpot(int id, int floor) {
            this.id = id;
            this.floor = floor;
            isValid = false;
            isOccupied = false;
            reservedFor = null;
            idToSpot.put(id, this);
        }
    }


    class ParkingRow {
        int upperLeftX;
        int upperLeftY;
        ParkingSpot[] parkingSpots;
        int parkingSpotWidth;
        int parkingSpotHeight;
    }

    private static class GetParkingStatusTask extends AsyncTask<LinkedList<Integer>, Void, Void> {
        @Override
        protected Void doInBackground(LinkedList<Integer>... linkedLists) {
            for(Integer parkingSpotId : linkedLists[0])
                MainActivity.messagePassing.sendGetParkingStatusRequest(parkingSpotId);
            return null;
        }
    }

    static class LogoutTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            MainActivity.messagePassing.sendLogoutRequest();
            MainActivity.messagePassing = null;
            return null;
        }
    }


    protected static class changePasswordTask extends AsyncTask<String, Void, ReplyMessage> {

        @Override
        protected ReplyMessage doInBackground(String... strings) {

            MainActivity.messagePassing.sendChangePasswordRequest(strings[0], strings[1]);

            return MainActivity.messagePassing.getNextChangePasswordReply();
        }

        @Override
        protected void onPostExecute(ReplyMessage reply) {
            if (reply.reply == Reply.SUCCESS) {
                Toast.makeText(MainActivity.activeUserActivity, R.string.password_change_success_message, Toast.LENGTH_LONG).show();
            }
            else {
                MainActivity.activeMainActivity.playMusic.StarSound();
                Toast.makeText(MainActivity.activeUserActivity, R.string.password_change_fail_message, Toast.LENGTH_LONG).show();
            }
        }
    }
}
