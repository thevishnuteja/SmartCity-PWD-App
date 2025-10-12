package com.SIMATS.smartcity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build; // IMPORT THIS
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.WindowManager; // IMPORT THIS
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class actprofileedit extends AppCompatActivity {
    private ImageButton btnClose;
    private Button btnChangePicture, btnUpdateProfile;
    private TextInputEditText etDob;
    private EditText etUserName, etCity, etOccupation, etMobileNumber;
    private ImageView ivProfilePic;
    private SessionManager sessionManager;
    private Calendar calendar;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profileedit);

        // ======================= KEYBOARD BEHAVIOR FIX =======================
        // Add this block to force the correct window behavior for resizing.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        // ===================================================================

        sessionManager = new SessionManager(this);
        calendar = Calendar.getInstance();

        // Initialize views
        btnClose = findViewById(R.id.btn_close);
        btnChangePicture = findViewById(R.id.btn_change_picture);
        btnUpdateProfile = findViewById(R.id.btn_updateprofile);
        ivProfilePic = findViewById(R.id.profile_pic);
        etUserName = findViewById(R.id.user_name);
        etDob = findViewById(R.id.date_of_birth);
        etCity = findViewById(R.id.city);
        etOccupation = findViewById(R.id.occupation);
        etMobileNumber = findViewById(R.id.mobile_number);

        btnUpdateProfile.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary_blue));

        setupImagePicker();
        btnClose.setOnClickListener(v -> finish());

        btnChangePicture.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        btnUpdateProfile.setOnClickListener(v -> updateProfile());
        setupDatePicker();
        getUserProfile();
    }

    private void getUserProfile() {
        int userId = sessionManager.getUserId();
        if (userId == -1) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        String url = actapiconfig.getPublicAPI() + "get_user_details.php?user_id=" + userId;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if ("success".equals(response.getString("status"))) {
                            etUserName.setText(response.getString("username"));
                            etDob.setText(response.getString("date_of_birth"));
                            etCity.setText(response.getString("city"));
                            etOccupation.setText(response.getString("occupation"));
                            etMobileNumber.setText(response.getString("mobile_number"));

                            String base64Image = response.optString("profile_pic_base64", null);

                            if (base64Image != null && !base64Image.isEmpty()) {
                                try {
                                    String pureBase64 = base64Image.substring(base64Image.indexOf(",") + 1);
                                    byte[] decodedString = Base64.decode(pureBase64, Base64.DEFAULT);
                                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                    if (decodedByte != null) {
                                        ivProfilePic.setImageBitmap(decodedByte);
                                    } else {
                                        ivProfilePic.setImageResource(R.drawable.profileedit);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    ivProfilePic.setImageResource(R.drawable.profileedit);
                                }
                            } else {
                                ivProfilePic.setImageResource(R.drawable.profileedit);
                            }

                        } else {
                            Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing profile data.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Failed to fetch profile details.", Toast.LENGTH_SHORT).show();
                });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonObjectRequest);
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            ivProfilePic.setImageURI(imageUri);
                            try {
                                String base64Image = convertUriToBase64(imageUri);
                                String finalImageString = "data:image/jpeg;base64," + base64Image;
                                uploadProfilePicture(finalImageString);
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(this, "Failed to convert image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );
    }

    private String convertUriToBase64(Uri imageUri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(imageUri);
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        byte[] imageBytes = byteBuffer.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private void uploadProfilePicture(final String base64Image) {
        int userId = sessionManager.getUserId();
        if (userId == -1) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }
        String url = actapiconfig.getPublicAPI() + "upload_profile_pic.php";
        Toast.makeText(this, "Uploading picture...", Toast.LENGTH_SHORT).show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String message = jsonResponse.getString("message");
                        Toast.makeText(actprofileedit.this, message, Toast.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(actprofileedit.this, "Error parsing server response.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Upload failed: " + error.toString(), Toast.LENGTH_LONG).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(userId));
                params.put("image_data", base64Image);
                return params;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

    private void setupDatePicker() {
        etDob.setFocusable(false);
        etDob.setClickable(true);
        etDob.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
                    calendar.set(Calendar.YEAR, selectedYear);
                    calendar.set(Calendar.MONTH, selectedMonth);
                    calendar.set(Calendar.DAY_OF_MONTH, selectedDayOfMonth);
                    updateDobField();
                }, year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void updateDobField() {
        String dateFormat = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.getDefault());
        etDob.setText(sdf.format(calendar.getTime()));
    }

    private void updateProfile() {
        int userId = sessionManager.getUserId();
        String url = actapiconfig.getPublicAPI() + "edit_profile.php";
        final String username = etUserName.getText().toString();
        final String dob = etDob.getText().toString();
        final String city = etCity.getText().toString();
        final String occupation = etOccupation.getText().toString();
        final String mobile = etMobileNumber.getText().toString();
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(this, response, Toast.LENGTH_LONG).show();
                    if (response.toLowerCase().contains("success")) {
                        Intent intent = new Intent(actprofileedit.this, actprofileupdateconfirmation.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_up, R.anim.stay_still);
                        finish();
                    }
                },
                error -> Toast.makeText(this, "Error updating profile: " + error.toString(), Toast.LENGTH_LONG).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", String.valueOf(userId));
                params.put("username", username);
                params.put("date_of_birth", dob);
                params.put("mobile_number", mobile);
                params.put("city", city);
                params.put("occupation", occupation);
                return params;
            }
        };
        queue.add(stringRequest);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.stay_still, R.anim.slide_out_down);
    }
}