package cse340.finalproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 The MessagesAdapter class represents an adapter for a RecyclerView that displays a list of messages
 in a chat interface
 */
public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    // A list of messages to display in the RecyclerView
    private final List<Message> mMessageList;

    View currentMessage;

    /**
     * Constructs a new MessagesAdapter with the given list of messages
     * @param messageList the list of messages to display
     */
    public MessagesAdapter(List<Message> messageList) {
        mMessageList = messageList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == Message.SEND_BY_ME) {
            currentMessage = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.right_side_message, parent, false);
            return new RightMessageHolder(currentMessage);

        } else  {
            currentMessage = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.left_side_message, parent, false);
            return new LeftMessageHolder(currentMessage);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = mMessageList.get(position);
        switch (holder.getItemViewType()) {
            case Message.SEND_BY_ME:
                ((RightMessageHolder) holder).setMessageText(message);
                break;
            case Message.SEND_BY_BOT:
                ((LeftMessageHolder) holder).setMessageText(message);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mMessageList.get(position).getSender();
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    /**
     * A ViewHolder for messages sent by the therapist
     */
    private static class LeftMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText;  // TextView to display the message text

        /**
         Constructs a new LeftMessageHolder object with the given View object
         @param itemView The View object for this holder
         */
        public LeftMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.left_message_text);
        }

        /**
         Sets the given Message object to the view holder
         @param message The Message object to set
         */
        public void setMessageText(Message message) {
            messageText.setText(message.getMessage());
        }
    }

    /**
     * A ViewHolder for messages sent by the user
     */
    private static class RightMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText;  // TextView to display the message text

        /**
         Constructs a new RightMessageHolder object with the given View object
         @param itemView The View object for this holder
         */
        public RightMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.right_message_text);
        }

        /**
         Sets the given Message object to the view holder
         @param message The Message object to set
         */
        public void setMessageText(Message message) {
            messageText.setText(message.getMessage());
        }
    }

}
