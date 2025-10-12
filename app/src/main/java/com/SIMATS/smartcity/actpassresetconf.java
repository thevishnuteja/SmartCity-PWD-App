package com.SIMATS.smartcity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class actpassresetconf extends AppCompatActivity {

    Button goToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.passresetconfpage);

        goToLogin = findViewById(R.id.btn_go_login);

        goToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(actpassresetconf.this, actlogin.class);
                startActivity(intent);
                finish();
            }
        });
    }
}