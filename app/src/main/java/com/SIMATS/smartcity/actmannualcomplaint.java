package com.SIMATS.smartcity;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class actmannualcomplaint extends AppCompatActivity {
    // ... (variables are mostly the same)
    private AutoCompleteTextView spinnerIssueType;
    private TextInputEditText etIssueDetails, etDateTime, etLocation, etLandmark, etAdditionalInfo;
    private ActivityResultLauncher<Intent> locationPickerLauncher;
    private MaterialButton btnSubmit;
    private ImageButton btnClose;
    private Calendar calendar;

    // --- MODIFIED: Launchers for Attachments ---
    private LinearLayout layoutAddAttachment;
    private RelativeLayout attachmentSlot1, attachmentSlot2;
    private TextView attachmentFilename1, attachmentFilename2, tvAttachmentLabel;
    private ImageButton btnRemoveAttachment1, btnRemoveAttachment2;
    private ArrayList<Uri> selectedImageUris = new ArrayList<>();
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private Uri cameraImageUri;

    // NEW: Launcher for requesting the camera permission
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;


    // --- NEW: Inner classes for custom dropdown ---

    /**
     * Data model for an item in the issue type dropdown.
     * Holds the title text and the icon's resource ID.
     */
    private static class IssueItem {
        final String title;
        final int iconResId;

        IssueItem(String title, int iconResId) {
            this.title = title;
            this.iconResId = iconResId;
        }

        // This is crucial for the AutoCompleteTextView to display the text after selection
        @NonNull
        @Override
        public String toString() {
            return title;
        }
    }


    /**
     * Custom ArrayAdapter to display an icon next to the text in the dropdown.
     */
    private class IssueTypeAdapter extends ArrayAdapter<IssueItem> {

        public IssueTypeAdapter(Context context, List<IssueItem> items) {
            // Using a standard layout, we will modify the TextView within it.
            super(context, android.R.layout.simple_spinner_dropdown_item, items);
        }

        // This method is called to get the view for the selected item (when dropdown is closed)
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            // We use the default implementation which calls toString() on the object
            TextView view = (TextView) super.getView(position, convertView, parent);
            IssueItem item = getItem(position);
            if (item != null) {
                // Set the icon on the left of the text
                view.setCompoundDrawablesWithIntrinsicBounds(item.iconResId, 0, 0, 0);
                // Add some padding between the icon and the text
                view.setCompoundDrawablePadding(24);
            }
            return view;
        }

        // This method is called for each item in the dropdown list
        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            // We use the default implementation which creates the TextView
            TextView view = (TextView) super.getDropDownView(position, convertView, parent);
            IssueItem item = getItem(position);
            if (item != null) {
                view.setText(item.title);
                view.setCompoundDrawablesWithIntrinsicBounds(item.iconResId, 0, 0, 0);
                view.setCompoundDrawablePadding(24);
            }
            return view;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mannualcomplaint);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // ==========================================================
        // === CORRECTED ORDER OF OPERATIONS TO PREVENT CRASH ===
        // ==========================================================

        // STEP 1: All views are initialized FIRST.
        initializeViews();

        // STEP 2: All setup methods are called.
        setupIssueTypeDropdown();
        setupDateTimePicker();
        setupAttachmentPicker();
        setupSubmitButton();
        setupLocationPicker();
        btnClose.setOnClickListener(v -> finish());

        // STEP 3: Handle incoming data from other activities.
        String placeName = getIntent().getStringExtra("place_name");
        if (placeName != null) {
            etLocation.setText(placeName);
        }

        // STEP 4: NOW it is safe to handle the chatbot data, because all views exist.
        // In actmannualcomplaint.java -> onCreate() method
// ... after your initializeViews(); call ...

        Intent intent = getIntent();
        if (intent != null) {
            String issueTypeFromChatbot = intent.getStringExtra("PREFILL_ISSUE_TYPE");
            String prefillIssueDetails = intent.getStringExtra("PREFILL_ISSUE_DETAILS"); // This line was already here
            String prefillImageUriString = intent.getStringExtra("PREFILL_IMAGE_URI");
            String prefillDateTime = intent.getStringExtra("PREFILL_DATE_TIME");

            if (issueTypeFromChatbot != null) {
                spinnerIssueType.setText(issueTypeFromChatbot, false);
            }

            // ================== ADD THIS IF BLOCK ==================
            if (prefillIssueDetails != null) {
                etIssueDetails.setText(prefillIssueDetails);
            }
            // =======================================================

            if (prefillImageUriString != null) {
                Uri imageUri = Uri.parse(prefillImageUriString);
                if (!selectedImageUris.contains(imageUri)) {
                    selectedImageUris.add(imageUri);
                    updateAttachmentUI();
                }
            }

            if (prefillDateTime != null) {
                etDateTime.setText(prefillDateTime);
            }
        }
        // ==========================================================
    }

    private void setupAttachmentPicker() {
        // Launcher for choosing from Gallery (remains the same)
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            selectedImageUris.add(imageUri);
                            updateAttachmentUI();
                        }
                    }
                }
        );

        // Launcher for taking a photo with the Camera (remains the same)
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success) {
                        selectedImageUris.add(cameraImageUri);
                        updateAttachmentUI();
                    } else {
                        Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // NEW: Launcher for handling the result of the CAMERA permission request
        requestCameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // Permission is granted. Continue the action
                        launchCameraIntent();
                    } else {
                        // Explain to the user that the feature is unavailable
                        Toast.makeText(this, "Camera permission is required to take photos.", Toast.LENGTH_LONG).show();
                    }
                });


        layoutAddAttachment.setOnClickListener(v -> {
            if (selectedImageUris.size() < 2) {
                showImageSourceDialog();
            } else {
                Toast.makeText(this, "You can only attach up to 2 files.", Toast.LENGTH_SHORT).show();
            }
        });

        // Remove button logic remains the same
        btnRemoveAttachment1.setOnClickListener(v -> {
            if (!selectedImageUris.isEmpty()) {
                selectedImageUris.remove(0);
                updateAttachmentUI();
            }
        });
        btnRemoveAttachment2.setOnClickListener(v -> {
            if (selectedImageUris.size() > 1) {
                selectedImageUris.remove(1);
            } else if (selectedImageUris.size() == 1) {
                selectedImageUris.remove(0);
            }
            updateAttachmentUI();
        });
    }

    private void showImageSourceDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Add Attachment")
                .setItems(new CharSequence[]{"Take Photo", "Choose from Gallery"}, (dialog, which) -> {
                    if (which == 0) { // Take Photo
                        // MODIFIED: We now check for permission before launching
                        checkCameraPermissionAndLaunch();
                    } else { // Choose from Gallery
                        launchGalleryIntent();
                    }
                })
                .show();
    }

    // NEW: Method that checks for camera permission before proceeding
    private void checkCameraPermissionAndLaunch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Permission is already granted
            launchCameraIntent();
        } else {
            // Permission is not granted, so we request it
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchGalleryIntent() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    // RENAMED from launchCamera to launchCameraIntent for clarity
    // MODIFIED: This version saves the image to a different, more stable directory
    private void launchCameraIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File imageFile = null;
        try {
            // Create the File where the photo should go
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Calendar.getInstance().getTime());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            imageFile = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
        } catch (IOException ex) {
            // Error occurred while creating the File
            Log.e("CameraError", "Error creating image file", ex);
            Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
            return;
        }

        // Continue only if the File was successfully created
        if (imageFile != null) {
            cameraImageUri = FileProvider.getUriForFile(
                    this,
                    "com.example.smartcity.provider", // Using your package name directly
                    imageFile
            );
            cameraLauncher.launch(cameraImageUri);
        }
    }


    // --- All other methods below are unchanged ---

    @SuppressLint("ClickableViewAccessibility")
    private void setupLocationPicker() {
        locationPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        String selectedAddress = result.getData().getStringExtra("selected_location_address");
                        if (selectedAddress != null) {
                            etLocation.setText(selectedAddress);
                        }
                    }
                });
        etLocation.setOnTouchListener((v, event) -> {
            final int DRAWABLE_RIGHT = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (etLocation.getRight() - etLocation.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    Intent intent = new Intent(actmannualcomplaint.this, activitylocationpicker.class);
                    locationPickerLauncher.launch(intent);
                    return true;
                }
            }
            return false;
        });
    }

    private void initializeViews() {
        spinnerIssueType = findViewById(R.id.spinner_issue_type);
        etIssueDetails = findViewById(R.id.et_issue_details);
        etIssueDetails.setHighlightColor(ContextCompat.getColor(this, R.color.hint_text_color));
        etDateTime = findViewById(R.id.et_date_time);
        etLocation = findViewById(R.id.et_location);
        etLandmark = findViewById(R.id.et_landmark);
        etAdditionalInfo = findViewById(R.id.et_additional_info);
        btnSubmit = findViewById(R.id.btn_submit);
        btnClose = findViewById(R.id.btn_close);
        calendar = Calendar.getInstance();
        layoutAddAttachment = findViewById(R.id.layout_add_attachment);
        attachmentSlot1 = findViewById(R.id.attachment_slot_1);
        attachmentSlot2 = findViewById(R.id.attachment_slot_2);
        attachmentFilename1 = findViewById(R.id.attachment_filename_1);
        attachmentFilename2 = findViewById(R.id.attachment_filename_2);
        tvAttachmentLabel = findViewById(R.id.tv_attachment_label);
        btnRemoveAttachment1 = findViewById(R.id.btn_remove_attachment_1);
        btnRemoveAttachment2 = findViewById(R.id.btn_remove_attachment_2);
    }

    /**
     * MODIFIED: This method now uses a custom adapter to show icons.
     */
    private void setupIssueTypeDropdown() {
        ArrayList<CharSequence> issueItems = new ArrayList<>();

        issueItems.add(getTextWithIcon(" Potholes", R.drawable.pothole));
        issueItems.add(getTextWithIcon(" Cracks on Road", R.drawable.damage));
        issueItems.add(getTextWithIcon(" Waterlogging", R.drawable.waterlog));
        issueItems.add(getTextWithIcon(" Damaged Speed Breaker", R.drawable.bumps));
        issueItems.add(getTextWithIcon(" Street Light Not Working", R.drawable.streetlight));
        issueItems.add(getTextWithIcon(" Blocked Drainage", R.drawable.drainage));
        issueItems.add(getTextWithIcon(" Faded Road Markings", R.drawable.roadmark));
        issueItems.add(getTextWithIcon(" Garbage on Road", R.drawable.garabaseonroad));
        issueItems.add(getTextWithIcon(" Other", R.drawable.other));

        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, issueItems
        );

        spinnerIssueType.setAdapter(adapter);
        spinnerIssueType.setOnClickListener(v -> spinnerIssueType.showDropDown());
    }

    private CharSequence getTextWithIcon(String text, int drawableRes) {
        SpannableString spannable = new SpannableString("  " + text);
        Drawable d = getResources().getDrawable(drawableRes);
        d.setBounds(0, 0, 50, 50); // size of the icon
        ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM);
        spannable.setSpan(span, 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        return spannable;
    }


    private void setupDateTimePicker() {
        etDateTime.setFocusable(false);
        etDateTime.setClickable(true);
        etDateTime.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    showTimePicker();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    updateDateTimeField();
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
        );
        timePickerDialog.show();
    }

    private void updateDateTimeField() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        etDateTime.setText(sdf.format(calendar.getTime()));
    }

    private void updateAttachmentUI() {
        attachmentSlot1.setVisibility(View.GONE);
        attachmentSlot2.setVisibility(View.GONE);
        layoutAddAttachment.setVisibility(View.VISIBLE);
        tvAttachmentLabel.setText("Attachments Files here");
        if (selectedImageUris.size() >= 1) {
            attachmentSlot1.setVisibility(View.VISIBLE);
            attachmentFilename1.setText("1 file attached");
        }
        if (selectedImageUris.size() >= 2) {
            attachmentSlot2.setVisibility(View.VISIBLE);
            attachmentFilename2.setText("2 files attached");
            layoutAddAttachment.setVisibility(View.GONE);
        }
        if (selectedImageUris.isEmpty()) {
            tvAttachmentLabel.setText("No file attached");
        }
    }

    private void setupSubmitButton() {
        btnSubmit.setOnClickListener(v -> {
            if (validateForm()) {
                checkProfileAndSubmit();
            }
        });
    }
    // ADD THESE TWO NEW METHODS to your actmannualcomplaint.java file

    /**
     * Checks the user's profile on the server before attempting to submit the complaint.
     */
    private void checkProfileAndSubmit() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Verifying profile...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        SessionManager sessionManager = new SessionManager(this);
        int userId = sessionManager.getUserId();

        // We use the same API script as before
        String url = actapiconfig.getPublicAPI() + "check_profile_status.php?user_id=" + userId;

        StringRequest request = new StringRequest(Request.Method.GET, url, response -> {
            progressDialog.dismiss(); // Dismiss the dialog once we have a response
            try {
                JSONObject jsonObject = new JSONObject(response);

                if (jsonObject.has("error")) {
                    Toast.makeText(this, "Could not verify profile. Please try again.", Toast.LENGTH_SHORT).show();
                    Log.e("ProfileCheck", "Error from server: " + jsonObject.getString("error"));
                    return;
                }

                String dob = jsonObject.optString("date_of_birth", "");
                String mobile = jsonObject.optString("mobile_number", "");
                String location = jsonObject.optString("city", "");
                String occupation = jsonObject.optString("occupation", "");

                boolean isProfileIncomplete = "0000-00-00".equals(dob) ||
                        "0000000000".equals(mobile) || mobile.isEmpty() ||
                        "NA".equalsIgnoreCase(location) || location.isEmpty() ||
                        "NA".equalsIgnoreCase(occupation) || occupation.isEmpty();

                if (isProfileIncomplete) {
                    // If profile is incomplete, show the "update required" dialog
                    showProfileUpdateRequiredDialog();
                } else {
                    // If profile is complete, proceed with submitting the complaint
                    submitComplaintToServer();
                }

            } catch (JSONException e) {
                Toast.makeText(this, "Error reading profile data.", Toast.LENGTH_SHORT).show();
                Log.e("ProfileCheckJSON", "Error parsing profile status JSON", e);
            }
        }, error -> {
            progressDialog.dismiss();
            Toast.makeText(this, "Network error. Could not verify profile.", Toast.LENGTH_LONG).show();
            Log.e("ProfileCheckVolley", "Network error checking profile status", error);
        });

        Volley.newRequestQueue(this).add(request);
    }

    /**
     * Shows a dialog informing the user that their profile is incomplete and must be updated.
     */
    private void showProfileUpdateRequiredDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Profile Incomplete")
                .setMessage("Please update your profile details to proceed with submitting a complaint.")
                .setCancelable(false)
                .setPositiveButton("Update Profile", (dialog, which) -> {
                    // Redirect user to the profile editing screen
                    // Make sure you have an activity named 'acteditprofile.class'
                    Intent intent = new Intent(actmannualcomplaint.this, actprofileedit.class);
                    startActivity(intent);
                })
                .setNegativeButton("Later", (dialog, which) -> {
                    // User chose not to update now, so just dismiss the dialog
                    dialog.dismiss();
                })
                .show();
    }

    private boolean validateForm() {
        if (spinnerIssueType.getText().toString().trim().isEmpty()) {
            spinnerIssueType.setError("Please select an issue type");
            return false;
        }
        if (etIssueDetails.getText().toString().trim().isEmpty()) {
            etIssueDetails.setError("Please describe the issue");
            return false;
        }
        if (etDateTime.getText().toString().trim().isEmpty()) {
            etDateTime.setError("Please select date and time");
            return false;
        }
        if (etLocation.getText().toString().trim().isEmpty()) {
            etLocation.setError("Please enter the location");
            return false;
        }
        return true;
    }

    private void submitComplaintToServer() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Submitting complaint...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        String url = actapiconfig.getPublicAPI() + "complaint.php";
        RequestQueue queue = Volley.newRequestQueue(this);
        SessionManager sessionManager = new SessionManager(this);
        int userId = sessionManager.getUserId();
        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, url,
                response -> {
                    progressDialog.dismiss();
                    try {
                        // This logs the raw server response, which is great for debugging
                        String rawResponse = new String(response.data);
                        Log.d("RAW_RESPONSE", "Server sent: " + rawResponse);

                        JSONObject jsonResponse = new JSONObject(rawResponse);
                        String status = jsonResponse.optString("status").trim();

                        // ================= THIS IS THE MISSING PART =================
                        // Check if the status from PHP was "success"
                        if ("success".equals(status)) {
                            // If SUCCESS, show the success toast and go to the confirmation screen
                            Toast.makeText(actmannualcomplaint.this, "Complaint submitted successfully!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(actmannualcomplaint.this, actcomplaintconformation.class));
                            finish();
                        } else {
                            // If status is not "success", show the real error from the server
                            String errorMessage = jsonResponse.optString("message", "An unknown error occurred.");
                            Toast.makeText(actmannualcomplaint.this, "Submission Failed: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                        // =============================================================

                    } catch (JSONException e) {
                        // If the server sends something that isn't JSON
                        Toast.makeText(actmannualcomplaint.this, "Error parsing server response.", Toast.LENGTH_LONG).show();
                        Log.e("PARSING_ERROR", "Could not parse server response", e);
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    Toast.makeText(actmannualcomplaint.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(userId));
                params.put("issue_type", spinnerIssueType.getText().toString().trim());
                params.put("issue_details", etIssueDetails.getText().toString().trim());
                params.put("date_time", etDateTime.getText().toString().trim());
                params.put("location", etLocation.getText().toString().trim());
                params.put("landmark", etLandmark.getText().toString().trim());
                params.put("additional_info", etAdditionalInfo.getText().toString().trim());
                params.put("status", "Pending");
                params.put("privacy_policy_agreement", "1");
                return params;
            }
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                try {
                    if (selectedImageUris.size() > 0) {
                        params.put("attachment1", new DataPart("image1.jpg", getFileDataFromUri(selectedImageUris.get(0))));
                    }
                    if (selectedImageUris.size() > 1) {
                        params.put("attachment2", new DataPart("image2.jpg", getFileDataFromUri(selectedImageUris.get(1))));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return params;
            }
        };
        queue.add(multipartRequest);
    }

    private byte[] getFileDataFromUri(Uri uri) throws IOException {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.stay_still, R.anim.slide_out_down);
    }
}