package cse340.finalproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter{

    private final List<Message> mMessageList;

    public MessagesAdapter(List<Message> messageList) {
        mMessageList = messageList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View currentMessage;

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
                ((RightMessageHolder) holder).bind(message);
                break;
            case Message.SEND_BY_BOT:
                ((LeftMessageHolder) holder).bind(message);
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

    private class LeftMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText;

        public LeftMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.left_message_text);
        }
        public void bind(Message message) {
            messageText.setText(message.getMessage());
        }
    }

    private class RightMessageHolder extends RecyclerView.ViewHolder {
        TextView messageText;

        public RightMessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.right_message_text);
        }
        public void bind(Message message) {
            messageText.setText(message.getMessage());
        }
    }

}
