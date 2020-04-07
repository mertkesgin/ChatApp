package com.example.chatapp.Login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;


import com.example.chatapp.R;
import com.example.chatapp.Utils.FirebaseMethods;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private Toolbar toolbar;
    private TextInputLayout regdisplayName,regEmail,regPassword;
    private Button btnCreateAccount;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference mRef;
    private FirebaseMethods firebaseMethods;

    private Context mContext = RegisterActivity.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        setupToolbar();
        registerWidgets();
    }

    private void registerWidgets() {
        Log.d(TAG,"registerWidgets");
        regdisplayName = findViewById(R.id.reg_display_name);
        progressBar = findViewById(R.id.register_progressBar);
        regEmail = findViewById(R.id.reg_email);
        regPassword = findViewById(R.id.reg_password);
        btnCreateAccount = findViewById(R.id.btn_create_account);

        mAuth = FirebaseAuth.getInstance();
        firebaseMethods = new FirebaseMethods(getApplicationContext());

        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"registerWidgets: onClick: creatingAccount");
                String display_name = regdisplayName.getEditText().getText().toString();
                String email = regEmail.getEditText().getText().toString();
                String password = regPassword.getEditText().getText().toString();
                if (!TextUtils.isEmpty(display_name) || !TextUtils.isEmpty(email) || !TextUtils.isEmpty(password)){
                    progressBar.setVisibility(View.VISIBLE);
                    firebaseMethods.createAccount(email,password,display_name);
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.register_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
