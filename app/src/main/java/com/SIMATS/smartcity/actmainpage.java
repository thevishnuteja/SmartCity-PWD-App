package com.SIMATS.smartcity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.airbnb.lottie.LottieAnimationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class actmainpage extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnPolylineClickListener {

    // UI Elements
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Marker locationMarker, selectedLocationMarker;
    private String selectedPlaceName;
    private boolean isLocationFetched = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private BottomSheetBehavior<NestedScrollView> bottomSheetBehavior;
    private TextView tvScrollUp;
    private CardView btnArrow, option1, option2, option3, option4, option5;
    private ImageView btnArrowIcon, locationPin;
    private boolean isMenuExpanded = false;
    private CardView cardManualComplaint;
    private LinearLayout btnTrack, btnHistory, btnEmergency;
    private CardView btnGo;
    private TextView recentComplaintTextView;
    private final String[] issueDetailsHolder = new String[1];
    private final String[] statusHolder = new String[1];
    private final String[] datetimeHolder = new String[1];
    private final String[] titleHolder = new String[1];
    private final String[] locationHolder = new String[1];
    private final int[] complaintIdHolder = new int[1];

    // --- PWD ROADS: CORRECTED VARIABLES ---
    private RoadsModels.Road currentlySelectedRoad;
    private LinearLayout roadInfoPanel, originalContentWrapper;
    private TextView tvRoadName, tvRoadDetails;
    private ImageView centerPin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainpage);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyAn-5RV7TmZKuQMAFojD0vAzeKzcXXkGgE");
        }

        initMap();
        initUI();
        initButtons();

        SessionManager sessionManager = new SessionManager(getApplicationContext());
        int userId = sessionManager.getUserId();
        if (userId != -1) {
            fetchRecentComplaint(userId);
            checkUserProfileStatus(userId);
        } else {
            Toast.makeText(this, "Invalid user ID", Toast.LENGTH_SHORT).show();
        }
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void initUI() {
        NestedScrollView bottomSheetLayout = findViewById(R.id.bottomSheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);

        tvScrollUp = bottomSheetLayout.findViewById(R.id.tv_scroll_up);
        recentComplaintTextView = bottomSheetLayout.findViewById(R.id.recent_complaint_title);
        cardManualComplaint = bottomSheetLayout.findViewById(R.id.card_manual_complaint);
        btnTrack = bottomSheetLayout.findViewById(R.id.btn_track);
        btnHistory = bottomSheetLayout.findViewById(R.id.btn_history);
        btnEmergency = bottomSheetLayout.findViewById(R.id.btn_emergency);
        CardView searchCard = bottomSheetLayout.findViewById(R.id.search_card);
        // Changed from ImageButton to LottieAnimationView
        LottieAnimationView btnAiChatbot = bottomSheetLayout.findViewById(R.id.btn_ai_chatbot);
        btnAiChatbot.setOnClickListener(v -> startActivityWithAnimation(new Intent(this, GeminiChatActivity.class)));

        btnArrow = findViewById(R.id.btn_arrow);
        btnArrowIcon = findViewById(R.id.btn_arrow_icon);
        option1 = findViewById(R.id.option_1);
        option2 = findViewById(R.id.option_2);
        option3 = findViewById(R.id.option_3);
        option4 = findViewById(R.id.option_4);
        option5 = findViewById(R.id.option_5);
        locationPin = findViewById(R.id.location_pin);
        btnGo = findViewById(R.id.btnGo);

        searchCard.setOnClickListener(v -> openSearchBar());
        btnAiChatbot.setOnClickListener(v -> startActivity(new Intent(this, GeminiChatActivity.class)));

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // --- PWD ROADS: Find UI elements ---
        centerPin = findViewById(R.id.center_pin);
        roadInfoPanel = bottomSheetLayout.findViewById(R.id.road_info_panel);
        tvRoadName = bottomSheetLayout.findViewById(R.id.tv_road_name);
        tvRoadDetails = bottomSheetLayout.findViewById(R.id.tv_road_details);
        originalContentWrapper = bottomSheetLayout.findViewById(R.id.original_content_wrapper);

        setupBottomSheetListener();
    }

    private void initButtons() {
        cardManualComplaint.setOnClickListener(v -> startActivityWithAnimation(new Intent(this, actmannualcomplaint.class)));
        btnTrack.setOnClickListener(v -> startActivityWithAnimation(new Intent(this, acttrackpage.class)));
        btnHistory.setOnClickListener(v -> startActivityWithAnimation(new Intent(this, acthistory.class)));
        btnEmergency.setOnClickListener(v -> startActivityWithAnimation(new Intent(this, actemergencypage.class)));
        btnArrow.setOnClickListener(v -> toggleMenu());
        option1.setOnClickListener(v -> startActivityWithAnimation(new Intent(this, actprofile.class)));
        option2.setOnClickListener(v -> startActivityWithAnimation(new Intent(this, actaboutuspage.class)));
        option3.setOnClickListener(v -> startActivityWithAnimation(new Intent(this, acttermandconditions.class)));
        option4.setOnClickListener(v -> sendSupportEmail());
        option5.setOnClickListener(v -> showLogoutDialog());
        locationPin.setOnClickListener(v -> zoomToLocation());

        // --- PWD ROADS: MODIFIED: btnGo now handles both pinpoint mode and search mode ---
        btnGo.setOnClickListener(v -> {
            if (currentlySelectedRoad != null) {
                LatLng preciseLocation = mMap.getCameraPosition().target;
                Intent intent = new Intent(this, actmannualcomplaint.class);
                intent.putExtra("road_name", currentlySelectedRoad.name);
                intent.putExtra("road_id", currentlySelectedRoad.properties.id);
                intent.putExtra("complaint_latitude", preciseLocation.latitude);
                intent.putExtra("complaint_longitude", preciseLocation.longitude);
                startActivityWithAnimation(intent);

                centerPin.setVisibility(View.GONE);
                btnGo.setVisibility(View.GONE);
                resetBottomSheetUI();
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            else if (selectedPlaceName != null && !selectedPlaceName.isEmpty()) {
                Intent intent = new Intent(this, actmannualcomplaint.class);
                intent.putExtra("place_name", selectedPlaceName);
                startActivityWithAnimation(intent);
            }
            else {
                Toast.makeText(this, "No location selected on map", Toast.LENGTH_SHORT).show();
            }
        });

        // --- PWD ROADS: Listener for the road info panel to enter pinpoint mode ---
        roadInfoPanel.setOnClickListener(v -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            centerPin.setVisibility(View.VISIBLE);
            btnGo.setVisibility(View.VISIBLE);

            if (currentlySelectedRoad != null && !currentlySelectedRoad.path.isEmpty()) {
                RoadsModels.Coordinate firstPoint = currentlySelectedRoad.path.get(0);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(firstPoint.lat, firstPoint.lng), 17f));
            }
            Toast.makeText(this, "Move map to pinpoint issue location", Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        LatLng defaultLocation = new LatLng(17.3850, 78.4867); // Hyderabad
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f));
        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.setOnMapClickListener(latLng -> {
            // --- FLICKER FIX: MODIFIED ---
            // If a road is selected, clicking the map smoothly collapses the sheet.
            // The listener will handle resetting the UI once it's collapsed.
            if (currentlySelectedRoad != null) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                return;
            }

            // Default behavior if no road is selected
            Geocoder geocoder = new Geocoder(actmainpage.this, Locale.getDefault());
            String addressText;
            try {
                List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                addressText = (addresses != null && !addresses.isEmpty()) ? addresses.get(0).getAddressLine(0) : String.format(Locale.US, "Lat: %.4f, Lng: %.4f", latLng.latitude, latLng.longitude);
            } catch (IOException e) {
                addressText = String.format(Locale.US, "Lat: %.4f, Lng: %.4f", latLng.latitude, latLng.longitude);
            }
            selectedPlaceName = addressText;
            if (selectedLocationMarker != null) selectedLocationMarker.remove();
            selectedLocationMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Location").snippet(selectedPlaceName));
            if (selectedLocationMarker != null) selectedLocationMarker.showInfoWindow();
            btnGo.setVisibility(View.VISIBLE);
        });

        fetchLocation();
        loadAndDrawRoads();
        mMap.setOnPolylineClickListener(this);
    }

    @Override
    public void onPolylineClick(@NonNull Polyline polyline) {
        Object tag = polyline.getTag();
        if (tag instanceof RoadsModels.Road) {
            currentlySelectedRoad = (RoadsModels.Road) tag;
            RoadsModels.Properties props = currentlySelectedRoad.properties;

            tvRoadName.setText(currentlySelectedRoad.name);
            String details;
            if ("developed".equals(currentlySelectedRoad.status)) {
                details = "Maintained by: " + props.maintained_by + "\nLast Service: " + props.last_maintained;
            } else {
                details = "Project: " + props.project_details + "\nExpected Completion: " + props.expected_completion;
            }
            tvRoadDetails.setText(details);

            originalContentWrapper.setVisibility(View.GONE);
            roadInfoPanel.setVisibility(View.VISIBLE);

            bottomSheetBehavior.setHideable(true);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    private void resetBottomSheetUI() {
        if (originalContentWrapper != null) {
            roadInfoPanel.setVisibility(View.GONE);
            originalContentWrapper.setVisibility(View.VISIBLE);

            bottomSheetBehavior.setHideable(false);

            int originalPeekHeight = getResources().getDimensionPixelSize(R.dimen.bottom_sheet_peek_height);
            bottomSheetBehavior.setPeekHeight(originalPeekHeight);
        }
        currentlySelectedRoad = null;
    }

    private void setupBottomSheetListener() {
        bottomSheetBehavior.setHideable(false);

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // --- FLICKER FIX: FINAL LOGIC ---
                // This now handles all state transitions smoothly.
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    // If the sheet collapses AND we are currently showing road info,
                    // it means the user swiped down or we commanded it to collapse.
                    // Now is the safe time to reset the UI.
                    if (roadInfoPanel.getVisibility() == View.VISIBLE) {
                        resetBottomSheetUI();
                    }
                } else if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    // If the sheet is hidden (e.g., from pinpoint mode), reset it
                    // and then bring it back to the default collapsed state.
                    resetBottomSheetUI();
                    bottomSheet.post(() -> bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED));
                }

                if(originalContentWrapper.getVisibility() == View.VISIBLE) {
                    if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                        tvScrollUp.setVisibility(View.GONE);
                    } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                        tvScrollUp.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if (originalContentWrapper.getVisibility() == View.VISIBLE) {
                    tvScrollUp.setAlpha(1 - slideOffset);
                } else {
                    tvScrollUp.setAlpha(0);
                }
            }
        });
    }

    private void loadAndDrawRoads() {
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.roads);
            InputStreamReader reader = new InputStreamReader(inputStream);
            RoadsModels.RoadsData roadsData = new Gson().fromJson(reader, RoadsModels.RoadsData.class);
            if (roadsData == null || roadsData.roads == null) return;

            for (RoadsModels.Road road : roadsData.roads) {
                PolylineOptions polylineOptions = new PolylineOptions();
                int color = "developed".equals(road.status)
                        ? Color.parseColor("#FF0099CC")  // Dark Sky Blue
                        : Color.rgb(255, 165, 0);
                polylineOptions.color(color);
                polylineOptions.width(12f);
                polylineOptions.clickable(true);
                for (RoadsModels.Coordinate coordinate : road.path) {
                    polylineOptions.add(new LatLng(coordinate.lat, coordinate.lng));
                }
                Polyline polyline = mMap.addPolyline(polylineOptions);
                polyline.setTag(road);
            }
        } catch (Exception e) {
            Log.e("RoadsFeature", "Error loading or drawing roads", e);
            Toast.makeText(this, "Could not load PWD road data.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (centerPin != null && centerPin.getVisibility() == View.VISIBLE) {
            centerPin.setVisibility(View.GONE);
            btnGo.setVisibility(View.GONE);
            resetBottomSheetUI();
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }

    // --- All your other existing, unchanged methods go below ---

    private BitmapDescriptor getCircularMarkerIcon(int drawableRes, int size) {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) ContextCompat.getDrawable(this, drawableRes);
        if (bitmapDrawable == null) return null;
        Bitmap bitmap = bitmapDrawable.getBitmap();
        bitmap = Bitmap.createScaledBitmap(bitmap, size, size, false);
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, size, size);
        final RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(0xff424242);
        canvas.drawOval(rectF, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return BitmapDescriptorFactory.fromBitmap(output);
    }

    private static final int AUTOCOMPLETE_REQUEST_CODE = 100;

    private void openSearchBar() {
        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fields)
                .build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                LatLng latLng = place.getLatLng();
                if (latLng != null) {
                    selectedPlaceName = place.getName();
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    if (selectedLocationMarker != null) selectedLocationMarker.remove();
                    selectedLocationMarker = mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(selectedPlaceName));
                    if (selectedLocationMarker != null) {
                        selectedLocationMarker.showInfoWindow();
                    }
                    btnGo.setVisibility(View.VISIBLE);
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                if (status != null) {
                    Toast.makeText(this, "Error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void fetchLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        locationPin.setEnabled(false);
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null && mMap != null) {
                isLocationFetched = true;
                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                if (locationMarker != null) locationMarker.remove();
                BitmapDescriptor customIcon = getCircularMarkerIcon(R.drawable.userlocation, 130);
                if (customIcon != null) {
                    locationMarker = mMap.addMarker(new MarkerOptions()
                            .position(userLocation)
                            .title("You are here")
                            .icon(customIcon));
                }
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f));
                locationPin.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(this, "Could not get location. Please ensure GPS is on.", Toast.LENGTH_SHORT).show();
            }
            locationPin.setEnabled(true);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocation();
            } else {
                Toast.makeText(this, "Location permission is required to show your position.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void checkUserProfileStatus(int userId) {
        SessionManager sessionManager = new SessionManager(getApplicationContext());
        if (sessionManager.hasProfilePromptBeenShown()) {
            return;
        }
        String url = actapiconfig.getPublicAPI() + "check_profile_status.php?user_id=" + userId;
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);
                if (jsonObject.has("error")) {
                    Log.e("ProfileCheck", "Error from server: " + jsonObject.getString("error"));
                    return;
                }
                String dob = jsonObject.optString("date_of_birth", "");
                String mobile = jsonObject.optString("mobile_number", "");
                String location = jsonObject.optString("city", "");
                String occupation = jsonObject.optString("occupation", "");

                boolean isProfileIncomplete = dob.isEmpty() || mobile.isEmpty() || location.isEmpty() || occupation.isEmpty();

                if (isProfileIncomplete) {
                    sessionManager.setProfilePromptShown(true);
                    showUpdateProfileDialog();
                }
            } catch (JSONException e) {
                Log.e("ProfileCheckJSON", "Error parsing profile status JSON", e);
            }
        }, error -> {
            Log.e("ProfileCheckVolley", "Network error checking profile status", error);
        });
        Volley.newRequestQueue(this).add(request);
    }

    private void showUpdateProfileDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Update Your Profile")
                .setMessage("We've noticed some of your profile details are missing. Please take a moment to update them for a better experience.")
                .setCancelable(false)
                .setPositiveButton("Update Now", (dialog, which) -> {
                    Intent intent = new Intent(actmainpage.this, actprofileedit.class);
                    startActivity(intent);
                })
                .setNegativeButton("Skip Now", (dialog, which) -> dialog.dismiss())
                .setIcon(R.drawable.logout_icon)
                .show();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    SessionManager sessionManager = new SessionManager(getApplicationContext());
                    sessionManager.logoutUser();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void sendSupportEmail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@smartcity.com"));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Support Request");
        intent.putExtra(Intent.EXTRA_TEXT, "Dear Support Team,\n\n");
        try {
            startActivity(Intent.createChooser(intent, "Send Email"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
        }
    }

    private void startActivityWithAnimation(Intent intent) {
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_up, R.anim.stay_still);
    }

    private void toggleMenu() {
        isMenuExpanded = !isMenuExpanded;
        btnArrowIcon.animate().rotation(isMenuExpanded ? 90f : 0f).setDuration(300).start();
        View[] options = {option1, option2, option3, option4, option5};
        if (isMenuExpanded) {
            for (int i = 0; i < options.length; i++) {
                showMenuOption(options[i], (i + 1) * 50L);
            }
        } else {
            for (int i = 0; i < options.length; i++) {
                hideMenuOption(options[i], (options.length - i) * 50L);
            }
        }
    }

    private void showMenuOption(View view, long startDelay) {
        view.setVisibility(View.VISIBLE);
        view.setAlpha(0f);
        view.animate().alpha(1f).setDuration(300).setStartDelay(startDelay).start();
    }

    private void hideMenuOption(View view, long startDelay) {
        view.animate().alpha(0f).setDuration(300).setStartDelay(startDelay)
                .withEndAction(() -> view.setVisibility(View.GONE)).start();
    }

    private void zoomToLocation() {
        if (isLocationFetched && locationMarker != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationMarker.getPosition(), 16));
            if (selectedLocationMarker != null) {
                selectedLocationMarker.remove();
                selectedLocationMarker = null;
            }
            selectedPlaceName = null;
            btnGo.setVisibility(View.GONE);
        } else {
            Toast.makeText(this, "Location not yet available", Toast.LENGTH_SHORT).show();
            fetchLocation();
        }
    }

    private void fetchRecentComplaint(int userId) {
        String url = actapiconfig.getPublicAPI() + "recent_complaint.php?user_id=" + userId;
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            try {
                if (response.trim().startsWith("[")) {
                    // Handle array logic if needed
                } else {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.has("error") || jsonObject.has("message")) {
                        recentComplaintTextView.setText("No recent complaints found.");
                        return;
                    }
                    complaintIdHolder[0] = jsonObject.getInt("complaint_id");
                    titleHolder[0] = jsonObject.getString("issue_type");
                    issueDetailsHolder[0] = jsonObject.getString("issue_details");
                    statusHolder[0] = jsonObject.getString("status");
                    datetimeHolder[0] = jsonObject.getString("date_time");
                    locationHolder[0] = jsonObject.getString("location");
                    recentComplaintTextView.setText(titleHolder[0]);
                    recentComplaintTextView.setOnClickListener(v -> {
                        Intent intent = new Intent(this, actcomplaintdetails.class);
                        intent.putExtra("complaint_id", String.valueOf(complaintIdHolder[0]));
                        intent.putExtra("complaint_title", titleHolder[0]);
                        intent.putExtra("complaint_details", issueDetailsHolder[0]);
                        intent.putExtra("complaint_status", statusHolder[0]);
                        intent.putExtra("datetime", datetimeHolder[0]);
                        intent.putExtra("location", locationHolder[0]);
                        startActivityWithAnimation(intent);
                    });
                }
            } catch (JSONException e) {
                recentComplaintTextView.setText("Could not load complaint data.");
                Log.e("JSONError", "Error parsing recent complaint JSON: " + response, e);
            }
        }, error -> {
            recentComplaintTextView.setText("Network error. Could not connect.");
            Log.e("VolleyError", "Network error fetching recent complaint", error);
        });
        queue.add(request);
    }
}