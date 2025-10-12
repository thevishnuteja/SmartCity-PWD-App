package com.SIMATS.smartcity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class actadminmainpage extends AppCompatActivity {

    private CardView dashboardCard, complaintCard, navProfileCard;
    private FloatingActionButton fabAdd;
    private TextView greetingText;
    private ImageView notificationIcon;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adminmainpage);

        // Initialize Views
        dashboardCard = findViewById(R.id.card_dashboard);  // Corrected
        complaintCard = findViewById(R.id.card_complaint);  // Corrected
        navProfileCard = findViewById(R.id.nav_profile);
        greetingText = findViewById(R.id.admin_greeting);
        notificationIcon = findViewById(R.id.notification_icon);
        logoutButton = findViewById(R.id.admin_logout);

        TextView dashboardDate = findViewById(R.id.dashboard_date);
        TextView trackDate = findViewById(R.id.track_date);
        TextView adminIdText = findViewById(R.id.admin_id);
        TextView adminNameText = findViewById(R.id.profileadmin);

        SimpleDateFormat sdf = new SimpleDateFormat("📅 dd MMMM yyyy", Locale.getDefault());
        String currentDate = sdf.format(new Date());

        dashboardDate.setText(currentDate);
        trackDate.setText(currentDate);

        // Session Manager
        SessionManager session = new SessionManager(this);

        // Set greeting dynamically
        int adminId = session.getAdminId();
        greetingText.setText("Welcome Admin!");
        adminIdText.setText("Admin Id: " + adminId);

        // Dashboard Card Click
        dashboardCard.setOnClickListener(v -> {
            Intent intent = new Intent(actadminmainpage.this, actadmindashboard.class);
            startActivity(intent);
        });

        // Complaint Card Click
        complaintCard.setOnClickListener(v -> {
            Intent intent = new Intent(actadminmainpage.this, actadmintrackusers.class);
            startActivity(intent);
        });

        // Admin Profile Card Click
        navProfileCard.setOnClickListener(v -> {
            Intent intent = new Intent(actadminmainpage.this, actadminprofile.class);
            startActivity(intent);
        });

        // Notification icon click
        notificationIcon.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:support@smartcity.com"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Support Request");
            intent.putExtra(Intent.EXTRA_TEXT, "Dear Support Team,\n\n");

            try {
                startActivity(Intent.createChooser(intent, "Send Email"));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
            }
        });

        // Logout button click
        logoutButton.setOnClickListener(v -> showLogoutDialog());
    }

    // Logout dialog
    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Logout admin
                    SessionManager session = new SessionManager(getApplicationContext());
                    session.logoutUser();

                    Intent intent = new Intent(actadminmainpage.this, actwelcomepage.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_up, R.anim.stay_still);
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
