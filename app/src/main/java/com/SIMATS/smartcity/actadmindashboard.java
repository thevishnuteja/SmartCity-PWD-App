package com.SIMATS.smartcity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.widget.AppCompatButton;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class actadmindashboard extends AppCompatActivity {

    private ImageButton btnClose;
    private RecyclerView recyclerViewComplaints;
    private admincomplaintadapter complaintAdapter;
    private List<admincomplaintmodel> complaintList = new ArrayList<>();
    private SessionManager sessionManager;

    private TextView tvTotalUsers, tvTotalComplaints; // New

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admindashboard);
        overridePendingTransition(R.anim.slide_in_up, R.anim.stay_still);


        sessionManager = new SessionManager(this);

        btnClose = findViewById(R.id.btn_close_dashboard);
        recyclerViewComplaints = findViewById(R.id.recycler_view_complaintsall1);

        tvTotalUsers = findViewById(R.id.tv_total_users);
        tvTotalComplaints = findViewById(R.id.tv_total_complaints);
        AppCompatButton btnFilterAll = findViewById(R.id.btn_filter_all);
        AppCompatButton btnFilterCompleted = findViewById(R.id.btn_filter_completed);
        AppCompatButton btnFilterPending = findViewById(R.id.btn_filter_pending);
        AppCompatButton btnFilterRejected = findViewById(R.id.btn_filter_rejected);
        btnFilterAll.setOnClickListener(v -> fetchComplaints("All"));
        btnFilterCompleted.setOnClickListener(v -> fetchComplaints("Completed"));
        btnFilterPending.setOnClickListener(v -> fetchComplaints("Pending"));
        btnFilterRejected.setOnClickListener(v -> fetchComplaints("Rejected"));



        recyclerViewComplaints.setLayoutManager(new LinearLayoutManager(this));
        complaintAdapter = new admincomplaintadapter(complaintList, this);
        recyclerViewComplaints.setAdapter(complaintAdapter);

        btnClose.setOnClickListener(v -> finish());

        fetchCounts();          // Get counts from PHP
        fetchComplaints("All");
        // Load complaints list
    }

    private void fetchCounts() {
        String url = actapiconfig.getPublicAPI() + "get_counts.php"; // Your PHP file

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        Log.d("CountsResponse", response);
                        JSONObject jsonResponse = new JSONObject(response);

                        int usersCount = jsonResponse.getInt("users");
                        int complaintsCount = jsonResponse.getInt("complaints");

                        tvTotalUsers.setText(String.valueOf(usersCount));
                        tvTotalComplaints.setText(String.valueOf(complaintsCount));

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Parsing error in counts", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Network error in counts: " + error.toString(), Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                return new HashMap<>();
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void fetchComplaints(String statusFilter) {
        String url = actapiconfig.getPublicAPI() + "get_allcomplaints.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        Log.d("FilteredComplaints", response);
                        complaintList.clear();

                        JSONObject jsonResponse = new JSONObject(response);
                        JSONArray jsonArray = jsonResponse.getJSONArray("complaints");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            String id = obj.getString("complaint_id");
                            String title = obj.getString("issue_title");
                            String details = obj.getString("issue_details");
                            String status = obj.getString("status");

                            // If filter is not "All", only add matching complaints
                            if (statusFilter.equals("All") || status.equalsIgnoreCase(statusFilter)) {
                                complaintList.add(new admincomplaintmodel(id, title, details, status));
                            }
                        }

                        complaintAdapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Parsing error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Network error: " + error.toString(), Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                // You can also send statusFilter to PHP if you want filtering on server side
                params.put("status", statusFilter);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.stay_still, R.anim.slide_out_down);
    }
}
