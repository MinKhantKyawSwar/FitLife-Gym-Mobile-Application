package com.example.fitlife;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.example.fitlife.utils.SessionManager;

/**
 * Splash screen activity - first screen shown when app launches
 * Checks if user is logged in and navigates accordingly
 */
public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_DELAY = 2000; // 2 seconds
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        SessionManager sessionManager = new SessionManager(this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (sessionManager.isLoggedIn()) {
                    // User is logged in, check if details exist
                    DatabaseHelper dbHelper = new DatabaseHelper(SplashActivity.this);
                    if (dbHelper.userDetailsExist(sessionManager.getUserId())) {
                        // Go to home
                        startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                    } else {
                        // Go to add details
                        startActivity(new Intent(SplashActivity.this, AddDetailsActivity.class));
                    }
                } else {
                    // User not logged in, go to login
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                }
                overridePendingTransition(0, 0);
                finish();
            }
        }, SPLASH_DELAY);
    }
}
