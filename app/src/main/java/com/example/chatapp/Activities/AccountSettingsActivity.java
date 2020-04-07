package com.example.chatapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.Models.User;
import com.example.chatapp.R;
import com.example.chatapp.Utils.FirebaseMethods;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountSettingsActivity extends AppCompatActivity {

    private static final String TAG = "AccountSettingsActivity";

    private FirebaseDatabase database;
    private DatabaseReference mRef;
    private FirebaseAuth mAuth;
    private StorageReference mStorageRef;
    private FirebaseMethods firebaseMethods;
    private String userID;

    private Toolbar toolbar;

    private CircleImageView settings_img;
    private TextView status;
    private TextView displayName;
    private Button btnChangeStatus,btnChangePhoto;
    private ProgressBar progressBar;
    private Dialog mDialog;

    static int PReqCode = 1;
    static int REQUESTCODE = 1;
    private Uri pickedImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        setupToolbar();
        setupFirebase();
        initViews();

        btnChangeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopUp();
            }
        });
        
        btnChangePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >=22){
                    checkAndRequestForPermission();
                }else {
                    openGallery();
                }
            }
        });
        
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User userInfo = firebaseMethods.getUserInfo(dataSnapshot);
                status.setText(userInfo.getStatus());
                displayName.setText(userInfo.getDisplayName());
                if (!userInfo.getImage().equals("default")){
                    Picasso.get().load(userInfo.getImage()).into(settings_img);
                }else {
                    settings_img.setImageResource(R.drawable.profile_photo);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,REQUESTCODE);
    }

    private void checkAndRequestForPermission() {
        if (ContextCompat.checkSelfPermission(AccountSettingsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(AccountSettingsActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                Toast.makeText(getApplicationContext(), "Lütfen gerekli izinleri kabul edin", Toast.LENGTH_SHORT).show();
            }else {
                ActivityCompat.requestPermissions(AccountSettingsActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PReqCode);
            }
        }else {
            openGallery();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUESTCODE && data != null){

            progressBar.setVisibility(View.VISIBLE);

            pickedImageUrl = data.getData();

            final StorageReference filePath = mStorageRef.child(getString(R.string.dbstorage_profile_photos))
                    .child(userID)
                    .child(pickedImageUrl.getLastPathSegment());
            filePath.putFile(pickedImageUrl).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()){
                        progressBar.setVisibility(View.INVISIBLE);
                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                firebaseMethods.updateProfilePhoto(uri.toString());
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        });
                        Toast.makeText(AccountSettingsActivity.this, "Profile photo uptated", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(AccountSettingsActivity.this, "Profile photo uptate failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),pickedImageUrl);
                settings_img.setImageBitmap(Bitmap.createScaledBitmap(bitmap,1024,1024,false));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showPopUp() {
        mDialog.setContentView(R.layout.dialog_change_status);
        final EditText etStatus = mDialog.findViewById(R.id.etStatus);
        Button btnSaveChanges = mDialog.findViewById(R.id.btnSaveChanges);
        Button btnCancel = mDialog.findViewById(R.id.btnCancel);

        btnSaveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status = etStatus.getText().toString();
                firebaseMethods.setStatus(status);
                mDialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.show();
    }

    private void initViews() {
        settings_img = findViewById(R.id.img_settings);
        status = findViewById(R.id.tvSettingsStatus);
        displayName = findViewById(R.id.tvSettingsName);
        btnChangeStatus = findViewById(R.id.btnChangeStatus);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);
        progressBar = findViewById(R.id.account_s_progressBar);
        mDialog = new Dialog(this);
    }

    private void setupFirebase() {
        database = FirebaseDatabase.getInstance();
        mRef = database.getReference();
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        firebaseMethods = new FirebaseMethods(AccountSettingsActivity.this);
        userID = mAuth.getCurrentUser().getUid();
    }

    private void setupToolbar() {
        Log.d(TAG,"setupToolbar");
        toolbar = findViewById(R.id.settingsToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Account Settings");
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
