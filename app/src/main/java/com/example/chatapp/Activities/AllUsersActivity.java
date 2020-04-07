package com.example.chatapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.example.chatapp.Adapters.AllUsersAdapter;
import com.example.chatapp.Models.User;
import com.example.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class AllUsersActivity extends AppCompatActivity {

    private static final String TAG = "AllUsersActivity";

    private Toolbar toolbar;

    private RecyclerView rvAllUsers;
    private AllUsersAdapter adapter;
    private List<User> userList;

    private DatabaseReference mRef;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        setupToolbar();
        setupRecyclerView();
    }

    private void setupRecyclerView() {

        rvAllUsers = findViewById(R.id.rvAllUsers);
        rvAllUsers.setHasFixedSize(true);
        rvAllUsers.setLayoutManager(new LinearLayoutManager(this));

        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mRef = database.getReference();
        mRef.child(getString(R.string.dbname_users)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList = new ArrayList<>();
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    User user = ds.getValue(User.class);
                    userList.add(user);
                }
                Log.d(TAG,userList.get(0).getDisplayName());
                adapter = new AllUsersAdapter(AllUsersActivity.this,userList);
                rvAllUsers.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setupToolbar() {
        Log.d(TAG,"setupToolbar");
        toolbar = findViewById(R.id.allUser_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.toolbar_all_users));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null){

        }else {
            mRef.child(getString(R.string.dbname_users))
                    .child(currentUser.getUid()).child("online").setValue(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mRef.child(getString(R.string.dbname_users))
                .child(mAuth.getCurrentUser().getUid())
                .child("online").setValue(ServerValue.TIMESTAMP);
    }
}
