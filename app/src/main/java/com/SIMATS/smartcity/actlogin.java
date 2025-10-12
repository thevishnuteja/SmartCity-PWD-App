package com.SIMATS.smartcity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

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

public class actlogin extends AppCompatActivity {
    private EditText emailInput, passwordInput;
    private Button signInButton, signUpButton;
    private MaterialButton googleSignInButton; // Changed from googleSignUpButton for clarity
    private TextView forgotPasswordText;
    private SessionManager sessionManager;

    // --- Google Sign-In Variables ---
    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginpage);

        // Initialize views
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        signInButton = findViewById(R.id.signInButton);
        signUpButton = findViewById(R.id.signUpButton);
        googleSignInButton = findViewById(R.id.googleSignUpButton); // Using the ID from XML
        forgotPasswordText = findViewById(R.id.forgotPassword);
        sessionManager = new SessionManager(this);


        // --- Configure Google Sign-In ---
        // Configure sign-in to request the user's ID, email address, and basic profile.
        // ID and basic profile are included in DEFAULT_SIGN_IN.
        // requestIdToken is crucial for backend authentication.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                // Use the string resource you just created
                .requestIdToken(getString(R.string.server_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // --- Initialize the ActivityResultLauncher ---
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        handleSignInResult(task);
                    } else {
                        Toast.makeText(this, "Google Sign In cancelled.", Toast.LENGTH_SHORT).show();
                    }
                });


        // --- Set OnClick Listeners ---
        signInButton.setOnClickListener(view -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(actlogin.this, "Please enter both email and password.", Toast.LENGTH_SHORT).show();
            } else {
                performLogin(email, password);
            }
        });

        signUpButton.setOnClickListener(view -> {
            Intent intent = new Intent(actlogin.this, actsignup.class);
            startActivity(intent);
        });

        forgotPasswordText.setOnClickListener(view -> {
            // Create an Intent to start the forgot password activity
            Intent intent = new Intent(actlogin.this, actforgotpass.class);
            // Execute the Intent
            startActivity(intent);
        });

        // --- Google Sign-In Button Listener ---
        googleSignInButton.setOnClickListener(view -> {
            signInWithGoogle();
        });
    }

    private void signInWithGoogle() {
        // First, sign out of any existing account to force the account picker.
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            // Now that the sign-out is complete, launch the sign-in intent as normal.
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, get the ID token
            String idToken = account.getIdToken();
            Log.d("GoogleSignIn", "ID Token: " + idToken);

            // --- SEND TOKEN TO YOUR BACKEND ---
            performGoogleLogin(idToken);

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            Log.w("GoogleSignIn", "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(this, "Google Sign In Failed. Code: " + e.getStatusCode(), Toast.LENGTH_LONG).show();
        }
    }

    private void performGoogleLogin(final String idToken) {
        String url = actapiconfig.getPublicAPI() + "auth.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    // This response handling is IDENTICAL to your regular login,
                    // as the backend should return the same JSON structure.
                    try {
                        Log.d("GoogleLoginResponse", "Response: " + response);
                        JSONObject jsonResponse = new JSONObject(response);

                        if (jsonResponse.has("role")) {
                            String role = jsonResponse.getString("role");
                            int userId = jsonResponse.getInt("user_id");
                            String username = jsonResponse.getString("username");
                            String emailResponse = jsonResponse.getString("email");

                            Log.d("LoginDebug", "Logged in as: " + role);

                            Intent intent;

                            if (role.equalsIgnoreCase("admin")) {
                                sessionManager.createAdminLoginSession(userId, username, emailResponse);
                                intent = new Intent(actlogin.this, actadminmainpage.class);
                            } else {
                                String mobileNumber = jsonResponse.getString("mobile_number");
                                String city = jsonResponse.getString("city");
                                String occupation = jsonResponse.getString("occupation");
                                sessionManager.createLoginSession(userId, username, emailResponse, mobileNumber, city, occupation);
                                intent = new Intent(actlogin.this, actmainpage.class);
                            }

                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();

                        } else if (jsonResponse.has("error")) {
                            Toast.makeText(actlogin.this, jsonResponse.getString("error"), Toast.LENGTH_LONG).show();
                        }

                    } catch (Exception e) {
                        Log.e("LoginError", "Exception details: " + e.getMessage(), e);
                        Toast.makeText(actlogin.this, "Frontend Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(actlogin.this, "Login failed. Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "google_login");
                params.put("id_token", idToken);
                return params;
            }

            // ======================= ADD THIS METHOD =======================
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                // This special header tells ngrok to skip its browser warning page.
                headers.put("ngrok-skip-browser-warning", "true");
                return headers;
            }
            // ===============================================================
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    // Your existing performLogin method remains unchanged
    private void performLogin(final String email, final String password) {
        String url = actapiconfig.getPublicAPI() + "auth.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        Log.d("LoginResponse", "Response: " + response);
                        JSONObject jsonResponse = new JSONObject(response);

                        if (jsonResponse.has("role")) {
                            String role = jsonResponse.getString("role");
                            int userId = jsonResponse.getInt("user_id");
                            String username = jsonResponse.getString("username");
                            String emailResponse = jsonResponse.getString("email");

                            Log.d("LoginDebug", "Logged in as: " + role);

                            Intent intent;

                            if (role.equalsIgnoreCase("admin")) {
                                sessionManager.createAdminLoginSession(userId, username, emailResponse);
                                intent = new Intent(actlogin.this, actadminmainpage.class);
                            } else {
                                String mobileNumber = jsonResponse.getString("mobile_number");
                                String city = jsonResponse.getString("city");
                                String occupation = jsonResponse.getString("occupation");
                                sessionManager.createLoginSession(userId, username, emailResponse, mobileNumber, city, occupation);
                                intent = new Intent(actlogin.this, actmainpage.class);
                            }

                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();

                        } else if (jsonResponse.has("error")) {
                            Toast.makeText(actlogin.this, jsonResponse.getString("error"), Toast.LENGTH_LONG).show();
                        }

                    } catch (Exception e) {
                        Log.e("LoginError", "Exception details: " .concat(e.getMessage()), e);
                        Toast.makeText(actlogin.this, "Frontend Error: ".concat(e.getMessage()), Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(actlogin.this, "Login failed. Please check your network connection. Error: ".concat(error.getMessage()), Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "login");
                params.put("email", email);
                params.put("password", password);
                return params;
            }

            // ======================= ADD THIS METHOD =======================
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                // This special header tells ngrok to skip its browser warning page.
                headers.put("ngrok-skip-browser-warning", "true");
                return headers;
            }
            // ===============================================================
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}