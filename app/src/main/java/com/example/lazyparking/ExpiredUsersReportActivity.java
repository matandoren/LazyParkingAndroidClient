package com.example.lazyparking;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

public class ExpiredUsersReportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expired_users_report);

        RecyclerView recyclerView = findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ExpiredUsersReportRecyclerAdapter adapter = new ExpiredUsersReportRecyclerAdapter(this, ((OperatorActivity)MainActivity.activeUserActivity).expiredUsersList);
        recyclerView.setAdapter(adapter);
    }
}
