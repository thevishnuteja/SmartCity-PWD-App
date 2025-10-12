package com.SIMATS.smartcity;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class actadminprofile extends AppCompatActivity {

    TextView hiUsername, tvUserName, tvJoined, tvRole, tvCases, tvUsers;
    ImageButton btnClose;
    ImageView ivProfilePicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adminprofilepage); // Make sure this XML file is named activity_admin_profile.xml
        overridePendingTransition(R.anim.slide_in_up, R.anim.stay_still);

        // Initialize views
        hiUsername = findViewById(R.id.hiusername);
        tvUserName = findViewById(R.id.tv_user_name);
        tvJoined = findViewById(R.id.tv_joined);
        tvRole = findViewById(R.id.tv_role);
        tvCases = findViewById(R.id.tv_cases);
        tvUsers = findViewById(R.id.tv_users);
        btnClose = findViewById(R.id.btn_close);
        ivProfilePicture = findViewById(R.id.iv_profile_picture);
//        btnEditProfile = findViewById(R.id.btn_edit_profile);

        // Set dummy text (optional)
        hiUsername.setText("Hi Admin!");
        tvUserName.setText("Vishnu (Admin)");
        tvJoined.setText("01/01/2020");
        tvRole.setText("District Admin");
        tvCases.setText("03");
        tvUsers.setText("05");

        // Close button action
        btnClose.setOnClickListener(v -> finish());

        ArcGaugeView arcGauge = findViewById(R.id.arcGauge);
        arcGauge.setProgress(15);


//        // Edit profile click (you can later open another activity or dialog here)
//        btnEditProfile.setOnClickListener(v -> {
//            // Placeholder for editing profile
//        });
    }
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.stay_still, R.anim.slide_out_down);
    }
}