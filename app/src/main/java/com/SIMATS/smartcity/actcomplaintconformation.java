package com.SIMATS.smartcity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.button.MaterialButton;

public class actcomplaintconformation extends AppCompatActivity {

    private MaterialButton btnBackHome;
    private MaterialButton btnTrackComplaint;
    private LottieAnimationView successAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.complaintconformation);

        btnBackHome = findViewById(R.id.btn_back_home);
        btnTrackComplaint = findViewById(R.id.btn_track_complaint);
        successAnim = findViewById(R.id.successAnim);

        // Get data passed from the previous activity
        String complaintStatus = getIntent().getStringExtra("complaint_status");
        if ("success".equals(complaintStatus)) {
            Toast.makeText(this, "Complaint submitted successfully!", Toast.LENGTH_LONG).show();
            successAnim.playAnimation(); // Play success animation
        }

        // Back to Home button functionality
        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(actcomplaintconformation.this, actmainpage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // Track My Complaint button functionality
        btnTrackComplaint.setOnClickListener(v -> {
            Intent intent = new Intent(actcomplaintconformation.this, acttrackpage.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_up, R.anim.stay_still);
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.stay_still, R.anim.slide_out_down);
    }
}
