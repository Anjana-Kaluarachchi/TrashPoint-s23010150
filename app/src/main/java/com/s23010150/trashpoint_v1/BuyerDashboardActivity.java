package com.s23010150.trashpoint_v1;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class BuyerDashboardActivity extends AppCompatActivity {
    private LinearLayout linearLayoutRequests;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buyer_dashboard);

        linearLayoutRequests = findViewById(R.id.linearLayoutRequests);
        db = FirebaseFirestore.getInstance();

        db.collection("requests")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        linearLayoutRequests.removeAllViews();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String requestId = document.getId();
                            String wasteType = document.getString("wasteType");
                            String otherWaste = document.getString("otherWaste");
                            String address = document.getString("address");
                            String phone = document.getString("phone");
                            String date = document.getString("date");
                            String status = document.getString("status");
                            String userId = document.getString("userId");
                            String imageUrl = document.getString("imageUrl");


                            LinearLayout requestContainer = new LinearLayout(this);
                            requestContainer.setOrientation(LinearLayout.VERTICAL);
                            requestContainer.setLayoutParams(new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT));
                            requestContainer.setPadding(0, 0, 0, 32);

                            // Waste type
                            TextView textWaste = new TextView(this);
                            textWaste.setText("Waste: " + (wasteType != null ? wasteType : ""));
                            requestContainer.addView(textWaste);

                            // Other waste
                            TextView textOtherWaste = new TextView(this);
                            textOtherWaste.setText("Other Waste: " + (otherWaste != null ? otherWaste : ""));
                            requestContainer.addView(textOtherWaste);

                            // Address
                            TextView textAddress = new TextView(this);
                            textAddress.setText("Address: " + (address != null ? address : ""));
                            requestContainer.addView(textAddress);

                            // Phone
                            TextView textPhone = new TextView(this);
                            textPhone.setText("Phone: " + (phone != null ? phone : ""));
                            requestContainer.addView(textPhone);

                            // Date
                            TextView textDate = new TextView(this);
                            textDate.setText("Date: " + (date != null ? date : ""));
                            requestContainer.addView(textDate);

                            // Status
                            TextView textStatus = new TextView(this);
                            textStatus.setText("Status: " + (status != null ? status : ""));
                            requestContainer.addView(textStatus);

                            // User ID
                            TextView textUserId = new TextView(this);
                            textUserId.setText("Seller User ID: " + (userId != null ? userId : ""));
                            requestContainer.addView(textUserId);

                            // Accept button
                            Button buttonAccept = new Button(this);
                            buttonAccept.setText("Accept");
                            buttonAccept.setOnClickListener(v -> {
                                db.collection("requests").document(requestId)
                                        .update("status", "accepted")
                                        .addOnSuccessListener(aVoid -> {
                                            textStatus.setText("Status: accepted");
                                        });
                            });
                            requestContainer.addView(buttonAccept);


                            Button buttonDecline = new Button(this);
                            buttonDecline.setText("Decline");
                            buttonDecline.setOnClickListener(v -> {
                                db.collection("requests").document(requestId)
                                        .update("status", "declined")
                                        .addOnSuccessListener(aVoid -> {
                                            textStatus.setText("Status: declined");
                                            buttonAccept.setEnabled(false);
                                            buttonDecline.setEnabled(false);
                                        });
                            });
                            requestContainer.addView(buttonDecline);

                            requestContainer.setOnClickListener(v -> {
                                Intent intent = new Intent(BuyerDashboardActivity.this, RequestDetailActivity.class);
                                intent.putExtra("requestId", requestId);
                                intent.putExtra("imageUrl", imageUrl);
                                startActivity(intent);
                            });

                            linearLayoutRequests.addView(requestContainer);
                        }
                    }
                });
    }
}
