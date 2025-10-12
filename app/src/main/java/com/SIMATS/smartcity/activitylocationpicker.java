package com.SIMATS.smartcity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class activitylocationpicker extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ImageButton btnClose;
    private FusedLocationProviderClient fusedLocationClient;
    private TextView tvCurrentAddress;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_picker);
        overridePendingTransition(R.anim.slide_in_up, R.anim.stay_still);

        tvCurrentAddress = findViewById(R.id.tv_current_address);
        btnClose = findViewById(R.id.btn_close);
        MaterialButton btnConfirmLocation = findViewById(R.id.btn_confirm_location);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        ScrollView scrollView = findViewById(R.id.scrollView);
        scrollView.setOnTouchListener((v, event) -> true); // consume all touch events, disables scrolling
        btnClose.setOnClickListener(v -> finish());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_picker);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnConfirmLocation.setOnClickListener(v -> {
            // Get the address from the TextView and send it back
            String selectedAddress = tvCurrentAddress.getText().toString();
            if (!selectedAddress.isEmpty() && !selectedAddress.equals("Fetching address...")) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selected_location_address", selectedAddress);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Please wait for address to load", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Move camera to a default location first
        LatLng defaultLocation = new LatLng(13.0827, 80.2707); // Chennai
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f));

        checkLocationPermissionAndFetch();

        // Add a listener for when the map stops moving
        mMap.setOnCameraIdleListener(() -> {
            LatLng center = mMap.getCameraPosition().target;
            getAddressFromLatLng(center);
        });
    }

    private void getAddressFromLatLng(LatLng latLng) {
        tvCurrentAddress.setText("Fetching address...");
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                String address = addresses.get(0).getAddressLine(0);
                tvCurrentAddress.setText(address);
            } else {
                tvCurrentAddress.setText("Address not found");
            }
        } catch (IOException e) {
            Log.e("LocationPicker", "Geocoder service not available", e);
            tvCurrentAddress.setText("Could not fetch address");
        }
    }

    private void checkLocationPermissionAndFetch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fetchCurrentUserLocation();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void fetchCurrentUserLocation() {
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f));
                } else {
                    Toast.makeText(this, "Could not get current location, using default.", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (SecurityException e) {
            Log.e("LocationPicker", "Location permission error", e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchCurrentUserLocation();
            } else {
                Toast.makeText(this, "Permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.stay_still, R.anim.slide_out_down);
    }
}
