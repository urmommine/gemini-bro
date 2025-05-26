package com.coba.geminiai.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.coba.geminiai.R;
import com.coba.geminiai.adapter.ViewPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.coba.geminiai.activities.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    ViewPager viewPager;
    TabLayout tabLayout;
    ImageButton btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        btnLogout = findViewById(R.id.btnLogout);

        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(1);
        tabLayout.setupWithViewPager(viewPager);

        btnLogout.setOnClickListener(view -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            Toast.makeText(this, "Logout Successful", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}