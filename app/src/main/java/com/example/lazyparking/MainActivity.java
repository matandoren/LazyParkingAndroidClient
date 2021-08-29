package com.example.lazyparking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;

import common.Reply;
import common.ReplyMessage;

import java.io.IOException;


public class MainActivity extends AppCompatActivity {
    static MessagePassing messagePassing;
    static MainActivity activeMainActivity;
    static DriverActivity activeUserActivity;

    public final int OPERATOR_PERMISSION = 2;
    String SERVER_IP;
    int SERVER_PORT;
    private EditText usernameET;
    private EditText pwET;
    private String username;
    PlayMusic playMusic;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().getDecorView().setBackgroundColor(Color.WHITE);
        activeMainActivity = this;


        SharedPreferences sp = getSharedPreferences(getString(R.string.shared_prefs_filename), MODE_PRIVATE);
        SERVER_IP = sp.getString("SERVER_IP", getString(R.string.server_ip));
        SERVER_PORT = sp.getInt("SERVER_PORT", Integer.parseInt(getString(R.string.server_port)));

        usernameET = findViewById(R.id.usernameField);
        pwET = findViewById(R.id.pwField);
        pwET.setOnEditorActionListener((view, actionId, event)->{
            if (actionId == EditorInfo.IME_ACTION_DONE) {
               String enteredUsername = usernameET.getText().toString();
               String enteredPW = pwET.getText().toString();

                if (enteredUsername.equals("") || enteredPW.equals("")) {
                    Toast.makeText(MainActivity.this, R.string.username_pw_not_empty_msg,Toast.LENGTH_LONG).show();
                } else {
                    new LoginTask().execute(enteredUsername, enteredPW);
                }
            }
            return false;
        });
        playMusic =  new PlayMusic(this);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.advance_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.advance_settings){
            AdvancedSettingsDialog advancedSettingsDialog = new AdvancedSettingsDialog();
            advancedSettingsDialog.show(getSupportFragmentManager(),"Advanced Settings");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        playMusic.release();
    }

    private static class LoginTask extends AsyncTask<String, Void, ReplyMessage> {
        @Override
        protected ReplyMessage doInBackground(String... strings) {
            try {
                if (messagePassing == null)
                    messagePassing = new MessagePassing(activeMainActivity.SERVER_IP, activeMainActivity.SERVER_PORT);
                messagePassing.sendLoginRequest(strings[0], strings[1]);
            } catch (IOException e) {
                ReplyMessage reply = new ReplyMessage();
                reply.reply = Reply.INSUFFICIENT_PRIVILEDGE;
                reply.stringField = e.getMessage();
                return reply;
            }

            return messagePassing.getNextLoginReply();
        }

        @Override
        protected void onPostExecute(ReplyMessage reply) {
            if (reply.reply == Reply.SUCCESS) {
                activeMainActivity.username = reply.stringField; // need to pass this to the next activity
                Intent intent;
                if (reply.intField >= activeMainActivity.OPERATOR_PERMISSION) {// OPERATOR
                    intent = new Intent(activeMainActivity, OperatorActivity.class);
                } else {// DRIVER
                    intent = new Intent(activeMainActivity, DriverActivity.class);
                }
                intent.putExtra("username", activeMainActivity.username);
                activeMainActivity.usernameET.setText("");
                activeMainActivity.pwET.setText("");

                MainActivity temp = activeMainActivity;
                temp.startActivity(intent);
            }
            else if (reply.reply == Reply.USERNAME_NOT_FOUND || reply.reply == Reply.WRONG_PW) {
                new DriverActivity.LogoutTask().execute();
                MainActivity.activeMainActivity.playMusic.StarSound();
                Toast.makeText(activeMainActivity, R.string.login_error_message, Toast.LENGTH_LONG).show();
            }
            else if (reply.reply == Reply.INSUFFICIENT_PRIVILEDGE) {
                MainActivity.activeMainActivity.playMusic.StarSound();
                Toast.makeText(activeMainActivity, reply.stringField, Toast.LENGTH_LONG).show();
            }
        }
    }
}
