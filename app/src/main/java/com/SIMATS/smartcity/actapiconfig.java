package com.SIMATS.smartcity;

public class actapiconfig {

    // Local IP (used when on same Wi-Fi network)
    private static final String LOCAL_IP = "172.23.50.71";
    private static final String LOCAL_URL = "http://" + LOCAL_IP + "/smartcity/";

    // Ngrok URL (used when on mobile data or external)
    private static final String NGROK_URL = "http://14.139.187.229:8081/PDD-2025(9thmonth)/smartcity_backend-main/smartcity/"; // Replace with your live ngrok URL

    // Function to get Local API URL
    public static String getLocalAPI() {
        return LOCAL_URL;
    }

    // Function to get Ngrok/Public API URL
    public static String getPublicAPI() {
        return NGROK_URL;
    }
}