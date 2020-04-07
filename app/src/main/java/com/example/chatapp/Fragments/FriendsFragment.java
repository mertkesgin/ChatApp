package com.example.chatapp.Fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chatapp.Activities.ChatActivity;
import com.example.chatapp.Models.Friends;
import com.example.chatapp.Models.User;
import com.example.chatapp.Activities.ProfileActivity;
import com.example.chatapp.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


public class FriendsFragment extends Fragment {

    private RecyclerView rvFriends;

    private DatabaseReference mFriendsRef;
    private DatabaseReference mUsersRef;
    private FirebaseAuth mAuth;
    private String currentUserID;

    private View view;

    public FriendsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_friends, container, false);

        rvFriends = view.findViewById(R.id.rvFriends);
        rvFriends.setHasFixedSize(true);
        rvFriends.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        mFriendsRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbname_friends))
                .child(currentUserID);
        mFriendsRef.keepSynced(true);
        mUsersRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbname_users));
        mUsersRef.keepSynced(true);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Friends> options = new FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(mFriendsRef,Friends.class).build();

        FirebaseRecyclerAdapter<Friends,FriendsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull Friends friends) {
                holder.setDate(friends.getDate());
                final String userID = getRef(position).getKey();

                mUsersRef.child(userID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final User user = dataSnapshot.getValue(User.class);
                        holder.setDisplayName(user.getDisplayName());
                        holder.setImage(user.getImage());
                        holder.view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CharSequence options[] = new CharSequence[]{"Open Profile","Send Message"};
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Select");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (which == 0){
                                            Intent toProfilIntent = new Intent(getContext(), ProfileActivity.class);
                                            toProfilIntent.putExtra("userID",userID);
                                            startActivity(toProfilIntent);
                                        }
                                        if (which == 1){
                                            Intent toMessageIntent = new Intent(getContext(), ChatActivity.class);
                                            toMessageIntent.putExtra("userID",userID);
                                            toMessageIntent.putExtra("toolbarTitle",user.getDisplayName());
                                            startActivity(toMessageIntent);
                                        }
                                    }
                                });
                                builder.show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_card_desing,parent,false);
                return new FriendsViewHolder(view);
            }
        };
        rvFriends.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{

        View view;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            view = itemView;
        }

        public void setDate(String date){
            TextView tvDate =  view.findViewById(R.id.user_status);
            tvDate.setText(date);
        }

        public void setDisplayName(String displayName){
            TextView tvDisplayName = view.findViewById(R.id.user_display_name);
            tvDisplayName.setText(displayName);
        }

        public void setImage(String imageURL){
            ImageView image = view.findViewById(R.id.img_pp);
            if (!imageURL.equals("default")){
                Picasso.get().load(imageURL).into(image);
            }else {
                image.setImageResource(R.drawable.profile_photo);
            }
        }
    }
}
