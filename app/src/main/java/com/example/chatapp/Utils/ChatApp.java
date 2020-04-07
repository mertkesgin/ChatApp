package com.example.chatapp.Utils;

import android.app.Application;

import androidx.annotation.NonNull;

import com.example.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

public class ChatApp extends Application {

    private DatabaseReference mRef;
    private FirebaseAuth mAuth;

    @Override
    public void onCreate() {
        super.onCreate();

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null){
            mRef = FirebaseDatabase.getInstance().getReference()
                    .child(getString(R.string.dbname_users))
                    .child(mAuth.getCurrentUser().getUid());

            mRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot != null){
                        mRef.child("online").onDisconnect().setValue(ServerValue.TIMESTAMP);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
}
