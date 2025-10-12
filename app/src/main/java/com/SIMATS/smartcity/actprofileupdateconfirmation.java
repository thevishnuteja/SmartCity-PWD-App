package com.SIMATS.smartcity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

    public class actprofileupdateconfirmation extends AppCompatActivity {

    private MaterialButton btnBackHome, btnViewProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Make sure your new XML file is named "layout_profile_updated.xml"
        setContentView(R.layout.profileupdateconformation);

        btnBackHome = findViewById(R.id.btn_back_home);
        btnViewProfile = findViewById(R.id.btn_view_profile);

        // Set click listener for the "Back to Home" button
        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, actmainpage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // Close this activity
        });

        // Set click listener for the "View Profile" button
        btnViewProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, actprofile.class);
            // We don't clear the stack here, so the user can go back to the main page
            startActivity(intent);
            finish(); // Close this activity to avoid stacking confirmation pages
        });
    }

    @Override
    public void finish() {
        super.finish();
        // Apply the smooth slide-down animation when closing
        overridePendingTransition(R.anim.stay_still, R.anim.slide_out_down);
    }
}
