package com.SIMATS.smartcity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class actwelcomepage extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcomepage);

        Button getStartedBtn = findViewById(R.id.btn_get_started);
        Button haveAccountBtn = findViewById(R.id.btn_have_account);

        getStartedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(actwelcomepage.this, actsignup.class);
                startActivity(intent);
            }
        });

        haveAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(actwelcomepage.this, actlogin.class);
                startActivity(intent);
            }
        });
    }
}
