package com.helloworld.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class SidebarActivity extends AppCompatActivity implements rvAdapterForUsersList.ToInteractWithUserListFrag {

    private static final String TAG = "okay";
    private AppBarConfiguration configuration;
    private NavController navController;
    String username,url;
    ImageView profileImage;
    TextView tv_username;
    DatabaseReference mDatabase;
    public UserProfile u;
    ProgressDialog progressDialog;
    String uid;
    Fragment frag_c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sidebar);

        Toolbar t = findViewById(R.id.toolbar_for_sidebar);
        setSupportActionBar(t);
        showProgressBarDialog();

        frag_c = new frag_chatrooms();

        DrawerLayout drawerLayout = findViewById(R.id.drawer_for_sidebar);
        NavigationView navigationView = findViewById(R.id.nav_host);
        configuration = new AppBarConfiguration.Builder(
                R.id.nav_chatrooms,R.id.nav_users,R.id.nav_myprofile,R.id.nav_previous_ride,R.id.nav_logout)
                .setDrawerLayout(drawerLayout)
                .build();

        navController = Navigation.findNavController(this,R.id.frag_container);
        NavigationUI.setupActionBarWithNavController(this,navController,configuration);
        NavigationUI.setupWithNavController(navigationView,navController);


        profileImage = navigationView.getHeaderView(0).findViewById(R.id.nav_header_image);
        tv_username = navigationView.getHeaderView(0).findViewById(R.id.nav_header_text);
        uid = getIntent().getStringExtra("user");
        getCurrentUserData(uid);
    }

//    @Override
//    public void onBackPressed() {
//        navController.popBackStack();
//        super.onBackPressed();
//    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, configuration)
                || super.onSupportNavigateUp();
    }



    void getCurrentUserData(final String uid){

        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        mDatabase.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //User u = snapshot.getValue(User.class);
                u = snapshot.getValue(UserProfile.class);
                u.uid = uid;
                Picasso.get().load(u.profileImage).into(profileImage);
                username = u.firstName +" " +u.lastName;
                tv_username.setText(username);
                hideProgressBarDialog();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public void seeUserProfile(UserProfile u) {
        Intent i = new Intent(this,showProfile.class);
        i.putExtra("user",u);
        startActivity(i);
    }

    //for showing the progress dialog
    public void showProgressBarDialog()
    {
        progressDialog = new ProgressDialog(SidebarActivity.this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    //for hiding the progress dialog
    public void hideProgressBarDialog()
    {
        progressDialog.dismiss();
    }
}