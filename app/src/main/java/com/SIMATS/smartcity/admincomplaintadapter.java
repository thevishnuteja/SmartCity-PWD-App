package com.SIMATS.smartcity;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
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
        holder.issueType.setText(complaint.getIssueType());
        holder.issueDetails.setText(complaint.getIssueDetails());
        holder.status.setText(complaint.getStatus());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, actadmincomplaintdetails.class);
            intent.putExtra("complaint_title", complaint.getIssueType());
            intent.putExtra("complaint_details", complaint.getIssueDetails());
            intent.putExtra("complaint_id", complaint.getid());
            intent.putExtra("complaint_status", complaint.getStatus());
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

