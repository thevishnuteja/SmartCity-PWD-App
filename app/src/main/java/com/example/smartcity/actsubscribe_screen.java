package com.example.smartcity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class actsubscribe_screen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subscribe_screen);

        Button subscribeBtn = findViewById(R.id.btn_subscribe);

        subscribeBtn.setOnClickListener(v -> {
            // After subscribing → move to Welcome screen
            Intent i = new Intent(actsubscribe_screen.this, actwelcomepage.class);
            startActivity(i);
            finish(); // close subscribe screen
        });
    }
}
