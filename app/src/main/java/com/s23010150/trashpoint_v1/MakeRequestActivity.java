package com.s23010150.trashpoint_v1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import androidx.core.content.FileProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;

public class MakeRequestActivity extends AppCompatActivity {
    private EditText editTextOtherWaste, editTextAddress, editTextPhone, editTextDate, editTextWeight;
    private RadioGroup radioGroupWasteType;
    private Button buttonProceed, buttonAddPhoto;
    private ImageView imageViewPhoto;
    private Uri imageUri;
    private Uri cameraImageUri;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private StorageReference storageRef;

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
                    if (cameraImageUri != null) {
                        imageUri = cameraImageUri;
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
            if (!checkPermissions()) {
                requestPermissions();
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
                try {
                    File photoFile = createImageFile();
                    cameraImageUri = FileProvider.getUriForFile(
                            this,
                            getPackageName() + ".fileprovider",
                            photoFile
                    );
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
                    takePhotoLauncher.launch(takePictureIntent);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to create file", Toast.LENGTH_SHORT).show();
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

    private File createImageFile() throws IOException {
        String imageFileName = "JPEG_" + System.currentTimeMillis();
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_MEDIA_IMAGES
            }, 100);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, 100);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            boolean granted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }
            if (granted) {
                openImagePicker();
            } else {
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
