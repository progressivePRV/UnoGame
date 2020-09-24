package com.helloworld.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class Driver_list extends AppCompatActivity implements rvAdapterForDriverList.ToInteractWithDriverList {

    ArrayList<UserProfile> drivers = new ArrayList<>();
    RecyclerView rv;
    RecyclerView.Adapter rvAdapter;
    private FirebaseFirestore db;
    RecyclerView.LayoutManager rvLayoutManager;
    private String chatRoomName;
    private RequestedRides rides;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_list);

        Toolbar t = findViewById(R.id.toolbar_for_sidebar);
        t.setTitleTextColor(Color.WHITE);
        setSupportActionBar(t);
        setTitle("List of Drivers");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        db = FirebaseFirestore.getInstance();

        drivers = (ArrayList<UserProfile>) getIntent().getSerializableExtra("drivers");

        rv = findViewById(R.id.rv_in_Driver_list);
        rv.setHasFixedSize(true);
        rvLayoutManager =  new LinearLayoutManager(this);
        rv.setLayoutManager(rvLayoutManager);
        rvAdapter =  new rvAdapterForDriverList(this,drivers);
        rv.setAdapter(rvAdapter);

        chatRoomName = getIntent().getExtras().getString("chatRoomName");
        rides = (RequestedRides) getIntent().getExtras().getSerializable("updateRideDetails");

        findViewById(R.id.buttonCancelRide).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void DriverSelect(UserProfile u) {
        Toast.makeText(this, "driver "+u.firstName+" selected", Toast.LENGTH_SHORT).show();
        Intent data = new Intent();
        data.putExtra("driverProfile", u);
        setResult(250, data);
        finish();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(Driver_list.this);
        builder1.setMessage("Are you sure you want to cancel this ride?");
        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        db.collection("ChatRoomList").document(chatRoomName)
                                .collection("Requested Rides")
                                .document(rides.riderId)
                                .update("rideStatus","CANCELLED")
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(Driver_list.this, "Ride is cancelled. Going back to the chatroom activity", Toast.LENGTH_SHORT).show();
                                        setResult(250, null);
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Driver_list.this, "Some error occured. Please try again", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

        builder1.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert11 = builder1.create();
        alert11.show();

    }

}