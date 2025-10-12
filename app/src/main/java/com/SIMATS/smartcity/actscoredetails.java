package com.SIMATS.smartcity;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.airbnb.lottie.LottieAnimationView; // Import LottieAnimationView

public class actscoredetails extends AppCompatActivity {

    private TextView tvScore;
    private TextView tvReportsScore, tvAccuracyScore, tvEngagementScore;
    private LinearLayout sectionScoreList;
    private LottieAnimationView ratingAnimationView; // Variable for the animation

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scoredetails);

        // Apply entry animation
        overridePendingTransition(R.anim.slide_in_up, R.anim.stay_still);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Header and Badge
        tvScore = findViewById(R.id.tv_score);

        // Score Metrics
        tvReportsScore = findViewById(R.id.tv_reports_points);
        tvAccuracyScore = findViewById(R.id.tv_accuracy_points);
        tvEngagementScore = findViewById(R.id.tv_engagement_points);

        // *** FIX: Correctly find the LinearLayout and the LottieAnimationView by their proper IDs ***
        sectionScoreList = findViewById(R.id.score_complaints_section);
        ratingAnimationView = findViewById(R.id.rating_animation_view);

        // Close button functionality
        ImageButton btnClose = findViewById(R.id.btn_close);
        btnClose.setOnClickListener(v -> finish());

        // Info buttons functionality
        findViewById(R.id.info_reports).setOnClickListener(v ->
                showInfoDialog("Reports Submitted", "You earn up to 40 points based on how many valid complaints you've submitted.")
        );
        findViewById(R.id.info_accuracy).setOnClickListener(v ->
                showInfoDialog("Report Accuracy", "You earn up to 30 points based on how many reports were approved or marked valid.")
        );
        findViewById(R.id.info_engagement).setOnClickListener(v ->
                showInfoDialog("Engagement", "You earn up to 10 points for profile completeness and app usage.")
        );

        // Retrieve data from Intent
        int honourScore = getIntent().getIntExtra("honour_score", 0);
        double reportScore = getIntent().getDoubleExtra("report_score", 0);
        double validityScore = getIntent().getDoubleExtra("validity_score", 0);
        double profileScore = getIntent().getDoubleExtra("profile_score", 0);

        // Set values in UI
        tvScore.setText("Honour Score: " + honourScore + " / 100");
        tvReportsScore.setText(String.format(java.util.Locale.US,"%.1f / 40", reportScore));
        tvAccuracyScore.setText(String.format(java.util.Locale.US,"%.1f / 30", validityScore));
        tvEngagementScore.setText(String.format(java.util.Locale.US,"%.1f / 10", profileScore));
    }

    private void showInfoDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.stay_still, R.anim.slide_out_down); // Exit animation
    }
}