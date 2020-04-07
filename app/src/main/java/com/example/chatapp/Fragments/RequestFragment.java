package com.example.chatapp.Fragments;


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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.Activities.ProfileActivity;
import com.example.chatapp.Models.FriendRequest;
import com.example.chatapp.Models.User;
import com.example.chatapp.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {

    private RecyclerView rvRequests;

    private DatabaseReference mRequestsRef;
    private DatabaseReference mUsersRef;
    private FirebaseUser currentUser;

    private View view;

    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_request, container, false);

        rvRequests = view.findViewById(R.id.rvRequests);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mUsersRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbname_users));
        mUsersRef.keepSynced(true);
        mRequestsRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.dbname_friend_request)).child(currentUser.getUid());
        mRequestsRef.keepSynced(true);

        rvRequests.setHasFixedSize(true);
        rvRequests.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<FriendRequest> options = new FirebaseRecyclerOptions.Builder<FriendRequest>().setQuery(mRequestsRef,FriendRequest.class).build();
        FirebaseRecyclerAdapter<FriendRequest,RequestViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<FriendRequest, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final RequestViewHolder holder, int position, @NonNull FriendRequest model) {
                final String userID = getRef(position).getKey();

                DatabaseReference requestTypeRef = getRef(position).child("request_type").getRef();
                requestTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            String requestType = dataSnapshot.getValue().toString();

                            if (requestType.equals("received")){
                                mUsersRef.child(userID).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        User user = dataSnapshot.getValue(User.class);
                                        holder.setDisplayName(user.getDisplayName());
                                        holder.setImage(user.getImage());

                                        holder.btnAccept.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                holder.acceptFriendRequest(currentUser.getUid(),userID);
                                            }
                                        });

                                        holder.btnDecline.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                holder.declineFriendRequest(currentUser.getUid(),userID);
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }else {
                                holder.requestLayout.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_request_design,parent,false);
                return new RequestViewHolder(view);
            }
        };
        rvRequests.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {

        View view;
        ConstraintLayout requestLayout;
        TextView tvFriendRequestName;
        ImageView img_pp;
        Button btnAccept,btnDecline;

        DatabaseReference mRef;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;

            mRef = FirebaseDatabase.getInstance().getReference();

            tvFriendRequestName = view.findViewById(R.id.friend_request_name);
            img_pp = view.findViewById(R.id.img_friend_request_pp);
            btnAccept = view.findViewById(R.id.btn_accept);
            btnDecline = view.findViewById(R.id.btn_decline);
            requestLayout = view.findViewById(R.id.requestLayout);
        }

        public void setImage(String imgURL){
            Picasso.get().load(imgURL).into(img_pp);
        }

        public void setDisplayName(String name){
            tvFriendRequestName.setText(name);
        }

        public void acceptFriendRequest(String currentUserID,String requestUserID){
            final String currentDate = DateFormat.getDateInstance().format(new Date());

            Map friendsMap = new HashMap();
            friendsMap.put("friends" + "/" + currentUserID + "/" + requestUserID + "/date",currentDate);
            friendsMap.put("friends" + "/" + requestUserID + "/" + currentUserID + "/date",currentDate);


            friendsMap.put("friend_request" + "/" + currentUserID + "/" + requestUserID,null);
            friendsMap.put("friend_request" + "/" + requestUserID + "/" + currentUserID,null);

            mRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                }
            });
        }

        public void declineFriendRequest(String currentUserID,String requestUserID){
            Map declineRequestMap = new HashMap();
            declineRequestMap.put("friend_request" + "/" + currentUserID + "/" + requestUserID,null);
            declineRequestMap.put("friend_request" + "/" + requestUserID + "/" + currentUserID,null);

            mRef.updateChildren(declineRequestMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                }
            });
        }
    }
}
