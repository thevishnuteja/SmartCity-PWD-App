package com.SIMATS.smartcity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.MessageViewHolder> {

    private final List<ChatMessage> messageList;
    private final OnChatButtonClickListener listener;

    // Interface to communicate button clicks back to the Activity
    public interface OnChatButtonClickListener {
        void onChatButtonClick(ChatMessage message);
    }

    public ChatMessageAdapter(List<ChatMessage> messageList, OnChatButtonClickListener listener) {
        this.messageList = messageList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == ChatMessage.TYPE_USER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_user, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_ai, parent, false);
        }
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);
        holder.bind(message, listener);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return messageList.get(position).getType();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        Button createComplaintButton;
        ImageView messageImageView;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.message_text);
            // These might be null if the view is from the user's layout
            createComplaintButton = itemView.findViewById(R.id.btn_create_complaint);
            messageImageView = itemView.findViewById(R.id.message_image);
        }

        void bind(final ChatMessage message, final OnChatButtonClickListener listener) {
            messageTextView.setText(message.getText());

            // Handle the action button for AI messages
            if (createComplaintButton != null) {
                String buttonText = message.getButtonText();
                if (buttonText != null && !buttonText.isEmpty()) {
                    createComplaintButton.setVisibility(View.VISIBLE);
                    createComplaintButton.setText(buttonText);
                    // Make sure listener is not null before setting OnClickListener
                    if (listener != null) {
                        createComplaintButton.setOnClickListener(v -> listener.onChatButtonClick(message));
                    }
                } else {
                    createComplaintButton.setVisibility(View.GONE);
                }
            }

            // Handle the image for user messages
            if (messageImageView != null) {
                if (message.getImage() != null) {
                    messageImageView.setImageBitmap(message.getImage());
                    messageImageView.setVisibility(View.VISIBLE);
                } else {
                    messageImageView.setVisibility(View.GONE);
                }
            }
        }
    }
}