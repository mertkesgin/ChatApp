package com.example.chatapp.Login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.chatapp.Activities.MainActivity;
import com.example.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;

public class StartActivity extends AppCompatActivity {

    private Button btnToRegister,btnToLogIn;
    private Context mContext = StartActivity.this;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null){
            Intent intent = new Intent(mContext, MainActivity.class);
            startActivity(intent);
        }

        btnToRegister = findViewById(R.id.btnToRegister);
        btnToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toReg_intent = new Intent(mContext, RegisterActivity.class);
                startActivity(toReg_intent);
            }
        });
        btnToLogIn = findViewById(R.id.btnToLogIn);
        btnToLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toLogIn_intent = new Intent(mContext, LoginActivity.class);
                startActivity(toLogIn_intent);
            }
        });
    }
}
