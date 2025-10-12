package com.SIMATS.smartcity;

import android.graphics.Bitmap; // NEW IMPORT
import android.graphics.BitmapFactory; // NEW IMPORT
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64; // NEW IMPORT
import android.widget.ImageButton;
import android.widget.ImageView; // NEW IMPORT
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class adminuserdetails extends AppCompatActivity {

    TextView tvName, tvEmail, tvPhone, tvDOB, tvCity, tvOccupation, tvComplaints;
    private ImageButton btnClose;
    private MaterialButton btnBlockUser, btnDeleteUser, btnResetPassword;
    private ImageView ivProfilePicture; // NEW

    int userId = 1;
    String currentStatus = null; // null = active, "block" = blocked

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adminuserdetails);
        overridePendingTransition(R.anim.slide_in_up, R.anim.stay_still);

        tvName = findViewById(R.id.tv_user_name);
        tvEmail = findViewById(R.id.gmail);
        tvPhone = findViewById(R.id.phonenum);
        tvDOB = findViewById(R.id.tv_dob);
        tvCity = findViewById(R.id.tv_city);
        tvOccupation = findViewById(R.id.tv_occupation);
        tvComplaints = findViewById(R.id.tv_reports_submitted);
        ivProfilePicture = findViewById(R.id.iv_profile_picture); // NEW

        btnClose = findViewById(R.id.btn_close);
        btnBlockUser = findViewById(R.id.btn_block_user);
        btnDeleteUser = findViewById(R.id.btn_delete_user);
        btnResetPassword = findViewById(R.id.btn_reset_password);

        btnClose.setOnClickListener(v -> finish());

        if (getIntent().hasExtra("user_id")) {
            userId = Integer.parseInt(getIntent().getStringExtra("user_id"));
        }

        btnBlockUser.setOnClickListener(v -> {
            // Your existing logic...
            if (currentStatus == null || currentStatus.equalsIgnoreCase("null") || currentStatus.isEmpty()) {
                new ManageUserTask().execute(userId, "block");
            } else if ("block".equalsIgnoreCase(currentStatus)) {
                new ManageUserTask().execute(userId, "unblock");
            }
        });

        btnDeleteUser.setOnClickListener(v -> {
            new ManageUserTask().execute(userId, "delete");
        });

        btnResetPassword.setOnClickListener(v -> {
            new ManageUserTask().execute(userId, "reset_password");
        });

        new FetchUserDetailsTask().execute(userId);
    }

    private class FetchUserDetailsTask extends AsyncTask<Integer, Void, String> {
        @Override
        protected String doInBackground(Integer... params) {
            // ... (No changes in doInBackground)
            try {
                URL url = new URL(actapiconfig.getPublicAPI() + "adminuserdetails.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                String postData = URLEncoder.encode("user_id", "UTF-8") + "=" + params[0];
                writer.write(postData);
                writer.flush();
                writer.close();
                os.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                conn.disconnect();
                return sb.toString();

            } catch (Exception e) {
                return "{\"error\":\"" + e.getMessage() + "\"}";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                JSONObject obj = new JSONObject(s);
                if (obj.has("error")) {
                    Toast.makeText(adminuserdetails.this, obj.getString("error"), Toast.LENGTH_LONG).show();
                } else {
                    // Setting text data...
                    tvName.setText(obj.getString("username"));
                    tvEmail.setText("Email: " + obj.getString("email"));
                    tvPhone.setText("Phone: " + obj.getString("mobile_number"));
                    tvDOB.setText(obj.getString("date_of_birth"));
                    tvCity.setText(obj.getString("city"));
                    tvOccupation.setText(obj.getString("occupation"));
                    tvComplaints.setText(obj.getString("complaints_count"));

                    // --- NEW: LOGIC TO DECODE AND SET THE PROFILE PICTURE ---
                    String base64Image = obj.optString("profile_pic_base64", null);
                    if (base64Image != null && !base64Image.isEmpty()) {
                        try {
                            String pureBase64 = base64Image.substring(base64Image.indexOf(",") + 1);
                            byte[] decodedString = Base64.decode(pureBase64, Base64.DEFAULT);
                            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            if (decodedByte != null) {
                                ivProfilePicture.setImageBitmap(decodedByte);
                            }
                        } catch (Exception e) {
                            ivProfilePicture.setImageResource(R.drawable.defaultprofile);
                        }
                    } else {
                        ivProfilePicture.setImageResource(R.drawable.defaultprofile);
                    }
                    // --- END OF NEW LOGIC ---

                    // Setting status for block/unblock button
                    if (!obj.isNull("status")) {
                        currentStatus = obj.getString("status");
                    } else {
                        currentStatus = null;
                    }
                    btnBlockUser.setText( (currentStatus != null && currentStatus.equals("block")) ? "Unblock" : "Block");
                }
            } catch (Exception e) {
                Toast.makeText(adminuserdetails.this, "Parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    // ... (Your ManageUserTask remains unchanged)
    private class ManageUserTask extends AsyncTask<Object, Void, String> {
        @Override
        protected String doInBackground(Object... params) {
            try {
                int uid = (int) params[0];
                String action = (String) params[1];
                URL url = new URL(actapiconfig.getPublicAPI() + "manage_user.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                String postData = URLEncoder.encode("user_id", "UTF-8") + "=" + uid +
                        "&" + URLEncoder.encode("action", "UTF-8") + "=" + URLEncoder.encode(action, "UTF-8");
                writer.write(postData);
                writer.flush();
                writer.close();
                os.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                conn.disconnect();
                return sb.toString();

            } catch (Exception e) {
                return "{\"error\":\"" + e.getMessage() + "\"}";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                JSONObject obj = new JSONObject(s);
                if (obj.has("error")) {
                    Toast.makeText(adminuserdetails.this, obj.getString("error"), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(adminuserdetails.this, obj.getString("message"), Toast.LENGTH_LONG).show();

                    if (obj.has("blocked_status")) {
                        currentStatus = obj.isNull("blocked_status") ? null : obj.optString("blocked_status", null);
                        btnBlockUser.setText(currentStatus == null ? "Block" : "Unblock");
                    }
                }
            } catch (Exception e) {
                Toast.makeText(adminuserdetails.this, "Parse error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.stay_still, R.anim.slide_out_down);
    }
}