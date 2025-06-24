package com.s23010150.trashpoint_v1;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ConfirmRequestActivity extends AppCompatActivity {
    private TextView textViewDetails;
    private Button buttonConfirm;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_request);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        textViewDetails = findViewById(R.id.textViewDetails);
        buttonConfirm = findViewById(R.id.buttonConfirm);

        Intent intent = getIntent();
        String wasteType = intent.getStringExtra("wasteType");
        String otherWaste = intent.getStringExtra("otherWaste");
        String address = intent.getStringExtra("address");
        String phone = intent.getStringExtra("phone");
        String date = intent.getStringExtra("date");
        String imageUrl = intent.getStringExtra("imageUrl");

        if (wasteType == null || address == null || phone == null || date == null || imageUrl == null) {
            Toast.makeText(this, "Missing required fields", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String details = "Waste Type: " + (wasteType.isEmpty() ? "Not specified" : wasteType) +
                "\nOther: " + (otherWaste != null ? otherWaste : "None") +
                "\nAddress: " + address +
                "\nPhone: " + phone +
                "\nDate: " + date +
                "\nImage URL: " + imageUrl;
        textViewDetails.setText(details);

        buttonConfirm.setOnClickListener(v -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                String requestId = db.collection("requests").document().getId();
                String userId = user.getUid();
                db.collection("requests").document(requestId)
                        .set(new Request(wasteType, otherWaste, address, phone, date, "pending", userId, imageUrl))
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(ConfirmRequestActivity.this, "Request submitted successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(ConfirmRequestActivity.this, ViewRequestsActivity.class));
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(ConfirmRequestActivity.this, "Failed to submit request", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        });
            } else {
                Toast.makeText(ConfirmRequestActivity.this, "User not authenticated", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public static class Request {
        private String wasteType;
        private String otherWaste;
        private String address;
        private String phone;
        private String date;
        private String status;
        private String userId;
        private String imageUrl;

        public Request() {}

        public Request(String wasteType, String otherWaste, String address, String phone, String date,
                       String status, String userId, String imageUrl) {
            this.wasteType = wasteType;
            this.otherWaste = otherWaste;
            this.address = address;
            this.phone = phone;
            this.date = date;
            this.status = status;
            this.userId = userId;
            this.imageUrl = imageUrl;
        }

        public String getWasteType() { return wasteType; }
        public String getOtherWaste() { return otherWaste; }
        public String getAddress() { return address; }
        public String getPhone() { return phone; }
        public String getDate() { return date; }
        public String getStatus() { return status; }
        public String getUserId() { return userId; }
        public String getImageUrl() { return imageUrl; }
    }
}
