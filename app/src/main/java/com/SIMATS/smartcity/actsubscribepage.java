package com.SIMATS.smartcity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class actsubscribepage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the content view to your XML layout file.
        // Make sure your layout file is named 'welcome_scr.xml' or change R.layout.welcome_scr accordingly.
        setContentView(R.layout.subscribepage);

        // Find the button from the layout using its ID.
        MaterialButton subscribeButton = findViewById(R.id.btn_get_started);

        // Set a click listener on the button to handle user taps.
        subscribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This code will now open the actwelcomepage activity.
                // Make sure you have a corresponding 'actwelcomepage.java' file in your project.
                Intent intent = new Intent(actsubscribepage.this, actwelcomepage.class);
                startActivity(intent);
            }
        });
    }
}