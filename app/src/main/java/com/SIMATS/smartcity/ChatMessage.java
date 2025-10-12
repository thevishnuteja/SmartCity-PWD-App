package com.SIMATS.smartcity;

import android.graphics.Bitmap;
import android.net.Uri;

public class ChatMessage {
    public static final int TYPE_USER = 0;
    public static final int TYPE_AI = 1;

    // Core properties
    private final String text;
    private final int type;
    private final Bitmap image; // For displaying an image in a chat message

    // Properties for AI messages that contain an action button
    private String buttonText = null;
    private String issueType = "";
    private String issueDetails = "";
    private String dateTime = "";
    private Uri imageUri = null;

    // Constructor for simple text messages (user or AI)
    public ChatMessage(String text, int type) {
        this.text = text;
        this.type = type;
        this.image = null; // No image for simple text messages
    }

    // Constructor for a user's message that includes an image
    public ChatMessage(String text, Bitmap image) {
        this.text = text;
        this.type = TYPE_USER;
        this.image = image;
    }

    // Constructor for an AI message that requires an action (e.g., a button)
    public ChatMessage(String text, String buttonText, String issueType, String issueDetails, String dateTime, Uri imageUri) {
        this.text = text;
        this.type = TYPE_AI;
        this.image = null;
        this.buttonText = buttonText;
        this.issueType = issueType;
        this.issueDetails = issueDetails;
        this.dateTime = dateTime;
        this.imageUri = imageUri;
    }

    // --- Getters for all properties ---

    public String getText() {
        return text;
    }

    public int getType() {
        return type;
    }

    public Bitmap getImage() {
        return image;
    }

    public String getButtonText() {
        return buttonText;
    }

    public String getIssueType() {
        return issueType;
    }

    public String getIssueDetails() {
        return issueDetails;
    }

    public String getDateTime() {
        return dateTime;
    }

    public Uri getImageUri() {
        return imageUri;
    }
}