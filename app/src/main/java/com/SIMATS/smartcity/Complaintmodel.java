package com.SIMATS.smartcity;

public class Complaintmodel {
    private String id;
    private String issueType;
    private String issueDetails;
    private String status;
    private String location;
    private String datetime;

    // Constructor
    public Complaintmodel(String id ,String issueType, String issueDetails, String status, String location, String datetime) {
        this.id = id;
        this.issueType = issueType;
        this.issueDetails = issueDetails;
        this.status = status;
        this.location = location;
        this.datetime = datetime;
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
    public String getlocation() {
        return location;
    }
    public String getdatetime() {
        return datetime;
    }
}

