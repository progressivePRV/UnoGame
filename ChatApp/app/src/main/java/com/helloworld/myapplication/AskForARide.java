package com.helloworld.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.annotations.NotNull;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
import java.util.Arrays;

public class AskForARide extends AppCompatActivity {

    private static final String TAG = "okay";
    UserProfile user;
    //TextView userName;
    FirebaseAuth mAuth;
    private FirebaseFirestore db;
    TextInputEditText toLocatioin, fromLocation;
    MaterialButton sendRideRequest;
    //TextInputLayout textInputTo,textInputFrom;
    private EditText etPlaceFrom;
    private EditText efPlaceTo;
    private ArrayList<Double> fromLatLong = new ArrayList<>();
    private ArrayList<Double> toLatLong = new ArrayList<>();
    private ProgressDialog progressDialog;
    private String chatRoomName;
    ArrayList<UserProfile> acceptedDrivers = new ArrayList<>();
    private RequestedRides updateRideDetails;
    ImageView fromImage;
    ImageView toImage;
    ImageView moreImage;
    int cnt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ask_for_a_ride);

        Toolbar t = findViewById(R.id.toolbar_for_sidebar);
        t.setTitleTextColor(Color.WHITE);
        setSupportActionBar(t);
        setTitle("Request a ride");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        user = (UserProfile) getIntent().getSerializableExtra("user");
        //toLocatioin = findViewById(R.id.to_tiet_main);
        //fromLocation = findViewById(R.id.from_tiet_main);
        //userName = findViewById(R.id.user_name_in_AskForaRide);
        fromImage=findViewById(R.id.imageViewFrom);
        toImage=findViewById(R.id.imageViewTo);
        moreImage=findViewById(R.id.imageViewMore);

        fromImage.setImageResource(R.drawable.rec);
        toImage.setImageResource(R.drawable.placeholder);
        moreImage.setImageResource(R.drawable.more);

        sendRideRequest = findViewById(R.id.send_ride_request);
        //textInputTo = findViewById(R.id.til_for_to_location);
        //textInputFrom = findViewById(R.id.til_for_from_location);
        chatRoomName = getIntent().getExtras().getString("chatRoomName");
        //userName.setText(user.firstName+ " " +user.lastName);
        mAuth=FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        sendRideRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              if(checkValidations(etPlaceFrom) && checkValidations(efPlaceTo)) {
                  RequestedRides requestedRides = new RequestedRides();
                  requestedRides.riderId = mAuth.getUid();
                  requestedRides.riderName = user.firstName+" "+user.lastName;
                  requestedRides.dropOffLocation = toLatLong;
                  requestedRides.pickUpLocation = fromLatLong;
                  requestedRides.rideStatus = "REQUESTED";
                  requestedRides.toLocation=efPlaceTo.getText().toString();
                  requestedRides.fromLocation=etPlaceFrom.getText().toString();

                  db.collection("ChatRoomList")
                          .document(getIntent().getExtras().getString("chatRoomName"))
                          .collection("Requested Rides")
                          .document(mAuth.getUid())
                          .set(requestedRides)
                          .addOnCompleteListener(new OnCompleteListener<Void>() {
                              @Override
                              public void onComplete(@NonNull Task<Void> task) {
                                  showProgressBarDialogWithHandler();
                                  Toast.makeText(AskForARide.this, "Fetching drivers", Toast.LENGTH_SHORT).show();
                              }
                          }).addOnFailureListener(new OnFailureListener() {
                      @Override
                      public void onFailure(@NonNull Exception e) {
                          Toast.makeText(AskForARide.this, "Some error occured. Please try again", Toast.LENGTH_SHORT).show();
                      }
                  });
              }
            }
        });

        String apiKey = getString(R.string.api_key);

        /**
         * Initialize Places. For simplicity, the API key is hard-coded. In a production
         * environment we recommend using a secure mechanism to manage API keys.
         */
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }

        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(this);

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment_from = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment_from);

        etPlaceFrom = (EditText) autocompleteFragment_from.getView().findViewById(R.id.places_autocomplete_search_input);
        etPlaceFrom.setHint("Enter From Location");

        

        autocompleteFragment_from.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

        autocompleteFragment_from.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
                fromLatLong.add(place.getLatLng().latitude);
                fromLatLong.add(place.getLatLng().longitude);
                Log.d("demo", place.toString());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment_to = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment_to);

        efPlaceTo = (EditText) autocompleteFragment_to.getView().findViewById(R.id.places_autocomplete_search_input);
        efPlaceTo.setHint("Enter To Location");

        autocompleteFragment_to.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

        autocompleteFragment_to.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
                 toLatLong.add(place.getLatLng().latitude);
                toLatLong.add(place.getLatLng().longitude);
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });


        //setting snapshot listener to the drivers accepted list and adding it in a list to display it in the alert box
        DocumentReference docRef = db.collection("ChatRoomList")
                .document(chatRoomName)
                .collection("Requested Rides")
                .document(user.uid);

        docRef.addSnapshotListener(AskForARide.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.d("demo:", error+"");
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    updateRideDetails = snapshot.toObject(RequestedRides.class);
                   // acceptedDrivers = new ArrayList<>();
                    acceptedDrivers = updateRideDetails.drivers;
                } else {
                    System.out.print("Current data: null");
                }
            }
        });
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        //cnt=0;
        //setting snapshot listener to the drivers accepted list and adding it in a list to display it in the alert box
        DocumentReference docRef = db.collection("ChatRoomList")
                .document(chatRoomName)
                .collection("Requested Rides")
                .document(user.uid);

        docRef.addSnapshotListener(AskForARide.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                //cnt++;
                if (error != null) {
                    Log.d("demo:", error+"");
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    updateRideDetails = snapshot.toObject(RequestedRides.class);
//                    Toast.makeText(AskForARide.this, "Fetching driver location", Toast.LENGTH_SHORT).show();
                    if(updateRideDetails.driverLocation!=null && !updateRideDetails.driverLocation.isEmpty()){
                        Intent intent = new Intent(AskForARide.this,RiderOnRideActivity.class);
                        intent.putExtra("chatRoomName",chatRoomName);
                        intent.putExtra("requestedRide",updateRideDetails);
                        progressDialog.hide();
                        startActivity(intent);
                        finish();
                    }
                } else {
                    System.out.print("Current data: null");
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //here the user has cancelled the ride even after seeing the driver list
        if(requestCode == 150 && resultCode == 250 && data == null){
            //deleting the requested ride here and finishing the activity
            deleteRequestedRide();
        }

        //Here the driver is selected and the rider is being forwarded to the next page!
        if(requestCode == 150 && resultCode == 250 && data!=null){
            UserProfile driverProfile = (UserProfile) data.getExtras().getSerializable("driverProfile");
            db = FirebaseFirestore.getInstance();
            updateRideDetails.driverId = driverProfile.uid;
            updateRideDetails.driverName = driverProfile.firstName +" "+ driverProfile.lastName;
            updateRideDetails.rideStatus = "ACCEPTED";

            db.collection("ChatRoomList")
                    .document(getIntent().getExtras().getString("chatRoomName"))
                    .collection("Requested Rides")
                    .document(updateRideDetails.riderId)
                    .set(updateRideDetails)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                progressDialog = new ProgressDialog(AskForARide.this);
                                progressDialog.setMessage("Fetching driver location...");
                                progressDialog.setCancelable(false);
                                progressDialog.show();
                            }
                            else {
                                Toast.makeText(AskForARide.this, "Some issue occured in ride", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AskForARide.this, "Some error occured. Please try again", Toast.LENGTH_SHORT).show();
                }
            });
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

    //for showing the progress dialog
    public void showProgressBarDialogWithHandler()
    {
        progressDialog = new ProgressDialog(AskForARide.this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        //Handler is set for 30 seconds for the driver to accept the invitation
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                progressDialog.dismiss();
                getToDriverListActivity();
            }
        }, 30000);
    }

//    @Override
//    protected void onResume() {
//        Log.d(TAG, "onResume: in chatroomActivity");
//        super.onResume();
//                ();
//    }

    void getToDriverListActivity(){
        if (acceptedDrivers.size()==0){
            Toast.makeText(this, "Sorry, No Driver is available at this moment", Toast.LENGTH_SHORT).show();
            deleteRequestedRide();
        }else{
            Intent i = new Intent(this, Driver_list.class);
            i.putExtra("drivers",acceptedDrivers);
            i.putExtra("updateRideDetails", updateRideDetails);
            i.putExtra("chatRoomName",chatRoomName);
            startActivityForResult(i, 150);
        }
    }

    public void deleteRequestedRide(){
        db.collection("ChatRoomList")
                .document(chatRoomName)
                .collection("Requested Rides")
                .document(user.uid)
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "onComplete: request ride deleted succefully in rider on ride activtiy");
                        }else{
                            Log.d(TAG, "onComplete: error while deleting the request ride in rider on rid activity");
                        }
                    }
                });
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    //for hiding the progress dialog
    public void hideProgressBarDialog()
    {
        progressDialog.dismiss();
    }
}