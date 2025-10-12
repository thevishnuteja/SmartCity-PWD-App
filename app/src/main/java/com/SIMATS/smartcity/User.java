package com.SIMATS.smartcity;

public class User {
    private String user_id;
    private String username;
    private String email;
    private String profilePicUrl; // NEW FIELD

    // MODIFIED CONSTRUCTOR
    public User(String user_id, String username, String email, String profilePicUrl) {
        this.user_id = user_id;
        this.username = username;
        this.email = email;
        this.profilePicUrl = profilePicUrl; // NEW
    }

    public String getUserId() {
        return user_id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    // NEW GETTER
    public String getProfilePicUrl() {
        return profilePicUrl;
    }
}