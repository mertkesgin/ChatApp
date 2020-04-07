package com.example.chatapp.Login;

import androidx.annotation.NonNull;
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
import android.widget.Toast;

import com.example.chatapp.Activities.MainActivity;
import com.example.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private Toolbar toolbar;

    private TextInputLayout loginEmail,loginPassword;
    private Button btnLogin;

    private ProgressBar progressBar;

    private FirebaseAuth mAuth;

    private Context mContext = LoginActivity.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setupToolbar();
        loginWidgets();
    }

    private void loginWidgets() {
        Log.d(TAG,"initViews");
        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        btnLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.login_progressBar);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressBar.setVisibility(View.VISIBLE);

                String email = loginEmail.getEditText().getText().toString();
                String password = loginPassword.getEditText().getText().toString();
                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
                    loginUser(email,password);
                }else {
                    Toast.makeText(mContext, "Fields can not be empty", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void loginUser(String email, String password) {
        Log.d(TAG,"loginUser");
        mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()){
                    Log.d(TAG,"loginUser: task.isSuccesfull: " + task.isSuccessful());
                    progressBar.setVisibility(View.INVISIBLE);
                    Intent toMainIntent = new Intent(mContext, MainActivity.class);
                    toMainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(toMainIntent);
                    finish();
                }else {
                    Log.d(TAG,"loginUser: task.isNOTSuccesfull" + task.getException());
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(mContext, "Please check email and password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupToolbar() {
        Log.d(TAG,"setupToolbar");
        toolbar = findViewById(R.id.login_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Log In");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
