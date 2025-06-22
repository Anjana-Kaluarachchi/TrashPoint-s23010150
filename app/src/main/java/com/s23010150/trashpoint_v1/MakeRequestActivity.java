package com.s23010150.trashpoint_v1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class MakeRequestActivity extends AppCompatActivity {
    private EditText editTextOtherWaste, editTextAddress, editTextPhone, editTextDate, editTextWeight;
    private RadioGroup radioGroupWasteType;
    private Button buttonProceed, buttonAddPhoto;
    private ImageView imageViewPhoto;
    private Uri imageUri;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openImagePicker();
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    imageViewPhoto.setImageURI(imageUri);
                    Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Intent> takePhotoLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    imageUri = result.getData() != null ? result.getData().getData() : null;
                    if (imageUri != null) {
                        imageViewPhoto.setImageURI(imageUri);
                        Toast.makeText(this, "Photo taken", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_request);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        radioGroupWasteType = findViewById(R.id.radioGroupWasteType);
        editTextOtherWaste = findViewById(R.id.editTextOtherWaste);
        editTextWeight = findViewById(R.id.editTextWeight);
        editTextAddress = findViewById(R.id.editTextAddress);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextDate = findViewById(R.id.editTextDate);
        buttonProceed = findViewById(R.id.buttonProceed);
        buttonAddPhoto = findViewById(R.id.buttonAddPhoto);
        imageViewPhoto = findViewById(R.id.imageViewPhoto);

        buttonAddPhoto.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            } else {
                openImagePicker();
            }
        });

        buttonProceed.setOnClickListener(v -> {
            String wasteType = getWasteType();
            String otherWaste = editTextOtherWaste.getText().toString();
            String weight = editTextWeight.getText().toString();
            String address = editTextAddress.getText().toString();
            String phone = editTextPhone.getText().toString();
            String date = editTextDate.getText().toString();

            if (wasteType.isEmpty() || address.isEmpty() || phone.isEmpty() || date.isEmpty() || weight.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                if (imageUri != null) {
                    String imageName = "requests/" + user.getUid() + "/" + System.currentTimeMillis() + ".jpg";
                    StorageReference imageRef = storageRef.child(imageName);
                    UploadTask uploadTask = imageRef.putFile(imageUri);
                    uploadTask.addOnSuccessListener(taskSnapshot -> {
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            Intent intent = new Intent(MakeRequestActivity.this, ConfirmRequestActivity.class);
                            intent.putExtra("wasteType", wasteType);
                            intent.putExtra("otherWaste", otherWaste);
                            intent.putExtra("weight", weight);
                            intent.putExtra("address", address);
                            intent.putExtra("phone", phone);
                            intent.putExtra("date", date);
                            intent.putExtra("imageUrl", imageUrl);
                            startActivity(intent);
                        });
                    }).addOnFailureListener(e -> {
                        Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show();
                    });
                } else {
                    Toast.makeText(this, "Please add a photo", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getWasteType() {
        int selectedId = radioGroupWasteType.getCheckedRadioButtonId();
        if (selectedId == R.id.radioPlastic) return "Plastic";
        if (selectedId == R.id.radioPolythene) return "Polythene";
        if (selectedId == R.id.radioCardboard) return "Cardboard";
        return "";
    }

    private void openImagePicker() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Add Photo");
        builder.setItems(options, (dialog, item) -> {
            if (options[item].equals("Take Photo")) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    takePhotoLauncher.launch(takePictureIntent);
                }
            } else if (options[item].equals("Choose from Gallery")) {
                Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickImageLauncher.launch(pickPhotoIntent);
            } else if (options[item].equals("Cancel")) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
