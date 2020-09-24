package com.helloworld.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "okay";
    EditText editTextFirstName;
    EditText editTextLastName;
    EditText editTextGender;
    EditText editTextCity;
    FirebaseStorage storage;
    StorageReference storageReference;
    DatabaseReference mDatabase;
    ImageView profileImage;
    UserProfile user;
    private ProgressDialog progressDialog;
    private boolean isProfileImageSet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Toolbar t = findViewById(R.id.toolbar_for_sidebar);
        t.setTitleTextColor(Color.WHITE);
        setSupportActionBar(t);
        setTitle("Edit Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        showProgressBarDialog("Loading...");
        user = (UserProfile) getIntent().getExtras().get("user");
        editTextFirstName = findViewById(R.id.editFirstName);
        editTextLastName = findViewById(R.id.editlastName);
        editTextGender = findViewById(R.id.editGender);
        editTextCity = findViewById(R.id.editCity);
        profileImage = findViewById(R.id.editProfileImage);


        Picasso.get().load(user.profileImage).into(profileImage);
        editTextFirstName.setText(user.firstName);
        editTextLastName.setText(user.lastName);
        editTextGender.setText(user.gender);
        editTextCity.setText(user.city);
        hideProgressBarDialog();

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        final StorageReference profileImageRef = storageReference.child(user.profileImage);


        findViewById(R.id.editButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkValidations(editTextFirstName) && checkValidations(editTextLastName) && checkValidations(editTextGender) && checkValidations(editTextCity)) {
                    showProgressBarDialog("Updating...");
                    HashMap<String, Object> editUser = new HashMap<>();
                    editUser.put("firstName", editTextFirstName.getText().toString());
                    editUser.put("lastName", editTextLastName.getText().toString());
                    editUser.put("gender", editTextGender.getText().toString());
                    editUser.put("email", user.email);
                    editUser.put("city", editTextCity.getText().toString());
                    editUser.put("profileImage", user.profileImage);

                    mDatabase = FirebaseDatabase.getInstance().getReference("users");

                    mDatabase.child(user.uid).updateChildren(editUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                if (isProfileImageSet){
                                    callUpdateProfileImage();
                                }else{
                                    finish();
                                }
                                Toast.makeText(EditProfileActivity.this, "Profile values edited successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.d("demo", "Profile could not be updated");
                                Toast.makeText(EditProfileActivity.this, "Failed to edit profile", Toast.LENGTH_SHORT).show();
                                hideProgressBarDialog();
                            }
                        }
                    });
                }
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };

                AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileActivity.this);
                builder.setTitle("Choose your profile picture");

                builder.setItems(options, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int item) {

                        if (options[item].equals("Take Photo")) {
                            Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(takePicture, 0);

                        } else if (options[item].equals("Choose from Gallery")) {
                            //trying to get the permission for the profile picture. but it is not happening.
                            Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(pickPhoto , 1);
                        } else if (options[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
            }
        });

        findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {
                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                        profileImage.setImageBitmap(selectedImage);
                        isProfileImageSet = true;
                    }

                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        try {
                            Uri imageUri = data.getData();
                            final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                            final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                            profileImage.setImageBitmap(selectedImage);
                            profileImage.setTag("New Image");
                            isProfileImageSet = true;
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            Toast.makeText(EditProfileActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                        }
                    }
                    break;
            }
        }
    }

    public void callUpdateProfileImage(){
        //store profile image of firebase file storage
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        final StorageReference profileImageRef = storageReference.child("images/"+user.uid);

        profileImage.setDrawingCacheEnabled(true);
        profileImage.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) profileImage.getDrawable()).getBitmap();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        byte[] imageData = outputStream.toByteArray();

        profileImageRef.putBytes(imageData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d(TAG, "onSuccess: image upload successful");


                //call to get  downloadable url
                profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d(TAG, "onSuccess: got downlad url after upadting profile image in edit profile");
                        //call setDownloadable url in realtime database
                        setDownlaodUrl(uri.toString());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: can't get download url");
                        Toast.makeText(EditProfileActivity.this, "Profile picture could not be updated", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: can't upload the image");
                Toast.makeText(EditProfileActivity.this, "Profile picture could not be updated", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setDownlaodUrl(final String url){
        mDatabase.child(user.uid).child("profileImage").setValue(url).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.d(TAG, "onComplete: done writing image url to realtime database");
                }else{
                    Log.d(TAG, "onComplete: cannot write realtime data base for image url");
                }
                Toast.makeText(EditProfileActivity.this, "Edit Profile picture Successful!", Toast.LENGTH_LONG).show();
                //hideProgressBarDialog();
                finish();
            }
        });
    }

    //for showing the progress dialog
    public void showProgressBarDialog(String message) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    //for hiding the progress dialog
    public void hideProgressBarDialog() {
        progressDialog.dismiss();
    }

    //For checking the empty strings
    public boolean checkValidations(EditText editText) {
        if (editText.getText().toString().trim().equals("")) {
            editText.setError("Cannot be empty");
            return false;
        } else {
            return true;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}