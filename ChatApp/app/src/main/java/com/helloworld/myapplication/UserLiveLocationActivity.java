package com.helloworld.myapplication;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.core.Constants;

public class UserLiveLocationActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final String KEY_LOCATION = "location";
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private boolean locationPermissionGranted;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private CameraPosition cameraPosition;
    private static final String TAG = "okay";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        if(getIntent()!=null && getIntent().getExtras()!=null && getIntent().getExtras().get("chatMessageDetails")!=null){
            setContentView(R.layout.activity_user_live_location);
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

            // Prompt the user for permission.
            getLocationPermission();
            // [END_EXCLUDE]

            // Turn on the My Location layer and the related control on the map.
            updateLocationUI();

            ChatMessageDetails chatMessageDetails = (ChatMessageDetails) getIntent().getExtras().get("chatMessageDetails");

            TextView tv = findViewById(R.id.textViewUserLocation);
            tv.setText("This is "+chatMessageDetails.firstname+"'s location at "+chatMessageDetails.date);
        }
    }

    /**
     * Prompts the user for permission to use the device location.
     */
    // [START maps_current_place_location_permission]
    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: called in rideronrid activty");
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
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    // [START maps_current_place_update_location_ui]
    private void updateLocationUI() {
        Log.d(TAG, "updateLocationUI: called in rideronrid activty");
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
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

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

        if(getIntent()!=null && getIntent().getExtras()!=null && getIntent().getExtras().get("chatMessageDetails")!=null){
            ChatMessageDetails chatMessageDetails = (ChatMessageDetails) getIntent().getExtras().get("chatMessageDetails");

            if(chatMessageDetails.userCurrentLocation!=null){
                LatLng latLng = new LatLng(chatMessageDetails.userCurrentLocation.latitude,chatMessageDetails.userCurrentLocation.longitude);
                MarkerOptions marker=new MarkerOptions()
                        .position(latLng)
                        .title(chatMessageDetails.firstname);
                latlngBuilder.include(marker.getPosition());
                mMap.addMarker(marker);


                final LatLngBounds latLngBounds = latlngBuilder.build();

                mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                        Log.d(TAG, "Map loaded");
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
                    }
                });
            }
            else{
                Toast.makeText(this, "No user location set.", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(this, "No data found for displaying map", Toast.LENGTH_SHORT).show();
        }
    }
}