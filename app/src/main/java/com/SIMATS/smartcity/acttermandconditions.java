package com.SIMATS.smartcity;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class acttermandconditions extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.termsandconditions); // Your layout XML file for Terms
        ImageButton btnClose = findViewById(R.id.btn_close);
        btnClose.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.stay_still, R.anim.slide_out_down); // exit animation
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.stay_still, R.anim.slide_out_down); // apply exit animation
    }
}
