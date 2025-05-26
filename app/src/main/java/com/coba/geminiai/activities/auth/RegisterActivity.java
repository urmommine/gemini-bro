package com.coba.geminiai.activities.auth;

import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.coba.geminiai.activities.MainActivity;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;

import com.coba.geminiai.R;

public class RegisterActivity extends AppCompatActivity {
    private Button btnRegister;
    private TextView tvLogin;
    private EditText etEmail, etPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        btnRegister.setOnClickListener(view -> {
            String getEmail = etEmail.getText().toString().trim();
            String getPassword = etPassword.getText().toString().trim();

            if (!getEmail.isEmpty() || getPassword.isEmpty()){
                mAuth.createUserWithEmailAndPassword(getEmail, getPassword)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()){
                                Toast.makeText(RegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                finish();
                            }else{
                                Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();                                return;
                            }
                        });
            }else{
                Toast.makeText(RegisterActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

        });

        tvLogin.setOnClickListener(view -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        });
    }
}