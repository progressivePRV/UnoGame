package com.helloworld.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class showProfile extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_profile);

        Toolbar t = findViewById(R.id.toolbar_for_sidebar);
        t.setTitleTextColor(Color.WHITE);
        setSupportActionBar(t);
        setTitle("User Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        final TextView textViewFirstName = findViewById(R.id.textViewFirstName);
        final TextView textViewLastName = findViewById(R.id.textViewLastName);
        final TextView textViewGender = findViewById(R.id.textViewGender);
        final TextView textViewEmail = findViewById(R.id.textViewEmail);
        final TextView textViewCity = findViewById(R.id.textViewCity);
        final ImageView profileImage = findViewById(R.id.imageViewProfileImage);

        UserProfile u = (UserProfile) getIntent().getSerializableExtra("user");
        Picasso.get().load(u.profileImage).into(profileImage);
        textViewFirstName.setText(u.firstName);
        textViewLastName.setText(u.lastName);
        textViewGender.setText(u.gender);
        textViewEmail.setText(u.email);
        textViewCity.setText(u.city);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}