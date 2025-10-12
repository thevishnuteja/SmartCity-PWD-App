package com.SIMATS.smartcity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class actforgotpass extends AppCompatActivity {

    private EditText emailInput;
    private Button confirmButton, signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgotpasspage); // <- make sure this matches your layout file name

        emailInput = findViewById(R.id.emailInput);
        confirmButton = findViewById(R.id.confirmButton);
        signUpButton = findViewById(R.id.signUpButton);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailInput.getText().toString().trim();

                // Client-side validation
                if (email.isEmpty()) {
                    Toast.makeText(actforgotpass.this, "Please fill in your email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!isValidEmail(email)) {
                    Toast.makeText(actforgotpass.this, "Invalid email format", Toast.LENGTH_SHORT).show();
                    return;
                }

                // If validation passes, send the email to the PHP script
                sendResetRequest(email);
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(actforgotpass.this, actsignup.class);
                startActivity(intent);
            }
        });
    }

    private void sendResetRequest(final String email) {
        // URL of your PHP script
        String url = actapiconfig.getPublicAPI() + "sendpasswordreset.php";

        // Show a loading message to the user
        Toast.makeText(this, "Sending reset link...", Toast.LENGTH_SHORT).show();

        // Create a StringRequest using Volley
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    // This is the success listener. It runs when the PHP script responds.
                    // The PHP script echoes "Message sent, please check your inbox."
                    // We can show this message and then go to the confirmation screen.

                    Toast.makeText(actforgotpass.this, response, Toast.LENGTH_LONG).show();

                    // Navigate to the confirmation screen on success
                    Intent intent = new Intent(actforgotpass.this, actpassresetconf.class);
                    startActivity(intent);
                },
                error -> {
                    // This is the error listener. It runs if there's a network error.
                    Toast.makeText(actforgotpass.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                // This method is where we send the POST data.
                Map<String, String> params = new HashMap<>();
                // The key "email" must match the one in your PHP script: $_POST["email"]
                params.put("email", email);

                return params;
            }
        };

        // Add the request to the RequestQueue to execute it.
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    // Method to validate email format
    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";
        return email.matches(emailPattern);
    }
}