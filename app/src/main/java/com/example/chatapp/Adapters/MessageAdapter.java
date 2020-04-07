package com.example.chatapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.Models.Messages;
import com.example.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{

    private List<Messages> messagesList;
    private Context mContext;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public MessageAdapter(List<Messages> messagesList, Context mContext) {
        this.messagesList = messagesList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_sender,parent,false);
            return new MessageViewHolder(view);
        }else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_receiver,parent,false);
            return new MessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Messages messages = messagesList.get(position);

        holder.textViewMessage.setText(messages.getMessage());
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (messagesList.get(position).getFrom().equals(mAuth.getCurrentUser().getUid())){
            return 1;
        }else return 2;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        TextView textViewMessage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewMessage = itemView.findViewById(R.id.textViewMessage);
        }
    }
}
