package com.SIMATS.smartcity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import com.bumptech.glide.Glide;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.FileProvider;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.shimmer.ShimmerFrameLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class actcomplaintdetails extends AppCompatActivity {

    // --- UI Elements ---
    private ImageButton btnClose, shareBtn;
    private LinearLayout complaintLayout, imagesSection;
    private TextView titleTextView, complaintIdTextView, statusTextView, detailsTextView, locationDetailsTextView, dateTimeTextView, nextStepsTitle, nextStepsText;
    private Button printButton, deleteBtn;
    private ImageView userImageView, completedImageView;
    private String complaintId;

    // --- NEW: Skeleton Loader UI ---
    private ShimmerFrameLayout shimmerLayout;

    // --- NEW: Flags for minimum display time ---
    private boolean isDataLoaded = false;
    private boolean isMinimumTimeElapsed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.complaintdetails);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        overridePendingTransition(R.anim.slide_in_up, R.anim.stay_still);

        complaintId = getIntent().getStringExtra("complaint_id");

        bindViews();
        setInitialDataFromIntent(); // Show initial data immediately

        if (complaintId != null && !complaintId.isEmpty()) {
            fetchFullComplaintDetails(complaintId); // Fetch full data
        } else {
            Toast.makeText(this, "Invalid Complaint ID", Toast.LENGTH_SHORT).show();
            finish();
        }

        setupListeners();
    }

    private void bindViews() {
        btnClose = findViewById(R.id.btn_close);
        shareBtn = findViewById(R.id.share_btn);
        complaintLayout = findViewById(R.id.complaint_layout); // This is the real content layout
        titleTextView = findViewById(R.id.complaint_issuetype);
        complaintIdTextView = findViewById(R.id.complaint_id);
        statusTextView = findViewById(R.id.complaint_status);
        detailsTextView = findViewById(R.id.complaint_details);
        nextStepsTitle = findViewById(R.id.next_steps_title);
        nextStepsText = findViewById(R.id.next_steps_text);
        locationDetailsTextView = findViewById(R.id.complaint_location);
        dateTimeTextView = findViewById(R.id.complaint_date);
        printButton = findViewById(R.id.print_complaint);
        deleteBtn = findViewById(R.id.delete_complaint);
        imagesSection = findViewById(R.id.images_section);
        userImageView = findViewById(R.id.image1);
        completedImageView = findViewById(R.id.image2);

        // --- NEW: Bind Shimmer Layout ---
        shimmerLayout = findViewById(R.id.shimmer_view_container_details);
    }

    private void setInitialDataFromIntent() {
        // This data comes from the list item click, so it's available instantly
        titleTextView.setText(getIntent().getStringExtra("complaint_title"));
        complaintIdTextView.setText(complaintId);
        statusTextView.setText(getIntent().getStringExtra("complaint_status"));
        detailsTextView.setText(getIntent().getStringExtra("complaint_details"));
        locationDetailsTextView.setText(getIntent().getStringExtra("location"));
        dateTimeTextView.setText(getIntent().getStringExtra("datetime"));
    }

    private void setupListeners() {
        btnClose.setOnClickListener(v -> finish());
        deleteBtn.setOnClickListener(v -> confirmDelete());
        printButton.setOnClickListener(v -> showExportOptionDialog());
        shareBtn.setOnClickListener(v -> shareComplaintAsImage());
    }

    private void fetchFullComplaintDetails(String id) {
        String url = actapiconfig.getPublicAPI() + "complaintdetails.php";
        RequestQueue queue = Volley.newRequestQueue(this);

        // --- NEW: Start a 2-second (2000ms) timer ---
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            isMinimumTimeElapsed = true;
            tryToShowContent();
        }, 2000);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getBoolean("success")) {
                            parseAndDisplayComplaintDetails(jsonResponse);
                        } else {
                            Toast.makeText(this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("ComplaintDetails", "JSON Parsing Error: " + e.getMessage());
                        Toast.makeText(this, "Invalid response from server.", Toast.LENGTH_SHORT).show();
                    }
                    // --- CHANGED ---
                    isDataLoaded = true;
                    tryToShowContent();
                },
                error -> {
                    Log.e("ComplaintDetails", "Volley Error: " + error.toString());
                    Toast.makeText(this, "Error fetching details.", Toast.LENGTH_SHORT).show();
                    // --- CHANGED ---
                    isDataLoaded = true;
                    tryToShowContent();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("complaint_id", id);
                return params;
            }
        };
        queue.add(request);
    }

    // --- NEW: Gatekeeper method ---
    private void tryToShowContent() {
        if (isDataLoaded && isMinimumTimeElapsed) {
            showContent();
        }
    }

    private void showContent() {
        if (shimmerLayout != null && complaintLayout != null) {
            shimmerLayout.stopShimmer();
            shimmerLayout.setVisibility(View.GONE);
            complaintLayout.setVisibility(View.VISIBLE);
        }
    }

    // ... The rest of your methods (parseAndDisplayComplaintDetails, updateUiForStatus, confirmDelete, etc.) remain exactly the same ...
    // ... No changes are needed in the methods below this point ...


// ... inside your actcomplaintdetails class

    private void parseAndDisplayComplaintDetails(JSONObject data) throws JSONException {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        String status = data.getString("status");
        statusTextView.setText(status);
        detailsTextView.setText(data.getString("issue_details"));
        updateUiForStatus(status);

        String attachment1Base64 = data.optString("attachment1_base64", null);
        String completedImageBase64 = data.optString("completedimage_base64", null);

        boolean hasAttachment1 = attachment1Base64 != null && !attachment1Base64.isEmpty() && !attachment1Base64.equals("null");
        boolean hasCompletedImage = completedImageBase64 != null && !completedImageBase64.isEmpty() && !completedImageBase64.equals("null");

        if (hasAttachment1 || hasCompletedImage) {
            imagesSection.setVisibility(View.VISIBLE);
        } else {
            imagesSection.setVisibility(View.GONE);
        }

        // --- NEW CODE USING GLIDE ---
        if (hasAttachment1) {
            byte[] decodedBytes = Base64.decode(attachment1Base64, Base64.DEFAULT);
            Glide.with(this) // Use the activity context
                    .load(decodedBytes)
                    .into(userImageView);
        }

        if (hasCompletedImage) {
            byte[] decodedBytes = Base64.decode(completedImageBase64, Base64.DEFAULT);
            Glide.with(this)
                    .load(decodedBytes)
                    .into(completedImageView);
        }
    }


// YOU CAN NOW DELETE these two methods from your class. They are no longer needed.
/*
private Bitmap decodeBase64ToBitmap(...) { ... }
public static int calculateInSampleSize(...) { ... }
*/

    private void updateUiForStatus(String complaintStatus) {
        if ("Completed".equalsIgnoreCase(complaintStatus) || "Rejected".equalsIgnoreCase(complaintStatus)) {
            deleteBtn.setVisibility(View.GONE);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            printButton.setLayoutParams(params);
        } else {
            deleteBtn.setVisibility(View.VISIBLE);
        }

        // --- Change status color ---
        if ("Pending".equalsIgnoreCase(complaintStatus)) {
            statusTextView.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        } else if ("Completed".equalsIgnoreCase(complaintStatus)) {
            statusTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else if ("Rejected".equalsIgnoreCase(complaintStatus)) {
            statusTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else {
            statusTextView.setTextColor(getResources().getColor(android.R.color.black)); // default
        }

        // --- Next steps text based on status ---
        if ("Completed".equalsIgnoreCase(complaintStatus)) {
            nextStepsTitle.setText("Issue Rectified");
            nextStepsText.setText("• The issue has been resolved by the concerned department.\n"
                    + "• Thank you for your patience.\n"
                    + "• No further action is required.");
        } else if ("Rejected".equalsIgnoreCase(complaintStatus)) {
            nextStepsTitle.setText("Complaint Rejected");
            nextStepsText.setText("• Your complaint has been reviewed and marked as invalid.\n"
                    + "• Please verify the issue and raise a new complaint if necessary.\n"
                    + "• You may contact support for further clarification from About Us.");
        } else {
            nextStepsTitle.setText("What happens next?");
            nextStepsText.setText("• Your complaint will be reviewed within 24–48 hours.\n"
                    + "• Track progress in the 'Track Status' section.");
        }
    }

    private Bitmap decodeBase64ToBitmap(String base64Str) {
        try {
            byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            Log.e("DecodeBase64", "Failed to decode Base64 string", e);
            return null;
        }
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Complaint")
                .setMessage("Are you sure you want to delete this complaint permanently?")
                .setPositiveButton("Delete", (dialog, which) -> deleteComplaintFromServer(complaintId))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteComplaintFromServer(String id) {
        String url = actapiconfig.getPublicAPI() + "delete_complaint.php";
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        Toast.makeText(this, jsonResponse.getString("message"), Toast.LENGTH_SHORT).show();
                        if (jsonResponse.getBoolean("success")) {
                            finish();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Invalid response.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error: " + error.toString(), Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("complaint_id", id);
                return params;
            }
        };
        queue.add(request);
    }

    private void shareComplaintAsImage() {
        Bitmap bitmap = captureComplaintLayout();
        if (bitmap == null) return;
        try {
            File cachePath = new File(getCacheDir(), "images");
            cachePath.mkdirs();
            File file = new File(cachePath, "complaint_share.png");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            Uri contentUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
            if (contentUri != null) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/png");
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(shareIntent, "Share Complaint via"));
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error sharing image", Toast.LENGTH_SHORT).show();
        }
    }

    private void showExportOptionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Export Complaint")
                .setMessage("Choose a format to export your complaint details:")
                .setPositiveButton("PDF", (dialog, which) -> exportToPdf())
                .setNegativeButton("Image", (dialog, which) -> exportToImage())
                .setNeutralButton("Cancel", null)
                .show();
    }

    private void exportToPdf() {
        Bitmap bitmap = captureComplaintLayout();
        if (bitmap == null) return;
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        page.getCanvas().drawBitmap(bitmap, 0, 0, null);
        document.finishPage(page);
        try {
            File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            downloadDir.mkdirs();
            File file = new File(downloadDir, "Complaint_" + complaintId + ".pdf");
            document.writeTo(new FileOutputStream(file));
            Toast.makeText(this, "PDF saved in Downloads", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving PDF", Toast.LENGTH_SHORT).show();
        } finally {
            document.close();
        }
    }

    private void exportToImage() {
        Bitmap bitmap = captureComplaintLayout();
        if (bitmap == null) return;
        try {
            File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            downloadDir.mkdirs();
            File file = new File(downloadDir, "Complaint_" + complaintId + ".png");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            Toast.makeText(this, "Image saved in Downloads", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving image", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap captureComplaintLayout() {
        complaintLayout.setDrawingCacheEnabled(true);
        complaintLayout.buildDrawingCache();
        Bitmap cache = complaintLayout.getDrawingCache();
        if (cache == null) {
            Log.e("CaptureLayout", "Failed to build drawing cache for layout.");
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(cache);
        complaintLayout.setDrawingCacheEnabled(false);
        return bitmap;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.stay_still, R.anim.slide_out_down);
    }
}