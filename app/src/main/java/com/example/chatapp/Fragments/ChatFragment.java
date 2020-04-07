package com.example.chatapp.Fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chatapp.Activities.ChatActivity;
import com.example.chatapp.Models.Chats;
import com.example.chatapp.R;
import com.example.chatapp.Utils.TimestampConverter;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ChatFragment extends Fragment {

    private RecyclerView rvChats;

    private DatabaseReference mChatRef;
    private DatabaseReference mMessagesRef;
    private DatabaseReference mUsersRef;
    private FirebaseUser currentUser;

    private View view;

    public ChatFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_chat,container,false);

        rvChats = view.findViewById(R.id.rvChats);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mUsersRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbname_users));
        mUsersRef.keepSynced(true);
        mMessagesRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbname_messages)).child(currentUser.getUid());
        mMessagesRef.keepSynced(true);
        mChatRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbname_chat))
                .child(currentUser.getUid());
        mChatRef.keepSynced(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        rvChats.setHasFixedSize(true);
        rvChats.setLayoutManager(linearLayoutManager);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        Query chatQuery = mChatRef.orderByChild("timestamp");

        FirebaseRecyclerOptions<Chats> options = new FirebaseRecyclerOptions.Builder<Chats>().setQuery(chatQuery,Chats.class).build();
        FirebaseRecyclerAdapter<Chats,ChatViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Chats, ChatViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatViewHolder holder, int position, @NonNull Chats model) {

                final String chatUserID = getRef(position).getKey();


                Query lastMessage = mMessagesRef.child(chatUserID).limitToLast(1);
                lastMessage.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        String lastMessage = dataSnapshot.child("message").getValue().toString();
                        String lastMessageTime = dataSnapshot.child("time").getValue().toString();
                        holder.setMessage(lastMessage);
                        holder.setTime(lastMessageTime,getContext());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                mUsersRef.child(chatUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String displayName = dataSnapshot.child("displayName").getValue().toString();
                        String img_pp = dataSnapshot.child("image").getValue().toString();

                        holder.setDisplayName(displayName);
                        holder.setImage(img_pp);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                holder.chatLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), ChatActivity.class);
                        intent.putExtra("userID",chatUserID);
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_ui_design,parent,false);
                return new ChatViewHolder(view);
            }
        };
        rvChats.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder{

        View mView;
        ConstraintLayout chatLayout;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;
            chatLayout = mView.findViewById(R.id.chat_layout);
        }

        public void setMessage(String message){
            TextView tvLastMessage = mView.findViewById(R.id.lastMessage);
            tvLastMessage.setText(message);
        }
        public void setDisplayName(String displayName){
            TextView tvDisplayName = mView.findViewById(R.id.chat_display_name);
            tvDisplayName.setText(displayName);
        }
        public void setImage(String imageURL){
            ImageView image = mView.findViewById(R.id.img_user_pp);
            if (!imageURL.equals("default")){
                Picasso.get().load(imageURL).into(image);
            }else {
                image.setImageResource(R.drawable.profile_photo);
            }
        }
        public void setTime(String time, Context mContext){
            TextView tvDate =  mView.findViewById(R.id.chat_message_time);
            tvDate.setText(TimestampConverter.getTimeAgo(Long.parseLong(time),mContext));
        }
    }
}
