package com.SIMATS.smartcity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.shimmer.ShimmerFrameLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class acthistory extends AppCompatActivity {

    private ImageButton btnClose;
    private TextView tvComplaintCount;
    private RecyclerView recyclerViewComplaints;
    private ComplaintAdapter complaintAdapter;
    private List<Complaintmodel> complaintList = new ArrayList<>();
    private SessionManager sessionManager;
    private ShimmerFrameLayout shimmerLayout; // <-- ADD THIS

    // --- NEW: Flags to ensure minimum display time ---
    private boolean isDataLoaded = false;
    private boolean isMinimumTimeElapsed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.historypage);

        sessionManager = new SessionManager(this);

        // Initialize views
        btnClose = findViewById(R.id.btn_close);
        tvComplaintCount = findViewById(R.id.tv_complaint_count);
        recyclerViewComplaints = findViewById(R.id.recycler_view_complaintsall);
        shimmerLayout = findViewById(R.id.shimmer_layout_history); // <-- ADD THIS

        // Setup RecyclerView
        recyclerViewComplaints.setLayoutManager(new LinearLayoutManager(this));
        complaintAdapter = new ComplaintAdapter(complaintList, this);
        recyclerViewComplaints.setAdapter(complaintAdapter);

        btnClose.setOnClickListener(v -> finish());

        // Fetch the complaints from the backend
        fetchComplaints();
    }

    private void fetchComplaints() {
        int userId = sessionManager.getUserId();
        String url = actapiconfig.getPublicAPI() + "all_complaints.php";

        // --- NEW: Start a 2-second (2000ms) timer ---
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            isMinimumTimeElapsed = true;
            tryToShowRecyclerView(); // Check if content can be shown
        }, 2000);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        Log.d("ComplaintResponse", "Response: " + response);
                        complaintList.clear();
                        JSONArray jsonArray = new JSONArray(response);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject complaintObject = jsonArray.getJSONObject(i);
                            String id = complaintObject.getString("id");
                            String issueType = complaintObject.getString("issue_type");
                            String issueDetails = complaintObject.getString("issue_details");
                            String status = complaintObject.getString("status");
                            String location = complaintObject.getString("location");
                            String datetime = complaintObject.getString("date_time");
                            complaintList.add(new Complaintmodel(id, issueType, issueDetails, status, location, datetime));
                        }
                        tvComplaintCount.setText(String.format("%02d", complaintList.size()));
                        complaintAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                    // --- CHANGED: Set data loaded flag and try to show content ---
                    isDataLoaded = true;
                    tryToShowRecyclerView();
                },
                error -> {
                    Toast.makeText(this, "Error fetching complaints: " + error.toString(), Toast.LENGTH_LONG).show();
                    // --- CHANGED: Set data loaded flag and try to show content ---
                    isDataLoaded = true;
                    tryToShowRecyclerView();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("action", "get_complaints");
                params.put("user_id", String.valueOf(userId));
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

    // --- NEW: Gatekeeper method to check both conditions ---
    private void tryToShowRecyclerView() {
        // Only show the content if both data is loaded AND 2 seconds have passed
        if (isDataLoaded && isMinimumTimeElapsed) {
            showRecyclerView();
        }
    }

    private void showRecyclerView() {
        shimmerLayout.stopShimmer();
        shimmerLayout.setVisibility(View.GONE);
        recyclerViewComplaints.setVisibility(View.VISIBLE);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.stay_still, R.anim.slide_out_down);
    }
}