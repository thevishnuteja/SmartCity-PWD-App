package com.SIMATS.smartcity;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ComplaintAdapter extends RecyclerView.Adapter<ComplaintAdapter.ComplaintViewHolder> {
    private List<Complaintmodel> complaintList;
    private Context context;

    public ComplaintAdapter(List<Complaintmodel> complaintList, Context context) {
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
        Complaintmodel complaint = complaintList.get(position);
        holder.issueType.setText(complaint.getIssueType());
        holder.issueDetails.setText(complaint.getIssueDetails());
        holder.status.setText(complaint.getStatus());

        // Change status color based on value
        String status = complaint.getStatus().toLowerCase();
        if (status.equals("pending")) {
            holder.status.setTextColor(ContextCompat.getColor(context, android.R.color.holo_orange_dark));
        } else if (status.equals("completed")) {
            holder.status.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
        } else if (status.equals("rejected")) {
            holder.status.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
        } else {
            holder.status.setTextColor(ContextCompat.getColor(context, android.R.color.black)); // default
        }

        // Click Listener to open ComplaintDetails activity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, actcomplaintdetails.class);
            intent.putExtra("complaint_title", complaint.getIssueType());
            intent.putExtra("complaint_details", complaint.getIssueDetails());
            intent.putExtra("complaint_id", complaint.getid());
            intent.putExtra("complaint_status", complaint.getStatus());
            intent.putExtra("location", complaint.getlocation());
            intent.putExtra("datetime", complaint.getdatetime());
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
