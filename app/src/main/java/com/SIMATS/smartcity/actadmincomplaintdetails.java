package com.SIMATS.smartcity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.FileProvider;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class actadmincomplaintdetails extends AppCompatActivity {

    // --- UI Elements ---
    private TextView complaintDetailsText, locationDetailsText, dateTimeDetailsText, complaintidtext;
    private RadioGroup statusRadioGroup;
    private RadioButton pendingRadio, completedRadio, rejectedRadio;
    private MaterialButton updateButton, transferButton;
    private ImageButton closeButton;
    private ImageView image1, image2;
    private ShimmerFrameLayout shimmerLayout;
    private View realContentLayout;

    private String complaintId;
    private Bitmap complaintBitmap1, complaintBitmap2;

    // --- NEW: Flags to ensure minimum display time ---
    private boolean isDataLoaded = false;
    private boolean isMinimumTimeElapsed = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admincomplaintdetails);
        overridePendingTransition(R.anim.slide_in_up, R.anim.stay_still);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        complaintId = getIntent().getStringExtra("complaint_id");

        bindViews();
        complaintidtext.setText(complaintId);
        setupListeners();

        fetchFullComplaintDetails();
    }

    private void bindViews() {
        complaintDetailsText = findViewById(R.id.complaint_details);
        locationDetailsText = findViewById(R.id.location_details);
        complaintidtext = findViewById(R.id.complaint_id);
        dateTimeDetailsText = findViewById(R.id.date_time_details);
        statusRadioGroup = findViewById(R.id.status_radio_group);
        pendingRadio = findViewById(R.id.status_pending);
        completedRadio = findViewById(R.id.status_completed);
        rejectedRadio = findViewById(R.id.status_rejected);
        updateButton = findViewById(R.id.update_complaint);
        closeButton = findViewById(R.id.btn_close);
        image1 = findViewById(R.id.image1);
        image2 = findViewById(R.id.image2);
        transferButton = findViewById(R.id.transfercomplaint);
        shimmerLayout = findViewById(R.id.shimmer_view_container);
        realContentLayout = findViewById(R.id.real_content_layout);
    }

    private void setupListeners() {
        closeButton.setOnClickListener(v -> finish());
        updateButton.setOnClickListener(v -> updateComplaintStatus());
        transferButton.setOnClickListener(v -> transferComplaintByEmail());
    }

    private void fetchFullComplaintDetails() {
        String url = actapiconfig.getPublicAPI() + "admincomplaintdetails.php";
        RequestQueue queue = Volley.newRequestQueue(this);

        // --- NEW: Start a 2-second (2000ms) timer ---
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            isMinimumTimeElapsed = true;
            tryToShowContent(); // Check if content can be shown
        }, 2000);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getBoolean("success")) {
                            parseAndDisplayDetails(obj);
                        } else {
                            Toast.makeText(this, "Failed: " + obj.optString("message", "Unknown error"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error parsing response: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    // --- CHANGED: Set data loaded flag and try to show content ---
                    isDataLoaded = true;
                    tryToShowContent();
                },
                error -> {
                    Toast.makeText(this, "Volley Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    // --- CHANGED: Set data loaded flag and try to show content ---
                    isDataLoaded = true;
                    tryToShowContent();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "get");
                params.put("complaint_id", complaintId);
                return params;
            }
        };
        queue.add(request);
    }

    // --- NEW: Gatekeeper method to check both conditions ---
    private void tryToShowContent() {
        // Only show the content if both data is loaded AND 2 seconds have passed
        if (isDataLoaded && isMinimumTimeElapsed) {
            showContent();
        }
    }

    private void showContent() {
        if (shimmerLayout != null && realContentLayout != null) {
            shimmerLayout.stopShimmer();
            shimmerLayout.setVisibility(View.GONE);
            realContentLayout.setVisibility(View.VISIBLE);
        }
    }

    private void parseAndDisplayDetails(JSONObject obj) {
        if (isFinishing() || isDestroyed()) return;

        complaintDetailsText.setText(obj.optString("issue_details", "No details"));
        locationDetailsText.setText(obj.optString("location", "No location"));
        dateTimeDetailsText.setText(obj.optString("date_time", ""));
        String status = obj.optString("status", "").toLowerCase();
        switch (status) {
            case "completed": completedRadio.setChecked(true); break;
            case "rejected": rejectedRadio.setChecked(true); break;
            default: pendingRadio.setChecked(true);
        }

        String base64Image1 = obj.optString("attachment1_base64");
        String base64Image2 = obj.optString("attachment2_base64");

        if (base64Image1 != null && !base64Image1.isEmpty() && !base64Image1.equals("null")) {
            try {
                byte[] decodedString = Base64.decode(base64Image1, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                Glide.with(this).load(decodedBitmap).into(image1);
                complaintBitmap1 = decodedBitmap;
            } catch (IllegalArgumentException e) {
                Log.e("Base64Decode", "Failed to decode image 1", e);
                image1.setImageResource(R.drawable.users_icon);
            }
        }

        if (base64Image2 != null && !base64Image2.isEmpty() && !base64Image2.equals("null")) {
            try {
                byte[] decodedString = Base64.decode(base64Image2, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                Glide.with(this).load(decodedBitmap).into(image2);
                complaintBitmap2 = decodedBitmap;
            } catch (IllegalArgumentException e) {
                Log.e("Base64Decode", "Failed to decode image 2", e);
                image2.setImageResource(R.drawable.users_icon);
            }
        }
    }

    private void updateComplaintStatus() {
        String newStatus = getSelectedStatus();
        String url = actapiconfig.getPublicAPI() + "admincomplaintdetails.php";
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getBoolean("success")) {
                            Toast.makeText(actadmincomplaintdetails.this, "Status updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Failed: " + obj.optString("message", "Unknown error"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error parsing response: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> Toast.makeText(this, "Volley Error: " + error.getMessage(), Toast.LENGTH_LONG).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "changestatus");
                params.put("complaint_id", complaintId);
                params.put("status", newStatus);
                return params;
            }
        };
        queue.add(request);
    }

    private String getSelectedStatus() {
        int selectedId = statusRadioGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.status_completed) return "Completed";
        if (selectedId == R.id.status_rejected) return "Rejected";
        return "Pending";
    }

    private void transferComplaintByEmail() {
        String emailBody = "Dear Support Team,\n\n" +
                "Please find the details for the following complaint:\n\n" +
                "---------------------------------\n" +
                "Complaint ID: " + complaintidtext.getText().toString() + "\n" +
                "Details: " + complaintDetailsText.getText().toString() + "\n" +
                "Location: " + locationDetailsText.getText().toString() + "\n" +
                "Date/Time: " + dateTimeDetailsText.getText().toString() + "\n" +
                "Current Status: " + getSelectedStatus() + "\n" + "Please Provide the completed image on this below link ASAP: " + "\n" +
                "https://thevishnuteja.github.io/GetContractorImage_SmartCity/" + "\n" +
                "---------------------------------\n";

        ArrayList<Uri> imageUris = new ArrayList<>();

        if (complaintBitmap1 != null) {
            File file1 = saveBitmapToCache(complaintBitmap1, "complaint_image1.png");
            if (file1 != null) {
                Uri uri1 = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file1);
                imageUris.add(uri1);
            }
        }
        if (complaintBitmap2 != null) {
            File file2 = saveBitmapToCache(complaintBitmap2, "complaint_image2.png");
            if (file2 != null) {
                Uri uri2 = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file2);
                imageUris.add(uri2);
            }
        }

        Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"contractor.smartcity@gmail.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Support Request - Complaint ID: #" + complaintId);
        emailIntent.putExtra(Intent.EXTRA_TEXT, emailBody);

        if (!imageUris.isEmpty()) {
            emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
            emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }

        try {
            startActivity(Intent.createChooser(emailIntent, "Send Email"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
        }
    }

    private File saveBitmapToCache(Bitmap bitmap, String fileName) {
        try {
            File cachePath = new File(getCacheDir(), "images");
            cachePath.mkdirs();
            File file = new File(cachePath, fileName);
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();
            return file;
        } catch (IOException e) {
            Log.e("SaveBitmap", "Error saving bitmap to cache", e);
            return null;
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.stay_still, R.anim.slide_out_down);
    }
}