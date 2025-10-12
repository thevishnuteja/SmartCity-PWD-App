package com.SIMATS.smartcity;

public class actapiconfig {

    // Local IP (used when on same Wi-Fi network)
    private static final String LOCAL_IP = "172.23.50.71";
    private static final String LOCAL_URL = "http://" + LOCAL_IP + "/smartcity/";

    // Ngrok URL (used when on mobile data or external)
    private static final String NGROK_URL = "https://e84a296d197d.ngrok-free.app/smartcity/"; // Replace with your live ngrok URL

    // Function to get Local API URL
    public static String getLocalAPI() {
        return LOCAL_URL;
    }

    // Function to get Ngrok/Public API URL
    public static String getPublicAPI() {
        return NGROK_URL;
    }
}