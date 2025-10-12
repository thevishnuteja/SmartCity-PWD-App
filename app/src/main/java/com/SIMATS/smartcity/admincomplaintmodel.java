package com.SIMATS.smartcity;

public class admincomplaintmodel {
    private String id;
    private String issueType;
    private String issueDetails;
    private String status;

    // Constructor
    public admincomplaintmodel(String id ,String issueType, String issueDetails, String status) {
        this.id = id;
        this.issueType = issueType;
        this.issueDetails = issueDetails;
        this.status = status;
    }

    // Getters
    public String getid() {
        return id;
    }
    public String getIssueType() {
        return issueType;
    }

    public String getIssueDetails() {
        return issueDetails;
    }

    public String getStatus() {
        return status;
    }
}

