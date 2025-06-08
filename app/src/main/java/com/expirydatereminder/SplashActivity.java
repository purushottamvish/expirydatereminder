package com.expirydatereminder;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import java.util.Objects;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Objects.requireNonNull(getSupportActionBar()).hide();

        new Handler().postDelayed(() -> {

            SharedPreferences preferences = getSharedPreferences("login",MODE_PRIVATE);
            boolean check = preferences.getBoolean("flag", false);
            final Intent i;
            if (check){ // for true (user logged in)
                i = new Intent(SplashActivity.this, MainActivity.class);
            }else {
                i = new Intent(SplashActivity.this, SignupActivity.class);
            }
            startActivity(i);
            finish();
        }, 2000);

    }
}