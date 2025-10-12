package com.SIMATS.smartcity;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // NEW IMPORT for Glide

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<User> userList;
    private Context context;

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserId, tvUsername, tvEmail;
        ImageView imgProfile; // This is our target ImageView

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserId = itemView.findViewById(R.id.tv_userid);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvEmail = itemView.findViewById(R.id.tv_email);
            imgProfile = itemView.findViewById(R.id.img_profile);
        }
    }

    public UserAdapter(List<User> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        holder.tvUserId.setText("ID: " + user.getUserId());
        holder.tvUsername.setText(user.getUsername());
        holder.tvEmail.setText(user.getEmail());

        // --- NEW: IMAGE LOADING LOGIC WITH GLIDE ---
        String imageUrl = user.getProfilePicUrl();

        // Construct the full URL if the path is not empty
        if (imageUrl != null && !imageUrl.isEmpty()) {
            String fullUrl = actapiconfig.getPublicAPI() + imageUrl;

            Glide.with(context)
                    .load(fullUrl)
                    .circleCrop() // Makes the image circular
                    .placeholder(R.drawable.ic_person) // Image to show while loading
                    .error(R.drawable.ic_person) // Image to show if loading fails
                    .into(holder.imgProfile);
        } else {
            // If there is no image URL, load the default drawable
            Glide.with(context)
                    .load(R.drawable.ic_person)
                    .circleCrop()
                    .into(holder.imgProfile);
        }
        // --- END OF NEW LOGIC ---

        // Click listener for each item
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, adminuserdetails.class);
            intent.putExtra("user_id", user.getUserId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}