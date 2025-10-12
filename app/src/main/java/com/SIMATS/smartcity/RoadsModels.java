package com.SIMATS.smartcity;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Contains the data model classes required to parse the road data from the JSON resource.
 * These classes map directly to the structure of your JSON file.
 *
 * NOTE: This version is updated to match the final roads.json structure.
 */
public class RoadsModels {

    public static class RoadsData {
        @SerializedName("roads")
        public List<Road> roads;
    }

    public static class Road {
        @SerializedName("name")
        public String name;

        @SerializedName("status")
        public String status;

        @SerializedName("properties")
        public Properties properties;

        @SerializedName("path")
        public List<Coordinate> path;
    }

    public static class Properties {
        // CORRECTED: Changed from int to String to match "HYD-ORR-01" format
        @SerializedName("id")
        public String id;

        // ADDED: New fields from JSON
        @SerializedName("type")
        public String type;

        @SerializedName("lanes")
        public int lanes;

        // Existing fields
        @SerializedName("maintained_by")
        public String maintained_by;

        @SerializedName("last_maintained")
        public String last_maintained;

        @SerializedName("project_details")
        public String project_details;

        @SerializedName("expected_completion")
        public String expected_completion;
    }

    public static class Coordinate {
        @SerializedName("lat")
        public double lat;

        @SerializedName("lng")
        public double lng;
    }
}