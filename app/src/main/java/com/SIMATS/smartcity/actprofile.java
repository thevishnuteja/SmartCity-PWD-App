package com.SIMATS.smartcity;

import android.content.Intent;
import android.graphics.Bitmap; // NEW IMPORT
import android.graphics.BitmapFactory; // NEW IMPORT
import android.os.Bundle;
import android.util.Base64; // NEW IMPORT
import android.widget.ImageButton;
import android.widget.ImageView; // NEW IMPORT
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONObject;

public class actprofile extends AppCompatActivity {

    private ImageButton btnClose;
    private TextView tvUserName, tvDob, tvCity, tvReports, tvOccupation, tvHiUserName;
    private ArcGaugeView arcGaugeView;
    private double reportScore = 0;
    private double validityScore = 0;
    private int honourScore = 0;
    private int reports = 0;
    private double profileScore = 0;
    private SessionManager sessionManager;
    private TextView btnEditProfile;
    private ImageView ivProfilePicture; // NEW: ImageView for the profile picture

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profilepage);

        sessionManager = new SessionManager(this);

        btnClose = findViewById(R.id.btn_close);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        tvUserName = findViewById(R.id.tv_user_name);
        tvDob = findViewById(R.id.tv_dob);
        tvCity = findViewById(R.id.tv_city);
        tvReports = findViewById(R.id.tv_reports_submitted);
        tvOccupation = findViewById(R.id.tv_occupation);
        tvHiUserName = findViewById(R.id.hiusername);
        arcGaugeView = findViewById(R.id.arcGauge);
        ivProfilePicture = findViewById(R.id.iv_profile_picture); // NEW: Initialize the ImageView

        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(actprofile.this, actprofileedit.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_up, R.anim.stay_still);
        });

        TextView btnScoreDetails = findViewById(R.id.btn_score_details);
        btnScoreDetails.setOnClickListener(v -> {
            Intent intent = new Intent(actprofile.this, actscoredetails.class);
            intent.putExtra("honour_score", honourScore);
            intent.putExtra("total_reports", reports);
            intent.putExtra("report_score", reportScore);
            intent.putExtra("validity_score", validityScore);
            intent.putExtra("profile_score", profileScore);
            startActivity(intent);
        });

        btnClose.setOnClickListener(v -> finish());

        // We fetch the profile every time the activity is created to get the latest data
    }

    // NEW: Use onResume to refresh data when returning from the edit screen
    @Override
    protected void onResume() {
        super.onResume();
        getUserProfile();
    }


    private void getUserProfile() {
        int userId = sessionManager.getUserId();
        if (userId == -1) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = actapiconfig.getPublicAPI() + "get_user_details.php?user_id=" + userId;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject profileObject = new JSONObject(response);
                        if (!profileObject.getString("status").equals("success")) {
                            Toast.makeText(actprofile.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // ... (existing data parsing remains the same)
                        String userName = profileObject.optString("username", "Unknown");
                        String dob = profileObject.optString("date_of_birth", "Not Provided");
                        String city = profileObject.optString("city", "Not Provided");
                        String occupation = profileObject.optString("occupation", "Not Provided");
                        honourScore = profileObject.optInt("honour_score", 0);
                        reportScore = profileObject.optInt("report_score", 0);
                        validityScore = profileObject.optInt("validity_score", 0);
                        profileScore = profileObject.optInt("profile_score", 0);
                        reports = profileObject.optInt("total_reports", 0);
                        // ... and so on

                        // Set text views
                        tvUserName.setText(userName);
                        tvDob.setText(dob);
                        tvCity.setText(city);
                        tvOccupation.setText(occupation);
                        tvHiUserName.setText("Hi " + userName + "!");
                        arcGaugeView.setProgress(honourScore);
                        tvReports.setText(String.valueOf(reports));

                        // --- NEW: LOGIC TO DECODE AND SET THE PROFILE PICTURE ---
                        String base64Image = profileObject.optString("profile_pic_base64", null);

                        if (base64Image != null && !base64Image.isEmpty()) {
                            try {
                                // The string from PHP includes the "data:image/..." prefix, remove it
                                String pureBase64 = base64Image.substring(base64Image.indexOf(",") + 1);

                                byte[] decodedString = Base64.decode(pureBase64, Base64.DEFAULT);
                                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                                if (decodedByte != null) {
                                    ivProfilePicture.setImageBitmap(decodedByte);
                                } else {
                                    // If decoding fails, fall back to default
                                    ivProfilePicture.setImageResource(R.drawable.defaultprofile);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                // In case of any error during decoding, set the default image
                                ivProfilePicture.setImageResource(R.drawable.defaultprofile);
                            }
                        } else {
                            // If no image is sent from server, set the default
                            ivProfilePicture.setImageResource(R.drawable.defaultprofile);
                        }
                        // --- END OF NEW LOGIC ---

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(actprofile.this, "Error parsing profile", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(actprofile.this, "Error fetching profile", Toast.LENGTH_SHORT).show());

        Volley.newRequestQueue(this).add(stringRequest);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.stay_still, R.anim.slide_out_down);
    }
}