package com.s23010150.trashpoint_v1;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button buttonMakeRequest = findViewById(R.id.buttonMakeRequest);
        Button buttonViewRequests = findViewById(R.id.buttonViewRequests);
        Button buttonViewMap = findViewById(R.id.buttonViewMap);
        Button buttonViewPoints = findViewById(R.id.buttonViewPoints);

        buttonMakeRequest.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, MakeRequestActivity.class)));
        buttonViewRequests.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, ViewRequestsActivity.class)));
        buttonViewMap.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, MapsActivity.class)));
        buttonViewPoints.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, PointsActivity.class)));
    }
}