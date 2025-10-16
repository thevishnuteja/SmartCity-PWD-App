package com.SIMATS.smartcity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color; // Note: While we could use Color.parseColor, using ContextCompat is better.
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat; // Import for safer color retrieval
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class admincomplaintadapter extends RecyclerView.Adapter<admincomplaintadapter.ComplaintViewHolder> {

    private List<admincomplaintmodel> complaintList;
    private Context context;

    public admincomplaintadapter(List<admincomplaintmodel> complaintList, Context context) {
        this.complaintList = complaintList;
        this.context = context;
    }

    @NonNull
    @Override
    public ComplaintViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.trackpage_item_complaint, parent, false);
        return new ComplaintViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComplaintViewHolder holder, int position) {
        admincomplaintmodel complaint = complaintList.get(position);

        String statusText = complaint.getStatus(); // Store status in a local variable

        holder.issueType.setText(complaint.getIssueType());
        holder.issueDetails.setText(complaint.getIssueDetails());
        holder.status.setText(statusText); // Set the text

        // --- Status Color Logic Added Here ---
        int colorResId;

        // Use equalsIgnoreCase for flexible status matching
        if (statusText != null) {
            if (statusText.equalsIgnoreCase("completed")) {
                // Use a defined color resource for green
                colorResId = R.color.colorCompletedGreen;
            } else if (statusText.equalsIgnoreCase("pending")) {
                // Use a defined color resource for orange
                colorResId = R.color.colorPendingOrange;
            } else if (statusText.equalsIgnoreCase("rejected")) {
                // Use a defined color resource for red
                colorResId = R.color.colorRejectedRed;
            } else {
                // Default color if status doesn't match (e.g., black or primary text color)
                // Assuming R.color.black exists, adjust as necessary
                colorResId = R.color.black;
            }

            // Apply the color safely using ContextCompat
            holder.status.setTextColor(ContextCompat.getColor(context, colorResId));
        }
        // ------------------------------------

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, actadmincomplaintdetails.class);
            intent.putExtra("complaint_title", complaint.getIssueType());
            intent.putExtra("complaint_details", complaint.getIssueDetails());
            intent.putExtra("complaint_id", complaint.getid());
            intent.putExtra("complaint_status", statusText); // Use the local status variable
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return complaintList.size();
    }

    static class ComplaintViewHolder extends RecyclerView.ViewHolder {
        TextView issueType;
        TextView issueDetails;
        TextView status;

        public ComplaintViewHolder(@NonNull View itemView) {
            super(itemView);
            issueType = itemView.findViewById(R.id.tv_complaint_title);
            issueDetails = itemView.findViewById(R.id.tv_complaint_details);
            status = itemView.findViewById(R.id.tv_complaint_status);
        }
    }
}