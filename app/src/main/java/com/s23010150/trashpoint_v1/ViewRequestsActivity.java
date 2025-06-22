package com.s23010150.trashpoint_v1;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ViewRequestsActivity extends AppCompatActivity {
    private TextView textViewRequests;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_requests);

        textViewRequests = findViewById(R.id.textViewRequests);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            db.collection("requests")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            StringBuilder requests = new StringBuilder();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                requests.append("Waste Type: ").append(document.getString("wasteType") != null ? document.getString("wasteType") : "Not specified").append("\n")
                                        .append("Other: ").append(document.getString("otherWaste") != null ? document.getString("otherWaste") : "None").append("\n")
                                        .append("Address: ").append(document.getString("address")).append("\n")
                                        .append("Phone: ").append(document.getString("phone")).append("\n")
                                        .append("Date: ").append(document.getString("date")).append("\n")
                                        .append("Status: ").append(document.getString("status")).append("\n\n");
                            }
                            textViewRequests.setText(requests.length() > 0 ? requests.toString() : "No requests found");
                        } else {
                            textViewRequests.setText("No requests found");
                        }
                    });
        } else {
            textViewRequests.setText("User not authenticated");
        }
    }
}