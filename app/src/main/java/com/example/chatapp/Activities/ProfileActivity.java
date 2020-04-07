package com.example.chatapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.Models.User;
import com.example.chatapp.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private String TAG = "ProfileActivity";

    private ImageView profileImage;
    private TextView profileName,profileStatus;
    private Button btnSendRequest,btnDecline;

    private Toolbar toolbar;

    private FirebaseDatabase database;
    private DatabaseReference mRef;
    private FirebaseUser firebaseUser;
    private String currentState;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userID = getIntent().getStringExtra("userID");
        currentState = "not_friends";
        setupFirebase();
        setupToolbar();
        initViews();
        setUserInfo();
        checkFriendshipStatus();


        btnSendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                btnSendRequest.setEnabled(false);

                if (currentState.equals("not_friends")){
                    sendFriendRequest();
                }

                if (currentState.equals("request_sent")){
                    cancelSentRequest();
                }

                if (currentState.equals("request_received")){
                    acceptFriendRequest();
                }

                if (currentState.equals("friends")){
                    removeFriend();
                }
            }
        });

        btnDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map declineRequestMap = new HashMap();
                declineRequestMap.put(getString(R.string.dbname_friend_request) + "/" + firebaseUser.getUid() + "/" + userID,null);
                declineRequestMap.put(getString(R.string.dbname_friend_request) + "/" + userID + "/" + firebaseUser.getUid(),null);

                mRef.updateChildren(declineRequestMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if (databaseError == null){
                            btnSendRequest.setText("Send Friend Request");
                            currentState = "not_friends";

                            btnDecline.setEnabled(false);
                            btnDecline.setVisibility(View.INVISIBLE);
                        }else {
                            Toast.makeText(ProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void removeFriend() {
        Map removeFriendMap = new HashMap();
        removeFriendMap.put(getString(R.string.dbname_friends) + "/" + firebaseUser.getUid(),null);
        removeFriendMap.put(getString(R.string.dbname_friends) + "/" + userID,null);

        mRef.updateChildren(removeFriendMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError == null){
                    btnSendRequest.setEnabled(true);
                    currentState = "not_friends";
                    btnSendRequest.setText("Send Friend Requestt");

                    btnDecline.setEnabled(false);
                    btnDecline.setVisibility(View.INVISIBLE);
                }else {
                    Toast.makeText(ProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void acceptFriendRequest() {
        final String currentDate = DateFormat.getDateInstance().format(new Date());

        Map friendsMap = new HashMap();
        friendsMap.put(getString(R.string.dbname_friends) + "/" + firebaseUser.getUid() + "/" + userID + "/date",currentDate);
        friendsMap.put(getString(R.string.dbname_friends) + "/" + userID + "/" + firebaseUser.getUid() + "/date",currentDate);


        friendsMap.put(getString(R.string.dbname_friend_request) + "/" + firebaseUser.getUid() + "/" + userID,null);
        friendsMap.put(getString(R.string.dbname_friend_request) + "/" + userID + "/" + firebaseUser.getUid(),null);

        mRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError == null){
                    btnSendRequest.setEnabled(true);
                    currentState = "friends";
                    btnSendRequest.setText("Remove Friend");

                    btnDecline.setEnabled(false);
                    btnDecline.setVisibility(View.INVISIBLE);
                }else {
                    Toast.makeText(ProfileActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void cancelSentRequest() {
        mRef.child(getString(R.string.dbname_friend_request))
                .child(firebaseUser.getUid())
                .child(userID).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mRef.child(getString(R.string.dbname_friend_request))
                        .child(userID)
                        .child(firebaseUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        btnSendRequest.setEnabled(true);
                        currentState = "not friends";
                        btnSendRequest.setText("Send Friend Request");

                        btnDecline.setVisibility(View.INVISIBLE);
                        btnDecline.setEnabled(false);

                        Toast.makeText(ProfileActivity.this, "Canceled Sending Request", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void sendFriendRequest() {
        String notificationID = mRef.child(getString(R.string.dbname_notifications))
                .child(userID).push().getKey();

        HashMap<String,String> notification = new HashMap<>();
        notification.put("from",firebaseUser.getUid());
        notification.put("type","request");

        Map friendRequestMap = new HashMap();
        friendRequestMap.put(getString(R.string.dbname_friend_request) + "/" + firebaseUser.getUid() + "/" + userID + "/" + "request_type","sent");
        friendRequestMap.put(getString(R.string.dbname_friend_request) + "/" + userID + "/" + firebaseUser.getUid() + "/" +"request_type","received");

        friendRequestMap.put(getString(R.string.dbname_notifications) + "/" + userID + "/" + notificationID,notification);

        mRef.updateChildren(friendRequestMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError != null){
                    Toast.makeText(ProfileActivity.this, "There was some error is sending request", Toast.LENGTH_SHORT).show();
                }
                btnSendRequest.setEnabled(true);
                btnSendRequest.setText("Cancel Friend Request");
                currentState = "request_sent";
            }
        });
    }

    private void checkFriendshipStatus() {
        mRef.child(getString(R.string.dbname_friend_request))
                .child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(userID)){
                    String requestType = dataSnapshot.child(userID).child("request_type").getValue().toString();
                    Log.d(TAG,"RequestType: " + requestType);
                    if (requestType.equals("received")){
                        currentState = "request_received";
                        btnSendRequest.setText("Accept Friend Request");
                        btnDecline.setVisibility(View.VISIBLE);
                        btnDecline.setEnabled(true);
                    }else if (requestType.equals("sent")){
                        currentState = "request_sent";
                        btnSendRequest.setText("Cancel Friend Request");
                        btnDecline.setVisibility(View.INVISIBLE);
                        btnDecline.setEnabled(false);
                    }
                }else {
                    mRef.child(getString(R.string.dbname_friends))
                            .child(firebaseUser.getUid())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(userID)){
                                        currentState = "friends";
                                        btnSendRequest.setText("Remove Friend");

                                        btnDecline.setVisibility(View.INVISIBLE);
                                        btnDecline.setEnabled(false);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setUserInfo() {
        mRef.child(getString(R.string.dbname_users)).child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                profileName.setText(user.getDisplayName());
                profileStatus.setText(user.getStatus());
                if (!user.getImage().equals("default")){
                    Picasso.get().load(user.getImage()).into(profileImage);
                }else {
                    profileImage.setImageResource(R.drawable.profile_photo);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initViews() {
        profileImage = findViewById(R.id.img_profile);
        profileName = findViewById(R.id.profile_display_name);
        profileStatus = findViewById(R.id.profile_status);
        btnSendRequest = findViewById(R.id.btnSendRequest);
        btnDecline = findViewById(R.id.btnDecline);
        btnDecline.setVisibility(View.INVISIBLE);
        btnDecline.setEnabled(false);
    }

    private void setupFirebase() {
        database = FirebaseDatabase.getInstance();
        mRef = database.getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    private void setupToolbar() {
            Log.d(TAG,"setupToolbar");
            toolbar = findViewById(R.id.profile_toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (firebaseUser == null){
        }else {
            mRef.child(getString(R.string.dbname_users))
                    .child(firebaseUser.getUid()).child("online").setValue(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mRef.child(getString(R.string.dbname_users))
                .child(firebaseUser.getUid())
                .child("online").setValue(ServerValue.TIMESTAMP);
    }
}
