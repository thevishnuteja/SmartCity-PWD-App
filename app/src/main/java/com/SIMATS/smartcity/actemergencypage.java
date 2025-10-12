package com.SIMATS.smartcity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class actemergencypage extends AppCompatActivity {

    private ImageButton btnClose;
    private LinearLayout linearLayoutContacts;  // LinearLayout to hold emergency contacts statically
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.emergencypage);  // Your layout XML

        // Initialize SessionManager to get user_id
        sessionManager = new SessionManager(this);

        // Initialize views
        btnClose = findViewById(R.id.btn_close);
        linearLayoutContacts = findViewById(R.id.emergencycontact);  // LinearLayout for contacts

        // Set click listener for the close button
        btnClose.setOnClickListener(v -> finish());

        // Set click listeners for emergency contacts
        setupEmergencyContactListeners();
    }

    // This method sets OnClickListeners for all emergency contacts TextViews
    private void setupEmergencyContactListeners() {
        // Set OnClickListener for each emergency contact
        setOnClickListenerForContact(R.id.tv_helpline_number, "1800-xxxx-001");
        setOnClickListenerForContact(R.id.tv_electricity_number, "1912");
        setOnClickListenerForContact(R.id.tv_water_supply_number, "1916");
        setOnClickListenerForContact(R.id.tv_municipal_waste_number, "155304");
        setOnClickListenerForContact(R.id.tv_disaster_management_number, "108");
        setOnClickListenerForContact(R.id.tv_road_repairs_number, "1800 xxxx 002");
        setOnClickListenerForContact(R.id.tv_senior_citizen_number, "14567");
        setOnClickListenerForContact(R.id.tv_gas_leak_number, "1906");
        setOnClickListenerForContact(R.id.tv_public_health_number, "104");
    }

    // This helper method sets the OnClickListener for each contact
    private void setOnClickListenerForContact(int textViewId, String phoneNumber) {
        TextView contactTextView = findViewById(textViewId);
        contactTextView.setOnClickListener(v -> openDialer(phoneNumber));
    }

    // Method to open the dialer with the emergency contact number
    private void openDialer(String phoneNumber) {
        // Create an intent to open the dialer
        Intent dialerIntent = new Intent(Intent.ACTION_DIAL);
        dialerIntent.setData(Uri.parse("tel:" + phoneNumber)); // Pass the phone number to the dialer
        startActivity(dialerIntent);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.stay_still, R.anim.slide_out_down);  // Adding animation
    }
}
