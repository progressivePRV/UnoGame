package com.helloworld.myapplication;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import android.widget.ImageView;
import android.widget.TextView;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class RiderOnRideActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "okay";
    private GoogleMap mMap;
    private FirebaseFirestore db;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final String KEY_LOCATION = "location";
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private boolean locationPermissionGranted;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastKnownLocation;
    private CameraPosition cameraPosition;
    private static final int DEFAULT_ZOOM = 15;
    private ProgressDialog progressDialog;

    private RequestedRides rider;
    private String chatRoomName;

    ImageView riderImage;
    ImageView pickUpLocationImage;
    ImageView dropOffLocationImage;
    TextView riderName;
    TextView pickUpLocation;
    TextView dropOffLocation;

    //variables for driver location update
    Marker driverMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        if(getIntent()!=null && getIntent().getExtras()!=null && getIntent().getExtras().get("requestedRide")!=null){
            setContentView(R.layout.activity_rider_on_ride);
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

            // Prompt the user for permission.
            getLocationPermission();
            // [END_EXCLUDE]

            // Turn on the My Location layer and the related control on the map.
            updateLocationUI();

            // Get the current location of the device and set the position of the map.
            //getDeviceLocation();

            riderImage=findViewById(R.id.riderImage);
            pickUpLocationImage=findViewById(R.id.pcikUpImage);
            dropOffLocationImage=findViewById(R.id.dropOffImage);

            riderName=findViewById(R.id.textViewDriverName);
            pickUpLocation=findViewById(R.id.textViewPickUpName);
            dropOffLocation=findViewById(R.id.textViewDropOffName);

            riderImage.setImageResource(R.drawable.vehicle);
            pickUpLocationImage.setImageResource(R.drawable.rec);
            dropOffLocationImage.setImageResource(R.drawable.placeholder);

            RequestedRides requestedRides = (RequestedRides) getIntent().getExtras().get("requestedRide");

            riderName.setText(requestedRides.driverName);
            pickUpLocation.setText(requestedRides.fromLocation);
            dropOffLocation.setText(requestedRides.toLocation);
        }

        db = FirebaseFirestore.getInstance();
        rider = (RequestedRides) getIntent().getExtras().getSerializable("requestedRide");
        Log.d("demo",rider.toString());
        chatRoomName = getIntent().getExtras().getString("chatRoomName");
        Log.d("demo", chatRoomName);
        //Adding snapshot listener to the riders and the drivers to go back to the chatroom activity



    }

    /**
     * Prompts the user for permission to use the device location.
     */
    // [START maps_current_place_location_permission]
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    // [START maps_current_place_get_device_location]
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {


                                MarkerOptions marker = new MarkerOptions()
                                        .position(new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()))
                                        .title("Your Location");
                                marker.icon(bitmapDescriptorFromVector(R.drawable.car));
                                mMap.addMarker(marker);
                            }
                        } else {
                            Log.d("demo", "Current location is null. Using defaults.");
                            Log.e("demo", "Exception: %s", task.getException());
//                            mMap.moveCamera(CameraUpdateFactory
//                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    //For changing the vector asset to bitmap
    private BitmapDescriptor bitmapDescriptorFromVector(@DrawableRes int vectorDrawableResourceId) {
        Drawable background = ContextCompat.getDrawable(this, R.drawable.ic_baseline_directions_car_24);
        background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
        Drawable vectorDrawable = ContextCompat.getDrawable(this, vectorDrawableResourceId);
        vectorDrawable.setBounds(40, 20, vectorDrawable.getIntrinsicWidth() + 40, vectorDrawable.getIntrinsicHeight() + 20);
        Bitmap bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(), background.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private BitmapDescriptor bitmapDescriptorFromVectorPerson(@DrawableRes int vectorDrawableResourceId) {
        Drawable background = ContextCompat.getDrawable(this, R.drawable.ic_baseline_person_pin_circle_24);
        background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
        Drawable vectorDrawable = ContextCompat.getDrawable(this, vectorDrawableResourceId);
        vectorDrawable.setBounds(40, 20, vectorDrawable.getIntrinsicWidth() + 40, vectorDrawable.getIntrinsicHeight() + 20);
        Bitmap bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(), background.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    // [START maps_current_place_update_location_ui]
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }
    // [END maps_current_place_update_location_ui]


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLngBounds.Builder latlngBuilder = new LatLngBounds.Builder();

//        PolylineOptions polylineOptions =  new PolylineOptions();

        RequestedRides requestedRides = (RequestedRides) getIntent().getExtras().getSerializable("requestedRide");

        LatLng fromLatLong = new LatLng(requestedRides.pickUpLocation.get(0), requestedRides.pickUpLocation.get(1));
        //latlngBuilder.include(fromLatLong);
        MarkerOptions marker = new MarkerOptions()
                .position(fromLatLong)
                .title("Pick Up Location");
        latlngBuilder.include(marker.getPosition());
        marker.icon(bitmapDescriptorFromVectorPerson(R.drawable.personrider));
        mMap.addMarker(marker);

        LatLng toLatLng = new LatLng(requestedRides.dropOffLocation.get(0), requestedRides.dropOffLocation.get(1));
        //latlngBuilder.include(toLatLng);
        MarkerOptions marker2=new MarkerOptions()
                .position(toLatLng)
                .title("Drop Location");
        latlngBuilder.include(marker2.getPosition());
        mMap.addMarker(marker2);

        LatLng driverLatLng = new LatLng(requestedRides.driverLocation.get(0),requestedRides.driverLocation.get(1));
        MarkerOptions markerDriver = new MarkerOptions()
                .position(driverLatLng)
                .title("Driver Location");
        latlngBuilder.include(markerDriver.getPosition());
        markerDriver.icon(bitmapDescriptorFromVector(R.drawable.car));
        driverMarker = mMap.addMarker(markerDriver);

//        Polyline polyline = mMap.addPolyline(polylineOptions);

        final LatLngBounds latLngBounds = latlngBuilder.build();

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 200));
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 200));
                //set animator for dirver location
                SetAnimatoinForDriver(driverMarker);
                // rider will start listnering fro update when map is fully loaded
                RequestRideUpdateListner();
            }
        });
    }


//    /**
//     * Manipulates the map once available.
//     * This callback is triggered when the map is ready to be used.
//     * This is where we can add markers or lines, add listeners or move the camera. In this case,
//     * we just add a marker near Sydney, Australia.
//     * If Google Play services is not installed on the device, the user will be prompted to install
//     * it inside the SupportMapFragment. This method will only be triggered once the user has
//     * installed Google Play services and returned to the app.
//     */
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        mMap = googleMap;
//
//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
//    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder1 = new AlertDialog.Builder(RiderOnRideActivity.this);
        builder1.setMessage("Are you sure you want to cancel this ride?");
        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        db.collection("ChatRoomList").document(chatRoomName)
                                .collection("Requested Rides")
                                .document(rider.riderId)
                                .update("rideStatus","CANCELLED")
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(RiderOnRideActivity.this, "Ride is cancelled. Going back to the chatroom activity", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(RiderOnRideActivity.this, "Some error occured. Please try again", Toast.LENGTH_SHORT).show();
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

    //below function are added for driver location updates

    void RequestRideUpdateListner(){
        DocumentReference docRiderDriverRef = db.collection("ChatRoomList")
                .document(chatRoomName)
                .collection("Requested Rides")
                .document(rider.riderId);

        docRiderDriverRef.addSnapshotListener(RiderOnRideActivity.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.d("demo:", error+"");
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    RequestedRides updated =  snapshot.toObject(RequestedRides.class);
                    // acceptedDrivers = new ArrayList<>();
                    //as this listen for changes it will update driver location
                    UpdatemarkerLocation(updated);
                    Log.d(TAG, "onEvent: updates in ride request listened in RiderOnRide");
                    if(updated.rideStatus.equals("CANCELLED")){
                        //Then either the rider or the driver has cancelled it. so finishing this intent.
                        Toast.makeText(RiderOnRideActivity.this, "This ride has been cancelled", Toast.LENGTH_LONG).show();
                        DeleteRequestRide(updated);
                        finish();
                    }else if(updated.rideStatus.equals("COMPLETED")){
                        //Please write code for what should be implemented if the ride status is completed.
                        Log.d(TAG, "onEvent: rider detected ride status completed");
                        //on complete add data to previous ride
                        //showProgressBarDialog();
                        AddDataToPreviousRide(updated);
                        DeleteRequestRide(updated);
                        finish();
                    }
                } else {
                    System.out.print("Current data: null");
                }
            }
        });
    }

    void SetAnimatoinForDriver(Marker driverMarker){
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = mMap.getProjection();
        Point startPoint = proj.toScreenLocation(driverMarker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 20000;

        final Interpolator interpolator = new LinearInterpolator();

        double lng = startLatLng.longitude;  //request.driverLocation.get(1);
        double lat = startLatLng.latitude;  //request.driverLocation.get(0);
        driverMarker.setPosition(new LatLng(lat, lng));

    }

    void UpdatemarkerLocation(RequestedRides request){
        double lng = request.driverLocation.get(1);
        double lat = request.driverLocation.get(0);
        driverMarker.setPosition(new LatLng(lat, lng));
    }

    public void showProgressBarDialog()
    {
        ProgressDialog progressDialog = new ProgressDialog(RiderOnRideActivity.this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    //adding function to add data in previous rides
    void AddDataToPreviousRide(RequestedRides request){
        PreviousRide ride = new PreviousRide();
        ride.driverID = request.driverId;
        ride.riderID = request.riderId;
        ride.fromLocation = request.fromLocation;
        ride.toLocation = request.toLocation;
        ride.dateAndTime = new Date();
        String docName = ride.riderID + ride.dateAndTime.getTime();

        DocumentReference documentReference = db.collection("Users")
                .document(ride.riderID)
                .collection("Previous Rides")
                .document(docName);
        documentReference.set(ride).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(RiderOnRideActivity.this, "added this ride info to previous ride", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onComplete: wrote into previous ride in riderOnrideActivty");
                }else{
                    Log.d(TAG, "onComplete: some error occured while adding data to previou ride in RiderOnride activity");
                }
            }
        });
    }
    //adding function to delete request ride
    void DeleteRequestRide(RequestedRides request){
        db.collection("ChatRoomList")
                .document(chatRoomName)
                .collection("Requested Rides")
                .document(request.riderId)
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
    }
}