package com.SIMATS.smartcity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class actadmintrackusers extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<User> userList;
    private TextView tvUserCount;
    private ImageButton btnClose;
    private MaterialButton btnActiveUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admintrackusers);
        overridePendingTransition(R.anim.slide_in_up, R.anim.stay_still);

        // Initialize Views
        recyclerView = findViewById(R.id.recycler_view_users);
        tvUserCount = findViewById(R.id.tv_user_count);
        btnClose = findViewById(R.id.btn_close);
        btnActiveUsers = findViewById(R.id.btn_active_users);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        adapter = new UserAdapter(userList, this);
        recyclerView.setAdapter(adapter);

        // Set Click Listeners
        btnClose.setOnClickListener(v -> finish());

        btnActiveUsers.setOnClickListener(v -> {
            // In the future, you can add filtering logic here.
            Toast.makeText(this, "Filter functionality coming soon.", Toast.LENGTH_SHORT).show();
        });

        // Fetch user data from the server
        new FetchUsersTask().execute(actapiconfig.getPublicAPI() + "get_user.php");
    }

    private class FetchUsersTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            if (urls.length == 0) {
                return null;
            }

            StringBuilder response = new StringBuilder();
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                return null; // Return null on error
            }
            return response.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result == null) {
                Toast.makeText(actadmintrackusers.this, "Failed to fetch data.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                JSONArray jsonArray = new JSONArray(result);
                userList.clear(); // Clear previous data

                // Loop through the JSON array to parse each user
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);

                    String id = obj.getString("user_id");
                    String name = obj.getString("username");
                    String email = obj.getString("email");

                    // This is the new part: get the profile picture path.
                    // Using optString is safer as it returns an empty string or null if the key doesn't exist.
                    String profilePic = obj.optString("profile_pic", null);

                    // Add the new User object (with the profile pic path) to the list
                    userList.add(new User(id, name, email, profilePic));
                }

                // Notify the adapter that the data has changed to refresh the RecyclerView
                adapter.notifyDataSetChanged();
                // Update the user count text
                tvUserCount.setText(String.valueOf(userList.size()));

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(actadmintrackusers.this, "Error parsing data.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
        // Apply custom animation when closing the activity
        overridePendingTransition(R.anim.stay_still, R.anim.slide_out_down);
    }
}