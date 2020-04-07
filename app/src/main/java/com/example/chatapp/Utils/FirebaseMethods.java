package com.example.chatapp.Utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.chatapp.Models.User;
import com.example.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class FirebaseMethods {

    private static final String TAG = "FirebaseMethods";

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference mRef;
    private String userID;

    private Context mContext;

    public FirebaseMethods(Context context){
        Log.d(TAG,"FirebaseMethods: Constructor");
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        mRef = database.getReference();
        mContext = context;

        if (mAuth.getCurrentUser() != null){
            userID = mAuth.getCurrentUser().getUid();
        }
    }

    public void createAccount(final String email, final String password, final String display_name) {
        Log.d(TAG,"createAccount");
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG,"createAccount: onComplete");
                if (task.isSuccessful()){
                    Log.d(TAG,"task.isSuccessful: user auth" + task.isSuccessful());
                    String user_id = mAuth.getCurrentUser().getUid();
                    String tokenID = FirebaseInstanceId.getInstance().getToken();
                    User userInfo = new User(display_name,mContext.getString(R.string.default_status),"default",user_id,tokenID);

                    mRef.child(mContext.getString(R.string.dbname_users))
                            .child(user_id)
                            .setValue(userInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d(TAG,"createAccount: onComplete(mRef)");
                            if (task.isSuccessful()){
                                Toast.makeText(mContext, mContext.getString(R.string.registration_successful), Toast.LENGTH_SHORT).show();
                                mAuth.signOut();
                            }else {
                                Toast.makeText(mContext,mContext.getString(R.string.registration_failed)+ task.getException(), Toast.LENGTH_SHORT).show();
                                mAuth.getCurrentUser().delete();
                            }
                        }
                    });
                }else {
                    Toast.makeText(mContext, mContext.getString(R.string.registration_failed), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public User getUserInfo(DataSnapshot dataSnapshot){
        Log.d(TAG,"getUserInfo");
        User userInfo = new User();
        for (DataSnapshot ds:dataSnapshot.getChildren()){
            if (ds.getKey().equals(mContext.getString(R.string.dbname_users))){
                userInfo.setDisplayName(
                        ds.child(userID)
                                .getValue(User.class)
                                .getDisplayName()
                );
                userInfo.setStatus(
                        ds.child(userID)
                                .getValue(User.class)
                                .getStatus()
                );
                userInfo.setImage(
                        ds.child(userID)
                                .getValue(User.class)
                                .getImage()
                );
                userInfo.setUserID(
                        ds.child(userID)
                                .getValue(User.class)
                                .getUserID()
                );
            }
        }
        return userInfo;
    }

    public void setStatus(String status){
        mRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .child(mContext.getString(R.string.db_child_status))
                .setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(mContext, "Changes saved", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(mContext, "Changes could not be saved", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void updateProfilePhoto(String url){
        mRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .child("image")
                .setValue(url);
    }
}
