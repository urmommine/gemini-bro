package com.coba.geminiai.activities.auth;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.coba.geminiai.activities.MainActivity;
import com.google.firebase.auth.FirebaseAuth;


import com.coba.geminiai.R;

public class LoginActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        btnLogin.setOnClickListener(view -> {
            String getEmail = etEmail.getText().toString().trim();
            String getPassword = etPassword.getText().toString().trim();

            if (!getEmail.isEmpty() && !getPassword.isEmpty()) {
                mAuth.signInWithEmailAndPassword(getEmail, getPassword)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()){
                                Toast.makeText(LoginActivity.this, "Selamat Datang!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            }else{
                                Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }else{
                Toast.makeText(LoginActivity.this, "Penuhi semua field!", Toast.LENGTH_SHORT).show();
                return;
            }
        });

        tvRegister.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });

    }
}