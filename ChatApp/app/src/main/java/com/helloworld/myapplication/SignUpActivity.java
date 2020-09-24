package com.helloworld.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;


public class SignUpActivity extends AppCompatActivity {
    private static final String TAG = "okay";
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private ImageView imageView;
    private DatabaseReference mDatabase;
    FirebaseStorage storage;
    String username ="";
    StorageReference storageReference;
    boolean isProfileImageSet=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        Toolbar t = findViewById(R.id.toolbar_for_sidebar);
        t.setTitleTextColor(Color.WHITE);
        setSupportActionBar(t);
        setTitle("Sign Up");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //For profile pic selection either from Camera or from Gallery
        imageView = findViewById(R.id.imageButton);
        imageView.setImageDrawable(getDrawable(R.drawable.user));

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CharSequence[] options = { "Take Photo", "Choose from Gallery","Cancel" };

                AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
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

        mAuth = FirebaseAuth.getInstance();
        findViewById(R.id.buttonSignupFirst).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getting all the values for signup
                final EditText fname = findViewById(R.id.editTextFname);
                final EditText lname = findViewById(R.id.editTextLname);
                final EditText email = findViewById(R.id.editTextEmail);
                EditText pass = findViewById(R.id.editTextChoosePassword);
                EditText rePass = findViewById(R.id.editTextRepeatPassword);
                final EditText gender = findViewById(R.id.editTextGender);
                final EditText city = findViewById(R.id.editTextCity);
                imageView = findViewById(R.id.imageButton);

                if(checkValidations(fname) && checkValidations(lname) &&
                        checkValidations(email) && checkEmailValidations(email)
                        && checkValidations(pass) && checkValidations(rePass) && checkValidations(gender) && checkValidations(city) && isProfileImageSet) {
                    String password = pass.getText().toString().trim();
                    String repeatPassword = rePass.getText().toString().trim();
                    String gen = gender.getText().toString();
                    if (password.equals(repeatPassword)) {
                        showProgressBarDialog();
                        //saving the authentication information and moving the activity from signup to chatroom.
                        mAuth.createUserWithEmailAndPassword(email.getText().toString().trim(), password)
                                .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            // Sign in success, update UI with the signed-in user's information
                                            //Create signed up user

                                            final FirebaseUser user = mAuth.getCurrentUser();

                                            mDatabase = FirebaseDatabase.getInstance().getReference("users");
                                            HashMap dbUser = new HashMap<>();
                                            dbUser.put("firstName",fname.getText().toString().trim());
                                            dbUser.put("lastName",lname.getText().toString().trim());
                                            dbUser.put("gender",gender.getText().toString().trim());
                                            dbUser.put("email",email.getText().toString().trim());
                                            dbUser.put("city",city.getText().toString().trim());
                                            dbUser.put("profileImage","");
                                            username = fname.getText().toString().trim()+ " " +lname.getText().toString().trim();
                                            //insert user info in database
                                            mDatabase.child(user.getUid()).setValue(dbUser).addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        Log.d("demo", "createUserWithEmail:success");
                                                        //start uploading the image
                                                        if(isProfileImageSet){
                                                            uploadImage();    
                                                        }
                                                        
                                                    }
                                                    else{
                                                        hideProgressBarDialog();
                                                        Toast.makeText(SignUpActivity.this, "Sign Up Failed", Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });

                                            //uptill here

                                        } else {
                                            // If sign in fails, display a message to the user.
                                            Log.w("demo", "createUserWithEmail:failure", task.getException());
                                            Toast.makeText(SignUpActivity.this, "Create user failed!" + task.getException(),
                                                    Toast.LENGTH_SHORT).show();
                                            hideProgressBarDialog();
                                        }
                                    }
                                });
                    } else {
                        Toast.makeText(SignUpActivity.this, "Passwords and Repeat Passwords are not matching", Toast.LENGTH_SHORT).show();
                    }
                    //hideProgressBarDialog();
                }
                else if(!isProfileImageSet){
                    Toast.makeText(SignUpActivity.this, "Please select a profile image", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Cancel Button
        //Actions: If cancel is pressed the signup screen finishes and goes to the login screen
        findViewById(R.id.buttonCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(SignUpActivity.this,MainActivity.class);
                startActivity(loginIntent);
            }
        });

    }

    //OnActivityResult
    //Actions: From Signup it would directly go to chat room and if the user clicks on signout from the chatroom,
    //the login screen should be visible to the user instead of signup. So OnActivityResult is finish is called here.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    if (resultCode == RESULT_OK && data != null) {
                        Bitmap selectedImage = (Bitmap) data.getExtras().get("data");
                        imageView.setImageBitmap(selectedImage);
                        isProfileImageSet=true;
                    }

                    break;
                case 1:
                    if (resultCode == RESULT_OK && data != null) {
                        try {
                            Uri imageUri = data.getData();
                            final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                            final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                            imageView.setImageBitmap(selectedImage);
                            imageView.setTag("New Image");
                            isProfileImageSet=true;
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            Toast.makeText(SignUpActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                        }
                    }
                    break;
            }
        }

        if(requestCode == 1000 && resultCode == 2000){
            finish();
        }
    }

    //for showing the progress dialog
    public void showProgressBarDialog()
    {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating Profile...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    //for hiding the progress dialog
    public void hideProgressBarDialog()
    {
        progressDialog.dismiss();
    }

    //Regex for checking email validations
    public boolean checkEmailValidations(EditText editText)
    {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        if(!editText.getText().toString().trim().matches(emailPattern))
        {
            editText.setError("Invalid Email");
            return false;
        }
        else
        {
            return true;
        }
    }

    //For checking the empty strings
    public boolean checkValidations(EditText editText){
        if(editText.getText().toString().trim().equals("")){
            editText.setError("Cannot be empty");
            return false;
        }else{
            return true;
        }
    }

    public void uploadImage(){
        //store profile image of firebase file storage
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        final StorageReference profileImageRef = storageReference.child("images/"+mAuth.getUid());

        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
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

                        //call setDownloadable url in realtime database
                        setDownlaodUrl(uri.toString());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: can't get download url");
                        Toast.makeText(SignUpActivity.this, "Profile picture could not be set, you can try it in in edit profile", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: can't upload the image");
                Toast.makeText(SignUpActivity.this, "Profile picture could not be set, you can try it in in edit profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setDownlaodUrl(final String url){
                mDatabase.child(mAuth.getUid()).child("profileImage").setValue(url).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "onComplete: done writing image url to realtime database");
                        }else{
                            Log.d(TAG, "onComplete: cannot write realtime data base for image url");
                        }
                        Toast.makeText(SignUpActivity.this, "Signed Up Sucessfully!", Toast.LENGTH_LONG).show();
                        hideProgressBarDialog();
                        Intent intent = new Intent(SignUpActivity.this,SidebarActivity.class);
                        //sending userid to the next activity and based on user id we can fetch the data from the firebase.
                        intent.putExtra("user",mAuth.getUid());

                        Log.d(TAG, "onComplete: before start intent");
                        startActivityForResult(intent, 1000);
                        Log.d(TAG, "onComplete: after start intent");
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent loginIntent = new Intent(SignUpActivity.this,MainActivity.class);
        startActivity(loginIntent);
        super.onBackPressed();
    }
}