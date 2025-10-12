package com.SIMATS.smartcity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "UserPrefs";

    // Common Keys
    private static final String KEY_ROLE = "role"; // either "user" or "admin"

    // User Keys
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PROFILE_PROMPT_SHOWN = "isProfilePromptShown";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_DATE_OF_BIRTH = "date_of_birth";
    private static final String KEY_MOBILE_NUMBER = "mobile_number";
    private static final String KEY_CITY = "city";
    private static final String KEY_OCCUPATION = "occupation";

    // Admin Keys
    private static final String KEY_ADMIN_ID = "admin_id";
    private static final String KEY_ADMIN_EMAIL = "admin_email";
    private static final String KEY_ADMIN_USERNAME = "admin_username";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context; // FIX #1: Declare a Context member variable

    public SessionManager(Context context) {
        this.context = context; // FIX #1: Store the context for later use
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // USER login session
    public void createLoginSession(int userId, String username, String email,
                                   String mobileNumber, String city, String occupation) {
        editor.putString(KEY_ROLE, "user");
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_MOBILE_NUMBER, mobileNumber);
        editor.putString(KEY_CITY, city);
        editor.putString(KEY_OCCUPATION, occupation);
        editor.apply();
    }

    // ADMIN login session
    public void createAdminLoginSession(int adminId, String username, String email) {
        editor.putString(KEY_ROLE, "admin");
        editor.putInt(KEY_ADMIN_ID, adminId);
        editor.putString(KEY_ADMIN_USERNAME, username);
        editor.putString(KEY_ADMIN_EMAIL, email);
        editor.apply();
    }

    // Check if anyone is logged in
    public boolean isLoggedIn() {
        return getRole() != null;
    }

    public void setProfilePromptShown(boolean hasBeenShown) {
        editor.putBoolean(KEY_PROFILE_PROMPT_SHOWN, hasBeenShown);
        editor.commit(); // Use commit() here for immediate saving, which is fine for this flag.
    }

    public boolean hasProfilePromptBeenShown() {
        // FIX #2: Use the correct variable name 'sharedPreferences' instead of 'pref'
        return sharedPreferences.getBoolean(KEY_PROFILE_PROMPT_SHOWN, false);
    }

    // Check role
    public String getRole() {
        return sharedPreferences.getString(KEY_ROLE, null);
    }

    // USER getters
    public int getUserId() {
        return sharedPreferences.getInt(KEY_USER_ID, -1);
    }

    public String getUsername() {
        return sharedPreferences.getString(KEY_USERNAME, null);
    }

    public String getEmail() {
        return sharedPreferences.getString(KEY_EMAIL, null);
    }

    public String getMobileNumber() {
        return sharedPreferences.getString(KEY_MOBILE_NUMBER, null);
    }

    public String getCity() {
        return sharedPreferences.getString(KEY_CITY, null);
    }

    public String getOccupation() {
        return sharedPreferences.getString(KEY_OCCUPATION, null);
    }

    // ADMIN getters
    public int getAdminId() {
        return sharedPreferences.getInt(KEY_ADMIN_ID, -1);
    }

    public String getAdminUsername() {
        return sharedPreferences.getString(KEY_ADMIN_USERNAME, null);
    }

    public String getAdminEmail() {
        return sharedPreferences.getString(KEY_ADMIN_EMAIL, null);
    }

    // Logout
    public void logoutUser() {
        // Clearing all data from SharedPreferences
        editor.clear();
        editor.commit();

        // After logout redirect user to Welcome Activity
        // FIX #1 (continued): Use the stored 'context' variable
        Intent i = new Intent(context, actwelcomepage.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }
}