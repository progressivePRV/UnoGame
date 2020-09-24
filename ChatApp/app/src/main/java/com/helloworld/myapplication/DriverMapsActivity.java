package com.helloworld.myapplication;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.api.Context;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firestore.v1.WriteResult;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;
import com.squareup.okhttp.OkHttpClient;

//import com.google.maps.DirectionsApi;
//import com.google.maps.DirectionsApiRequest;
//import com.google.maps.GeoApiContext;
//import com.google.maps.model.DirectionsLeg;
//import com.google.maps.model.DirectionsResult;
//import com.google.maps.model.DirectionsRoute;
//import com.google.maps.model.DirectionsStep;
//import com.google.maps.model.EncodedPolyline;

import java.sql.Driver;
import java.util.ArrayList;
import java.util.List;

import static android.provider.SettingsSlicesContract.KEY_LOCATION;

public class DriverMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String cTAG = "okay";
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
    private String chatRoomName;
    private UserProfile userProfile;
    private RequestedRides updateRides;
    boolean isYesClicked;
    TextView riderName;
    TextView toLocation;
    TextView fromLocation;
    ImageView imageViewRider;
    ImageView imageViewpickUpLocation;
    ImageView imageViewDropOffLocation;
    OkHttpClient client;
    RequestedRides requestedRides;
    LatLngBounds.Builder latlngBuilder;

    @Override
    protected void onResume() {
        Log.d(cTAG, "onResume: in drivermapsActivity");
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        setContentView(R.layout.activity_driver_maps);

        imageViewRider = findViewById(R.id.imageViewRider);
        imageViewpickUpLocation = findViewById(R.id.imageViewstartLocation);
        imageViewDropOffLocation = findViewById(R.id.imageViewdropOffLocation);

        imageViewRider.setImageResource(R.drawable.profileinfouser);
        imageViewpickUpLocation.setImageResource(R.drawable.rec);
        imageViewDropOffLocation.setImageResource(R.drawable.placeholder);

        db = FirebaseFirestore.getInstance();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.driverMap);
        mapFragment.getMapAsync(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        chatRoomName = getIntent().getExtras().getString("chatRoomName");
        userProfile = (UserProfile) getIntent().getExtras().getSerializable("userProfile");

        isYesClicked=false;

        requestedRides = (RequestedRides) getIntent().getExtras().getSerializable("requestedRides");
        riderName=findViewById(R.id.riderName);
        toLocation=findViewById(R.id.toLocation);
        fromLocation=findViewById(R.id.fromLocation);

        riderName.setText(requestedRides.riderName);
        toLocation.setText(requestedRides.toLocation);
        fromLocation.setText(requestedRides.fromLocation);

        latlngBuilder =  new LatLngBounds.Builder();

        // Prompt the user for permission.
        getLocationPermission();
        // [END_EXCLUDE]

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        final DocumentReference docRefDriver = db.collection("ChatRoomList")
                .document(chatRoomName)
                .collection("Requested Rides")
                .document(requestedRides.riderId);

        //if the driver says yes
        findViewById(R.id.buttonDriverYes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isYesClicked=true;
                Toast.makeText(DriverMapsActivity.this, "Sending confirmation. Please wait", Toast.LENGTH_SHORT).show();

                progressDialog = new ProgressDialog(DriverMapsActivity.this);
                progressDialog.setMessage("Waiting for ride confirmation");
                progressDialog.setCancelable(false);
                progressDialog.show();

                //Adding this driver to the driver list

                requestedRides.drivers.add(userProfile);
                requestedRides.rideStatus = "IN_PROGRESS";


                DocumentReference driverReference =  db.collection("ChatRoomList")
                        .document(chatRoomName)
                        .collection("Requested Rides")
                        .document(requestedRides.riderId);

                docRefDriver.addSnapshotListener(DriverMapsActivity.this,new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {

                        //Toast.makeText(DriverMapsActivity.this, snapshot +" "+ error, Toast.LENGTH_SHORT).show();
                        Log.d("demo", snapshot +" "+ error);
                        if (error != null) {
                            progressDialog.dismiss();
                            Log.d("demo:", error+"");
                            Toast.makeText(DriverMapsActivity.this, "Some error occured - Drviermapsactivity ", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (snapshot != null && snapshot.exists()) {
                            updateRides = snapshot.toObject(RequestedRides.class);
                            //showProgressBarDialogWithHandler();
                            if(updateRides.rideStatus.equals("ACCEPTED")){
                                Log.d(cTAG, "onEvent: ride is Accepted");
                                if(updateRides.driverId.equals(userProfile.uid) && (updateRides.driverLocation == null || updateRides.driverLocation.isEmpty())){
                                    getLocationPermission();
                                    try {
                                        // if (locationPermissionGranted) {
                                        Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                                        locationResult.addOnCompleteListener(DriverMapsActivity.this, new OnCompleteListener<Location>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Location> task) {
                                                if (task.isSuccessful()) {
                                                    // Set the map's camera position to the current location of the device.
                                                    lastKnownLocation = task.getResult();
                                                    if (lastKnownLocation != null) {
                                                        Log.d(cTAG, "onComplete: last known location is not null");
                                                        ArrayList<Double> driverLocation = new ArrayList<>();
                                                        driverLocation.add(lastKnownLocation.getLatitude());
                                                        driverLocation.add(lastKnownLocation.getLongitude());

                                                        updateRides.setDriverLocation(driverLocation);
                                                        //updateRides.setDrivers(null);

                                                        docRefDriver.set(updateRides);

                                                    }
                                                } else {
                                                    progressDialog.dismiss();
                                                    Log.d("demo", "Current location is null. Using defaults.");
                                                    Log.e("demo", "Exception: %s", task.getException());
                                                    mMap.getUiSettings().setMyLocationButtonEnabled(false);
                                                }
                                            }
                                        });
//                                                        }
//                                                        else {
//                                                            progressDialog.dismiss();
//                                                            Toast.makeText(DriverMapsActivity.this, "No permission to access maps", Toast.LENGTH_SHORT).show();
//                                                            finish();
//                                                        }
                                    } catch (SecurityException e) {
                                        progressDialog.dismiss();
                                        Log.e("Exception: %s", e.getMessage(), e);
                                    }
                                }else if(updateRides.driverId.equals(userProfile.uid) && updateRides.driverLocation != null && !updateRides.driverLocation.isEmpty()){
                                    progressDialog.dismiss();
                                    Toast.makeText(DriverMapsActivity.this, "You have been selected for this ride.. Wait the intent will come soon", Toast.LENGTH_SHORT).show();
                                    Intent intent= new Intent(DriverMapsActivity.this,OnRideActivity.class);
                                    intent.putExtra("requestedRide",updateRides);
                                    intent.putExtra("chatRoomName",chatRoomName);
                                    startActivityForResult(intent, 4025);
                                    finish();
                                }else{
                                    progressDialog.dismiss();
                                    Toast.makeText(DriverMapsActivity.this, "Sorry, the ride is not available", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }else if(updateRides.rideStatus.equals("CANCELLED")){
                                progressDialog.dismiss();
                                Toast.makeText(DriverMapsActivity.this, "Sorry! Rider has cancelled this ride", Toast.LENGTH_SHORT).show();
                                finish();
                            }


                        } else {
                            Toast.makeText(DriverMapsActivity.this, "Requested ride is empty - Drivermapsactivity", Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            finish();
                        }
                    }
                });

// Atomically add a new region to the "regions" array field.
                       driverReference.update("drivers",
                        FieldValue.arrayUnion(userProfile))
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                //Toast.makeText(DriverMapsActivity.this, "Lets see if it stores without any problem!", Toast.LENGTH_SHORT).show();

                                final DocumentReference docRefDriver = db.collection("ChatRoomList")
                                        .document(chatRoomName)
                                        .collection("Requested Rides")
                                        .document(requestedRides.riderId);


                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.hide();
                        Toast.makeText(DriverMapsActivity.this, "Some error occured. Please try again", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!isYesClicked){
                    Toast.makeText(DriverMapsActivity.this, "Ride rejected", Toast.LENGTH_SHORT).show();
                    //DriverMapsActivity.this.finish();
                    addRejectedRide();
                }
            }
        },20000);

        findViewById(R.id.buttonDriverNo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isYesClicked){
                    Toast.makeText(DriverMapsActivity.this, "Ride rejected", Toast.LENGTH_SHORT).show();
                    //finish();
                    addRejectedRide();
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Toast.makeText(DriverMapsActivity.this, "Ride rejected", Toast.LENGTH_SHORT).show();
        addRejectedRide();
    }

    public void addRejectedRide(){

        DocumentReference rejectReference =  db.collection("ChatRoomList")
                .document(chatRoomName)
                .collection("Requested Rides")
                .document(requestedRides.riderId);

        rejectReference.update("rejectedRides",
                FieldValue.arrayUnion(userProfile.uid))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(DriverMapsActivity.this, "Some error occured. Please try again", Toast.LENGTH_SHORT).show();
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 4025 && resultCode == 5025){
            //Toast.makeText(this, "entering here", Toast.LENGTH_SHORT).show();
            DriverMapsActivity.this.finish();
        }
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
                                        .position( new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()))
                                        .title("Your Location");
                                latlngBuilder.include(marker.getPosition());
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



//        PolylineOptions polylineOptions =  new PolylineOptions();

        RequestedRides requestedRides = (RequestedRides) getIntent().getExtras().getSerializable("requestedRides");
        LatLng fromLatLong = new LatLng(requestedRides.pickUpLocation.get(0), requestedRides.pickUpLocation.get(1));
        latlngBuilder.include(fromLatLong);
        MarkerOptions marker = new MarkerOptions()
                .position(fromLatLong)
                .title("Pick Up Location");
        marker.icon(bitmapDescriptorFromVectorPerson(R.drawable.personrider));
        mMap.addMarker(marker);

        LatLng toLatLng = new LatLng(requestedRides.dropOffLocation.get(0), requestedRides.dropOffLocation.get(1));
        latlngBuilder.include(toLatLng);
        mMap.addMarker(new MarkerOptions()
                .position(toLatLng)
                .title("Drop Location"));

        List<LatLng> path = new ArrayList();

        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(getString(R.string.api_key))
                .build();

        DirectionsApiRequest req=new DirectionsApiRequest(context);
        req.origin(new com.google.maps.model.LatLng(requestedRides.pickUpLocation.get(0),requestedRides.pickUpLocation.get(1)));
        req.destination(new com.google.maps.model.LatLng(requestedRides.dropOffLocation.get(0),requestedRides.dropOffLocation.get(1)));


        req.setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                DirectionsResult res = result;
                Log.d("demo",res.routes+"");
                if (res.routes != null && res.routes.length > 0) {
                    DirectionsRoute route = res.routes[0];

                    if (route.legs !=null) {
                        for(int i=0; i<route.legs.length; i++) {
                            DirectionsLeg leg = route.legs[i];
                            if (leg.steps != null) {
                                for (int j=0; j<leg.steps.length;j++){
                                    DirectionsStep step = leg.steps[j];
                                    if (step.steps != null && step.steps.length >0) {
                                        for (int k=0; k<step.steps.length;k++){
                                            DirectionsStep step1 = step.steps[k];
                                            EncodedPolyline points1 = step1.polyline;
                                            if (points1 != null) {
                                                //Decode polyline and add points to list of route coordinates
                                                List<com.google.maps.model.LatLng> coords1 = points1.decodePath();
                                                for (com.google.maps.model.LatLng coord1 : coords1) {
                                                    path.add(new LatLng(coord1.lat, coord1.lng));
                                                }
                                            }
                                        }
                                    } else {
                                        EncodedPolyline points = step.polyline;
                                        if (points != null) {
                                            //Decode polyline and add points to list of route coordinates
                                            List<com.google.maps.model.LatLng> coords = points.decodePath();
                                            for (com.google.maps.model.LatLng coord : coords) {
                                                path.add(new LatLng(coord.lat, coord.lng));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (path.size() > 0) {
                            PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.BLUE).width(10);
                            mMap.addPolyline(opts);
                        }

                        final LatLngBounds latLngBounds = latlngBuilder.build();

                        //List<LatLng> path = new ArrayList();

                        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                            @Override
                            public void onMapLoaded() {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds,200));
                                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds,200));
                            }
                        });
                    }
                });
            }

            @Override
            public void onFailure(Throwable e) {
                Log.d("demo","Directions API failed"+e.getMessage());
            }
        });

    }
}

//hello this is shehab ...