package com.SIMATS.smartcity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class actsignup extends AppCompatActivity {
    private EditText etUsername, etEmail, etPassword, etReenter;
    private Button btnGetStarted, btnSignIn;
    private ProgressBar passwordStrengthBar;

    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private SessionManager sessionManager;
    private MaterialButton googleSignUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signuppage);

        etUsername = findViewById(R.id.et_username);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etReenter = findViewById(R.id.et_reenter);
        btnGetStarted = findViewById(R.id.btn_get_started);
        btnSignIn = findViewById(R.id.btn_sign_in);
        passwordStrengthBar = findViewById(R.id.password_strength_bar);
        passwordStrengthBar.setProgress(0);

        googleSignUpButton = findViewById(R.id.btn_google_sign_in);
        sessionManager = new SessionManager(this);

        setupPasswordStrengthChecker();
        setupGetStartedButton();
        setupSignInButton();
        setupGoogleSignUp();
    }

    private void setupPasswordStrengthChecker() {
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = s.toString();
                if (password.length() < 6) passwordStrengthBar.setProgress(20);
                else if (password.length() < 10) passwordStrengthBar.setProgress(50);
                else passwordStrengthBar.setProgress(100);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupGetStartedButton() {
        btnGetStarted.setOnClickListener(view -> {
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String reenter = etReenter.getText().toString().trim();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || reenter.isEmpty()) {
                Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Email format validation
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Password length validation
            if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters long.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(reenter)) {
                Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
                return;
            }

            performRegularSignup(username, email, password);
        });
    }

    private void setupSignInButton() {
        btnSignIn.setOnClickListener(view -> {
            Intent intent = new Intent(actsignup.this, actlogin.class);
            startActivity(intent);
        });
    }

    private void performRegularSignup(String username, String email, String password) {
        String url = actapiconfig.getPublicAPI() + "auth.php";
        RequestQueue queue = Volley.newRequestQueue(actsignup.this);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(actsignup.this, "Signup successful! Please log in.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(actsignup.this, actlogin.class);
                    startActivity(intent);
                    finish();
                },
                error -> Toast.makeText(actsignup.this, "Error: " + error.toString(), Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "signup");
                params.put("username", username);
                params.put("email", email);
                params.put("password", password);
                params.put("dob", "0000-00-00");
                params.put("mobile", "");
                params.put("city", "NA");
                params.put("occupation", "NA");
                params.put("profile_pic", "");
                return params;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("ngrok-skip-browser-warning", "true");
                return headers;
            }
        };
        queue.add(request);
    }

    private void setupGoogleSignUp() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        handleSignInResult(task);
                    } else {
                        Toast.makeText(this, "Google Sign Up cancelled.", Toast.LENGTH_SHORT).show();
                    }
                });

        googleSignUpButton.setOnClickListener(view -> {
            mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                googleSignInLauncher.launch(signInIntent);
            });
        });
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String idToken = account.getIdToken();
            Log.d("GoogleSignUp", "ID Token: " + idToken);
            performGoogleLogin(idToken);
        } catch (ApiException e) {
            Log.w("GoogleSignUp", "signUpResult:failed code=" + e.getStatusCode());
            Toast.makeText(this, "Google Sign Up Failed. Code: " + e.getStatusCode(), Toast.LENGTH_LONG).show();
        }
    }

    private void performGoogleLogin(final String idToken) {
        String url = actapiconfig.getPublicAPI() + "auth.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);

                        if (jsonResponse.has("role")) {
                            int userId = jsonResponse.getInt("user_id");
                            String username = jsonResponse.getString("username");
                            String emailResponse = jsonResponse.getString("email");
                            String mobileNumber = jsonResponse.getString("mobile_number");
                            String city = jsonResponse.getString("city");
                            String occupation = jsonResponse.getString("occupation");

                            sessionManager.createLoginSession(userId, username, emailResponse, mobileNumber, city, occupation);
                            Intent intent = new Intent(actsignup.this, actmainpage.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();

                        } else if (jsonResponse.has("error")) {
                            Toast.makeText(actsignup.this, jsonResponse.getString("error"), Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(actsignup.this, "Frontend Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    Toast.makeText(actsignup.this, "Login failed. Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "google_login");
                params.put("id_token", idToken);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("ngrok-skip-browser-warning", "true");
                return headers;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}
