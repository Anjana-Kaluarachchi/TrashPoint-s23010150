package com.s23010150.trashpoint_v1;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;


public class RequestDetailActivity extends AppCompatActivity {
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_detail);

        db = FirebaseFirestore.getInstance();

        TextView textViewWasteType = findViewById(R.id.textViewWasteType);
        TextView textViewOtherWaste = findViewById(R.id.textViewOtherWaste);
        TextView textViewAddress = findViewById(R.id.textViewAddress);
        TextView textViewPhone = findViewById(R.id.textViewPhone);
        TextView textViewDate = findViewById(R.id.textViewDate);
        TextView textViewStatus = findViewById(R.id.textViewStatus);
        ImageView imageView = findViewById(R.id.imageView);

        String requestId = getIntent().getStringExtra("requestId");
        String imageUrl = getIntent().getStringExtra("imageUrl");


        db.collection("requests").document(requestId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        textViewWasteType.setText("Waste Type: " + (documentSnapshot.getString("wasteType") != null ? documentSnapshot.getString("wasteType") : "Not specified"));
                        textViewOtherWaste.setText("Other: " + (documentSnapshot.getString("otherWaste") != null ? documentSnapshot.getString("otherWaste") : "None"));
                        textViewAddress.setText("Address: " + documentSnapshot.getString("address"));
                        textViewPhone.setText("Phone: " + documentSnapshot.getString("phone"));
                        textViewDate.setText("Date: " + documentSnapshot.getString("date"));
                        textViewStatus.setText("Status: " + documentSnapshot.getString("status"));
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(this).load(imageUrl).into(imageView);
                        }
                    }
                });

        textViewWasteType.setText("Waste Type: " + (getIntent().hasExtra("wasteType") ? getIntent().getStringExtra("wasteType") : "Not specified"));
        textViewOtherWaste.setText("Other: " + (getIntent().hasExtra("otherWaste") ? getIntent().getStringExtra("otherWaste") : "None"));
        textViewAddress.setText("Address: " + (getIntent().hasExtra("address") ? getIntent().getStringExtra("address") : ""));
        textViewPhone.setText("Phone: " + (getIntent().hasExtra("phone") ? getIntent().getStringExtra("phone") : ""));
        textViewDate.setText("Date: " + (getIntent().hasExtra("date") ? getIntent().getStringExtra("date") : ""));
        textViewStatus.setText("Status: " + (getIntent().hasExtra("status") ? getIntent().getStringExtra("status") : "pending"));
    }
}